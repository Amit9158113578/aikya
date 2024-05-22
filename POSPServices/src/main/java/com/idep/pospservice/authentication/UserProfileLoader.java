package com.idep.pospservice.authentication;


import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

import org.apache.log4j.Logger;

public class UserProfileLoader
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UserProfileLoader.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices pospDataSync = new SyncGatewayPospDataServices();
	JsonNode errorNode;
	public JsonNode getProfileDetails(String mobile)
	{
		try
		{
			JsonNode userProfileNode = this.pospDataSync.getPospDataDocumentBySync(POSPServiceConstant.POSP_USER_PROFILE+mobile);
			log.info("POSP User Not Exist"); 
			if (userProfileNode == null){
				long posp_seq;
				synchronized(this)
				{
					posp_seq = this.pospData.updateDBSequence(POSPServiceConstant.POSP_SEQ_AG);
				}
				String userId = POSPServiceConstant.POSP_USER_PROFILE+mobile;
				String agentId = POSPServiceConstant.POSPAG_ID+posp_seq;
				ObjectNode userProfileData = this.objectMapper.createObjectNode();
				userProfileData.put("agentId",agentId);
				userProfileData.put("mobileNumber",mobile);
				String docId = POSPServiceConstant.USER_PROFILE_DEFAULT_CONFIG;
				JsonDocument userdoc = pospData.getDocBYId(docId);
				if(userdoc==null){
					 userdoc = pospData.getDocBYId(docId);	
				}
				log.info("POSP User Default:"+userdoc.content());
				ObjectNode userNode = (ObjectNode) objectMapper.readTree(userdoc.content().toString());
				userProfileData.putAll(userNode);
				log.info("POSP User Profile:"+userProfileData);
				String doc_status = this.pospDataSync.createPospDataDocumentBySync(userId,JsonObject.fromJson(objectMapper.writeValueAsString(userProfileData)));
				log.info("POSP Doc Status:"+doc_status+" "+"POSP UserId :"+userId+" "+"Doc Content:"+userProfileData);
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
					ObjectNode userProfile = this.objectMapper.createObjectNode();
					userProfile.put("agentId",agentId);
					return userProfile;

				}else
				{
					 this.log.error("profile not created: " + doc_status);
				      return null;
				}
				
			}else{
				log.info("POSP User Exist");  
				ObjectNode userProfileData = this.objectMapper.createObjectNode();
				userProfileData.put("agentId", userProfileNode.get("agentId"));
				return userProfileData;
			}  

		}
		catch (Exception e)
		{
			this.log.error("Exception while fetching user profile details : ", e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			return objectNode;
		}

	}
	
	public JsonNode RegisterNewUser(JsonNode request){
		
		try{
			
			String mobile=request.get("mobileNumber").asText();
					
			
			JsonNode userProfileNode = pospDataSync.getPospDataDocumentBySync(POSPServiceConstant.POSP_USER_PROFILE+mobile);
			log.info("POSP User Not Exist"); 
			if (userProfileNode == null){
				long posp_seq;
				synchronized(this)
				{
					posp_seq = this.pospData.updateDBSequence(POSPServiceConstant.POSP_SEQ_AG);
				}
				String userId = POSPServiceConstant.POSP_USER_PROFILE+mobile;
				String agentId = POSPServiceConstant.POSPAG_ID+posp_seq;
				ObjectNode userProfileData = this.objectMapper.createObjectNode();
				userProfileData.put("agentId",agentId);
				userProfileData.put("mobileNumber",mobile);
				
				if(request.has("firstName")){
					userProfileData.put("firstName",request.get("firstName").asText());
				}else{
					userProfileData.put("firstName","");
				}
				if(request.has("lastName")){
					userProfileData.put("lastName",request.get("lastName").asText());
				}else{
					userProfileData.put("lastName","");
				}
				if(request.has("emailId")){
					userProfileData.put("emailId",request.get("emailId").asText());
				}else{
					userProfileData.put("emailId","");
				}
				
				String docId = POSPServiceConstant.USER_PROFILE_DEFAULT_CONFIG;
				JsonDocument userdoc = pospData.getDocBYId(docId);
				if(userdoc==null){
					 userdoc = pospData.getDocBYId(docId);	
				}
				log.info("POSP User Default:"+userdoc.content());
				ObjectNode userNode = (ObjectNode) objectMapper.readTree(userdoc.content().toString());
				userProfileData.putAll(userNode);
				log.info("POSP User Profile:"+userProfileData);
				String doc_status = pospDataSync.createPospDataDocumentBySync(userId,JsonObject.fromJson(objectMapper.writeValueAsString(userProfileData)));
				log.info("POSP Doc Status:"+doc_status+" "+"POSP UserId :"+userId+" "+"Doc Content:"+userProfileData);
				if(doc_status.equalsIgnoreCase(POSPServiceConstant.DOC_CREATED)){
					ObjectNode userProfile = this.objectMapper.createObjectNode();
					userProfile.put("agentId",agentId);
					return userProfile;

				}else
				{
					 this.log.error("profile not created: " + doc_status);
				      return null;
				}
				}else{
					ObjectNode userProfile = this.objectMapper.createObjectNode();
					userProfile.put("agentId",userProfileNode.get("agentId").asText());
					return userProfile;
				}	
			}catch(Exception e){
				log.error("unable to register user in POSP : ",e);
			}
			return null;
	}
	
}
