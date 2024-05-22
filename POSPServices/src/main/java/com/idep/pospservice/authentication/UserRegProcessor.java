package com.idep.pospservice.authentication;

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

public class UserRegProcessor  implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UserRegProcessor.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
			log.info("RequestNode Of POSP Registration:"+requestNode);
			if(requestNode == null){
				throw new ExecutionTerminator();
			}

			ObjectNode userNode = (ObjectNode)requestNode;	
			userNode.put(POSPServiceConstant.DOCUMENT_TYPE,"POSPUserRegistration");

			JsonObject userDetails = JsonObject.fromJson(requestNode.toString());
			String userId = POSPServiceConstant.POSP_USER_PROFILE+requestNode.findValue("mobileNo").textValue();
			JsonDocument userRegDoc = PospData.getDocBYId(userId);
			log.info("POSP User Doc Id:"+userId);
			
			if(userRegDoc == null)
			{				
				userDetails.put("registrationDate", Functions.getDateAndTime());
				String doc_status = PospData.createDocument(userId, userDetails);
				log.info("POSP User Details: "+doc_status);		
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.RES_MSG_USERNOTEXIST);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}else
				{
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getcacheDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getcacheDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}
			}
			else
			{  
				userDetails.put("updatedDate", Functions.getDateAndTime());
				String doc_status = PospData.replaceDocument(userId, userDetails);
				log.info("Updated Document:"+doc_status);
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.USER_EXIST_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.USER_EXIST_MESSAGES).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.RES_MSG_USEREXIST);
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			}
		}
		catch(Exception e)
		{
			log.error("Unable to process : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getcacheDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getcacheDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}

	}
}
