package com.idep.pospservice.user.service;

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
import com.idep.pospservice.util.MD5Encryption;
import com.idep.pospservice.util.POSPServiceConstant;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;

public class AdminProfileResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AdminProfileResProcessor.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
			log.info("RequestNode "+requestNode);
			if(requestNode.findValue("mobileNumber").textValue().equals("") || requestNode.findValue("mobileNumber").textValue()==null){
				/**
				 * if mobileNumber == "" OR ==null then profile document will not be created. 
				 * **/
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			}
			String adminDocId = POSPServiceConstant.POSP_ADMIN_PROFILE+requestNode.findValue("mobileNumber").textValue();	

			JsonDocument adminDoc = pospData.getDocBYId(adminDocId);

			if(adminDoc == null){
				long posp_seq;
				synchronized(this)
				{
					posp_seq = this.pospData.updateDBSequence(POSPServiceConstant.POSP_SEQ_AD);
				}

				String adminId = POSPServiceConstant.POSPAD_ID+posp_seq;	
				JsonObject adminDetails = JsonObject.fromJson(requestNode.toString());
				adminDetails.put(POSPServiceConstant.DOCUMENT_TYPE,"pospAdminProfile");
				adminDetails.put("password", MD5Encryption.MD5(requestNode.findValue("password").textValue()));
				adminDetails.put("adminId", adminId);
				/*adminDetails.put("userRole", "AgentConsultant");*/
				String doc_status = pospData.createDocument(adminDocId, adminDetails);
				log.info("POSP AdminId and Data : "+adminDocId+" "+"Doc Node:"+adminDetails);	

				log.info("POSP Admin : "+doc_status);
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
					JsonNode jsonNode = this.objectMapper.readTree(adminDetails.removeKey("password").toString());
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

			}else{
			JsonNode adminProfileDoc = objectMapper.readTree(adminDoc.content().toString());
			((ObjectNode)adminProfileDoc).putAll((ObjectNode)requestNode);
			String docStatus = pospDataSync.replacePospDataDocumentBySync(adminDocId,JsonObject.fromJson(objectMapper.writeValueAsString(adminProfileDoc)));
			log.info("Password Updated for Admin : doc Status : "+docStatus);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			errorNode = objectMapper.createObjectNode();
			((ObjectNode)errorNode).put("message", "Admin Profile Updated  :  "+requestNode.get("mobileNumber").asText());
			objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.RES_CODE_SUCCESS);
			objectNode.put(POSPServiceConstant.RES_MSG,POSPServiceConstant.RES_MSG_SUCCESS);
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			}

		}
		catch(Exception e)
		{
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}
}
