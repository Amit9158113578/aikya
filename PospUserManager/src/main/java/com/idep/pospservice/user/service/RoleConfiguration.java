package com.idep.pospservice.user.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.Functions;
import com.idep.pospservice.util.POSPServiceConstant;

public class RoleConfiguration implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(RoleConfiguration.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("RequestNode Role: "+requestNode);	
			ObjectNode reqNode = (ObjectNode)requestNode;
			JsonObject req = JsonObject.fromJson(requestNode.toString());
			String  adminId="";
			String roleDocId ="";
			if(requestNode.has("roleId")){
				roleDocId = requestNode.get("roleId").asText();
			}else{
					long posp_seq=0;
				synchronized(this)
				{
					posp_seq = this.pospData.updateDBSequence(POSPServiceConstant.POSP_SEQ_ROLE);
				}
				roleDocId = POSPServiceConstant.POSPAG_ROLEID+posp_seq;
			}
			 //POSPServiceConstant.ROLEID+requestNode.findValue("roleName").textValue();
	
			JsonDocument roleDoc = pospData.getDocBYId(roleDocId);
			if(roleDoc==null){
				
				//req.removeKey("premissionGroup");
				req.put("documentType", "UserRole");
				req.put("createdBy",requestNode.get("adminId").asText());
				req.put("updatedDate",Functions.getDateAndTime());
				req.put("id", roleDocId);
				String doc_status = pospData.createDocument(roleDocId, req);
				log.info("Role Response :"+doc_status+","+roleDocId);
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
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
				
			}else{
				
				ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(roleDoc.content().toString());
				 adminId = requestNode.get("adminId").asText();
				jsonNode.put("updatedBy",adminId);
				jsonNode.put("updatedDate",Functions.getDateAndTime());
				req.put("documentType", "UserRole");
				if(requestNode.has("action")){
					if(requestNode.get("action").asText().equalsIgnoreCase("DISABLE")){
						jsonNode.put("isActive","No");
					}
					if(requestNode.get("action").asText().equalsIgnoreCase("ENABLE")){
						jsonNode.put("isActive","Yes");
					}
					((ObjectNode)requestNode).remove("action");
				}
				((ObjectNode)requestNode).remove("adminId");
				
				((ObjectNode)jsonNode).putAll(reqNode);
				String doc_status = pospData.replaceDocument(roleDocId, JsonObject.fromJson(jsonNode.toString()));
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
			
			/*
			 * Adding role into Multiple Group 
			 * **/
			if(requestNode.findValue("permissionGroup")!=null){
				
				ArrayNode groupList  = (ArrayNode)requestNode.get("permissionGroup");
				for(int i=0;i<groupList.size();i++){
					log.info("Group Id Found : "+groupList.get(i).asText());
					String group_doc = groupList.get(i).asText();
					log.info("Group Id Found : "+group_doc);
					JsonDocument groupDoc=pospData.getDocBYId(group_doc);
					if(groupDoc!=null){
						ArrayNode roleConfig=null;
						JsonNode roleConfigDoc =objectMapper.readTree(groupDoc.content().toString());
						if(roleConfigDoc.has("roleConfig")){
							roleConfig = (ArrayNode)objectMapper.readTree(roleConfigDoc.get("roleConfig").toString());
						}else{
							roleConfig = objectMapper.createArrayNode();
						}
						
						roleConfig.add(requestNode.findValue("roleName").asText());
						
						((ObjectNode)roleConfigDoc).put("roleConfig", roleConfig);
						((ObjectNode)roleConfigDoc).put("updatedBy", adminId);
						((ObjectNode)roleConfigDoc).put("updatedDate", Functions.getDateAndTime());
						JsonObject roleDocument = JsonObject.fromJson(roleConfigDoc.toString());
						String groupdoc_status=pospData.replaceDocument(group_doc,roleDocument);
						log.info("Group Document Updated : Status :"+groupdoc_status+","+group_doc);
					}else{
						log.error("Unable to add role into group document : "+group_doc);
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
						throw new ExecutionTerminator();
					}
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
	
	
	
}
