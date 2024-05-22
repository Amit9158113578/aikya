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
import com.idep.pospservice.util.MD5Encryption;
import com.idep.pospservice.util.POSPServiceConstant;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;

public class ResetAdminPassword implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AdminProfileResProcessor.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("RequestNode Of POSP Admin:"+requestNode);
			if(requestNode.get("updateInfo").asText().equalsIgnoreCase("Yes")){
				if(!requestNode.has("mobileNumber")){
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					errorNode = objectMapper.createObjectNode();
					((ObjectNode)errorNode).put("message", "unable to find user Profile , request in  mobileNumber not found : "+requestNode);
					objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.RES_CODE_SUCCESS);
					objectNode.put(POSPServiceConstant.RES_MSG,POSPServiceConstant.RES_MSG_SUCCESS);
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					throw new ExecutionTerminator();
				}
				
				String docId = POSPServiceConstant.POSP_ADMIN_PROFILE+requestNode.get("mobileNumber").asText();
				JsonDocument adminProfile = pospData.getDocBYId(docId);
				if(adminProfile!=null){
					JsonNode adminInfo = objectMapper.readTree(adminProfile.content().toString());
					((ObjectNode)adminInfo).put("password", MD5Encryption.MD5(requestNode.get("password").asText()));
					 String docStatus = pospDataSync.replacePospDataDocumentBySync(docId,JsonObject.fromJson(objectMapper.writeValueAsString(adminInfo)));
					log.info("Password Updated for Admin : doc Status : "+docStatus);
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					errorNode = objectMapper.createObjectNode();
					((ObjectNode)errorNode).put("message", "Password Updated for :  "+requestNode.get("mobileNumber").asText());
					objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.RES_CODE_SUCCESS);
					objectNode.put(POSPServiceConstant.RES_MSG,POSPServiceConstant.RES_MSG_SUCCESS);
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}else{
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					errorNode = objectMapper.createObjectNode();
					((ObjectNode)errorNode).put("message", "unable to find user Profile : "+requestNode.get("mobileNumber").asText());
					objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.RES_CODE_FAIL);
					objectNode.put(POSPServiceConstant.RES_MSG,POSPServiceConstant.RES_MSG_FAIL);
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					throw new ExecutionTerminator();
				}
			}
			
		}catch(Exception e){
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			errorNode = objectMapper.createObjectNode();
			((ObjectNode)errorNode).put("message", "unable to process request ");
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}

	}

}
