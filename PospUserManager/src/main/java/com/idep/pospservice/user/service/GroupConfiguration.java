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
import com.idep.pospservice.util.POSPServiceConstant;

public class GroupConfiguration implements Processor{


	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GroupConfiguration.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("RequestNode for Group :"+requestNode);	
			ObjectNode reqNode = (ObjectNode)requestNode;
			JsonObject req = JsonObject.fromJson(requestNode.toString());
			String gropuDocId ="";
			if(requestNode.has("groupId")){
				gropuDocId = 	requestNode.get("groupId").asText();
			}else{
				long posp_seq=0;
				synchronized(this)
				{
					posp_seq = this.pospData.updateDBSequence(POSPServiceConstant.POSP_SEQ_GROUP);
				}
				gropuDocId = POSPServiceConstant.POSPAG_GROUPID+posp_seq;
			}
			
			//POSPServiceConstant.GROPID+requestNode.findValue("groupName").textValue();	
			JsonDocument groupDoc = pospData.getDocBYId(gropuDocId);
			
			if(groupDoc == null){
				req.put("documentType", "UserGroup");
				req.put("isActive", "Yes");
				req.put("updatedBy", requestNode.get("adminId").asText());
				req.put("groupId", gropuDocId);
				String doc_status = pospData.createDocument(gropuDocId, req);
				log.info("Group Response :"+doc_status+","+gropuDocId);
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
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
				ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(groupDoc.content().toString());
				jsonNode.put("updatedBy", requestNode.get("adminId").asText());
				jsonNode.put("updatedDate",Functions.getDateAndTime());
				jsonNode.put("documentType", "UserGroup");
				if(requestNode.has("action")){
					if(requestNode.get("action").asText().equalsIgnoreCase("disable")){
						jsonNode.put("isActive", "No");
						
					}else if(requestNode.get("action").asText().equalsIgnoreCase("enable")){
						jsonNode.put("isActive", "Yes");
					}	
				}else {
					((ObjectNode)jsonNode).putAll(reqNode);	
				}
				
				String doc_status = pospData.replaceDocument(gropuDocId, JsonObject.fromJson(jsonNode.toString()));
				log.info("POSP Group Doc Status : "+doc_status);	
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_REPLACED)){
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
		catch(Exception e)
		{
			log.error("Unable to process : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}
/*
	public static void main(String[] args) {
		ObjectMapper obj = new ObjectMapper();
		String req = " {\"pospScreenConfig\": {     \"POSPSDASHBORD\": {       \"read\": false,       \"write\": false,       \"delete\": false     },     \"POSPSHOME\": {       \"read\": false,       \"write\": false,       \"delete\": false     } 	}}";
		try {
			JsonNode ScreenConfiig = obj.readTree(req);
			System.out.println("ScreenConfiig "+ScreenConfiig);
		 JsonNode screenList = ScreenConfiig.get("pospScreenConfig");
		  Iterator<String> fieldNames = ScreenConfiig.get("pospScreenConfig").fieldNames();
		 while(fieldNames.hasNext()){
			 String Key = fieldNames.next();
			 System.out.println(Key );
			 System.out.println(Key +" :"+screenList.get(Key));
		 }
		 
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	} */
	
	
}

