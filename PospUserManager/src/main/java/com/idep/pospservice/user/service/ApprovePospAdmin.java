package com.idep.pospservice.user.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.Functions;
import com.idep.pospservice.util.POSPServiceConstant;

public class ApprovePospAdmin implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ApprovePospAdmin.class.getName());

	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("Request : "+reqNode);
			if(reqNode.findValue(POSPServiceConstant.MOBILE_NO)!=null || reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText().equalsIgnoreCase("")){
				String docId = "POSPAdminProfile-"+reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText();
				JsonDocument adminDocument = pospData.getDocBYId(docId);

				if(reqNode.has("isVerified"))
				{			
					JsonObject admnProfileDoc = adminDocument.content();
					ObjectNode reqestNode = (ObjectNode)reqNode;	
					ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(admnProfileDoc.toString());
					jsonNode.put("verificationDate",Functions.getDateAndTime());
					jsonNode.put("isActive","Y");
					jsonNode.putAll(reqestNode);
					String doc_status = pospData.replaceDocument(docId, JsonObject.fromJson(jsonNode.toString()));
					log.info("Doc Status : "+doc_status);
					((ObjectNode)jsonNode).remove("password");
					if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_REPLACED)){
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,jsonNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));

					}else
					{
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					}

				}
			}

		}
		catch(Exception e){
			log.error("Unable to procss  : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}

}
