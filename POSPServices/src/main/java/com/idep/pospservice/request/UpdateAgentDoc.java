package com.idep.pospservice.request;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

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
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

public class UpdateAgentDoc implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UploadFileProcessor.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class).toString());
			log.info("Request:"+reqNode);
			ObjectNode requestNode = (ObjectNode)reqNode;	
			if(reqNode.has("adminId") || reqNode.has("agentId")){
				if(reqNode.findValue(POSPServiceConstant.MOBILE_NO)!=null || reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText().equalsIgnoreCase("")){
					String docId = "POSPUserProfile-"+reqNode.findValue(POSPServiceConstant.MOBILE_NO).asText();
					JsonNode userProfile = pospDataSync.getPospDataDocumentBySync(docId);
					
					if (userProfile != null)
					{    
					 ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(userProfile.toString());
					 jsonNode.put("updatedDate",Functions.getDateAndTime());
					 requestNode.put("agentId",userProfile.get("agentId").asText());
					 requestNode.put("mobileNumber",userProfile.get(POSPServiceConstant.MOBILE_NO).asText());
					 if(reqNode.has("adminId")){
						 jsonNode.put("updatedBy",reqNode.get("adminId").asText());	 
					 }
					 
					 if(requestNode.has("documentType")){
						 if(requestNode.get("documentType").asText().equalsIgnoreCase("businessExperience")){
							 
							 if(reqNode.has("adminId")){
								 ((ObjectNode)requestNode).remove("adminId");
							 }
							 if(reqNode.has(POSPServiceConstant.MOBILE_NO)){
								 ((ObjectNode)requestNode).remove(POSPServiceConstant.MOBILE_NO);
							 }
							 ((ObjectNode)jsonNode).put(requestNode.get("documentType").asText(),requestNode);
						 }
					 }else{
					((ObjectNode)jsonNode).putAll(requestNode);
					 }
					 String docStatus = pospDataSync.replacePospDataDocumentBySync(docId,JsonObject.fromJson(objectMapper.writeValueAsString(jsonNode)));
					 
					 log.info("User Profile document : " + docId + " status :" + docStatus);
					 
					   if(docStatus.equalsIgnoreCase(POSPServiceConstant.DOC_UPDATED)){
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));

					   }else{
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					  }
				   }
				}

			}

		}catch(Exception e){

			log.error("Unable to process : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}

}
