package com.idep.posp.connection;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.posp.request.CreateMoodleUser;
import com.idep.sync.service.impl.SyncGatewayPospDataServices;
import com.idep.sync.service.impl.SyncGatewayServices;

public class SaveDBServiceResponse{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CreateMoodleUser.class.getName());
	//SyncGatewayServices syncService = new SyncGatewayServices();
	SyncGatewayPospDataServices syncPospData = new SyncGatewayPospDataServices();
	public String SaveMoodleDetails(String res){
		
		try{
			
			JsonNode request = objectMapper.readTree(res);
			String docId=null;
			JsonNode userProfile=null;
			if(request.findValue("documentType")!=null && request.findValue("mobileNumber")!=null){
				docId= request.findValue("documentType").asText()+"-"+request.findValue("mobileNumber").asText();
				log.info("POSP User document Id Fetching usng Sync : "+docId);
				userProfile= syncPospData.getPospDataDocumentBySync(docId);
				if(userProfile!=null && !userProfile.has("error")){
				((ObjectNode)userProfile).put("moodleDetails", request.get("moodleDetails"));
				 String docStatus = syncPospData.replacePospDataDocumentBySync(docId,JsonObject.fromJson(objectMapper.writeValueAsString(userProfile)));
					log.info(docId+" updated , docStatus : "+docStatus);
					return "success";
				}else{
					((ObjectNode)userProfile).put("moodleDetails", request.get("moodleDetails"));
					((ObjectNode)userProfile).put("mobileNumber", request.findValue("mobileNumber").asText());
					((ObjectNode)userProfile).put("agentId", request.findValue("agentId").asText());
					((ObjectNode)userProfile).put("documentType", "POSPUserProfile");
					createProfile(docId,userProfile);
					return "success";
				}
			}
		}catch(Exception e){
			log.error("unale to stor service response : ",e);
		}
			return null;
	}

	
	public String createProfile(String docId,JsonNode req)
	  {
	    try
	    {
	      String docStatus = syncPospData.createPospDataDocumentBySync(docId, 
	        JsonObject.fromJson(objectMapper.writeValueAsString(req)));
	      log.info("POSPUserProfile Profile document: " + docId + " status : " + docStatus);
	      return docStatus;
	    }
	    catch (Exception e)
	    {
	      log.error("failed to create user profile : ", e);
	    }
	    return null;
	  }
public String SaveCourseDetails(String res){
		
		try{
			
			JsonNode request = objectMapper.readTree(res);
			String docId=null;
			JsonNode userProfile=null;
			if(request.findValues("documentType")!=null && request.findValues("mobileNumber")!=null){
				docId= request.findValue("documentType").asText()+"-"+request.findValue("mobileNumber").asText();
				userProfile= syncPospData.getPospDataDocumentBySync(docId);
				if(request.has("moodleDetails")){
				((ObjectNode)userProfile).put("moodleDetails", request.get("moodleDetails"));
				if(userProfile!=null && !userProfile.has("error")){
					 String docStatus = syncPospData.replacePospDataDocumentBySync(docId,JsonObject.fromJson(objectMapper.writeValueAsString(userProfile)));
						log.info(docId+" updated , docStatus : "+docStatus);
						return "success";
					}else{
						return null;
					}
				}
				return "success";
			}
		}catch(Exception e){
			log.error("unale to stor service response : ",e);
		}
			return null;
	}

}
