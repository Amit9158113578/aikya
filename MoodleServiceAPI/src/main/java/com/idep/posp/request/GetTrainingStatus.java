package com.idep.posp.request;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.posp.connection.RestServiceClient;
import com.idep.posp.connection.SaveDBServiceResponse;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;

public class GetTrainingStatus {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GetTrainingStatus.class.getName());
	RestServiceClient rsc = new RestServiceClient();
	SaveDBServiceResponse db  =  new SaveDBServiceResponse();
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	SyncGatewayPospDataServices syncPospData = new SyncGatewayPospDataServices();
	public String GetMoodleTranningStatus(String request){
			try{
				JsonNode requestNode = objectMapper.readTree(request);
				((ObjectNode)requestNode).put("urlConfigDocId", "POSPExternalServiceUrl");
				((ObjectNode)requestNode).put("ConfigDocId", "getTrainingStatus");
				((ObjectNode)requestNode).put("functionName", "MoodleTrainingStatus");
				JsonDocument userprofileDoc = null;
				String userProfileDocId ="";
				JsonNode userProfile =null;
				if(requestNode.has("mobileNumber")){
					userProfileDocId = "POSPUserProfile-"+requestNode.get("mobileNumber").asText();
					userprofileDoc = pospData.getDocBYId(userProfileDocId);
				}else{
					log.error("Unable to fetchUser Profile : "+request);
					return null;
				}
				if(userprofileDoc!=null){
					
					userProfile = objectMapper.readTree(userprofileDoc.content().toString());
				}else{
					log.error("Unable to fetchUser Profile : "+request);
					return null;
				}
				log.debug("Fetched User profile get training Status : "+userProfile);
				if(!requestNode.has("moodleUserId")){
						log.info("moodleUserId not found in request");	
						/*
							 * getting moodle userId from UserProfile 
							 * **/
							if(userProfile.has("moodleDetails")){
								if(userProfile.get("moodleDetails").has("id")){
									((ObjectNode)requestNode).put("moodleUserId",userProfile.get("moodleDetails").get("id").asText());
								}
							}else{
								/*
								 * if moodle userId not present in UserProfile then fetching from  Moodle Service and storing into DB 
								 * **/
								log.info("moodleUserId not found in request fetching from Moodle Service");	
								ObjectNode getUserData = objectMapper.createObjectNode();
								getUserData.put("urlConfigDocId", "POSPExternalServiceUrl");
								getUserData.put("ConfigDocId", "GetMoodleUserDetails");
								getUserData.put("functionName", "getUserDetails");
								getUserData.put("agentId", userProfile.get("agentId").asText().toLowerCase());
								String req= rsc.CallRestService(getUserData);
								JsonNode resNode = objectMapper.readTree(req);
								if(resNode.has("exception") || resNode.has("errorcode")){
									log.error("unable to create user in Moodle using service : "+resNode);
									return null;
								}else{
									ArrayNode moodleInfo = (ArrayNode)resNode;
									if(moodleInfo.get(0).has("id")){
										((ObjectNode)requestNode).put("moodleUserId",moodleInfo.get(0).get("id").asText());
									}
									((ObjectNode)userProfile).put("moodleDetails",moodleInfo.get(0));
									String doc_Status = pospData.replaceDocument(userProfileDocId,JsonObject.fromJson(userProfile.toString()));
									log.info("User Profile Updated for moodle details stored : "+userProfileDocId +" : "+doc_Status);
								}
							}
				      }
				((ObjectNode)requestNode).put("methodType","POST");
				//JsonNode response = objectMapper.createObjectNode();
				String req= rsc.CallRestService(requestNode);
				JsonNode resNode = objectMapper.readTree(req);
				if(resNode.has("exception") || resNode.has("errorcode")){
					log.error("unable to create user in Moodle using service : "+resNode);
					return null;
				}else{
					ArrayNode tranRes = (ArrayNode)resNode.get("statuses");
					log.info("User Course Status Details : "+tranRes);
					for(JsonNode trans : tranRes){
						if(trans.has("state")){
							log.info("Validating User course State : "+trans);
							if(trans.get("state").asInt() == 1 || trans.get("state").asInt() == 2){
								((ObjectNode)userProfile).put("isTraningCompleted", "true");	
							}else{
								((ObjectNode)userProfile).put("isTraningCompleted", "false");
								log.error("User Tranning not completed "+trans);
								break;
							}
						}
					}
					((ObjectNode)userProfile.get("moodleDetails")).put("tranningStatus",resNode);
					String doc_Status = pospData.replaceDocument(userProfileDocId,JsonObject.fromJson(userProfile.toString()));
					log.info("UserProfile Updated for Moodle Tranning Status : "+doc_Status);
					return "success";
				}
			}catch(Exception e){
				log.error("Unable to get Moodle Training Status : ",e);
			}
		return null;
	}
	
	
}
