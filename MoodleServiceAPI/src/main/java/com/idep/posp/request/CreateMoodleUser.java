package com.idep.posp.request;


import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.posp.connection.RestServiceClient;
import com.idep.posp.connection.SaveDBServiceResponse;

public class CreateMoodleUser{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CreateMoodleUser.class.getName());
	RestServiceClient rsc = new RestServiceClient();
	SaveDBServiceResponse db  =  new SaveDBServiceResponse();
	AssignUserRole aur = new AssignUserRole();
	//CreateMoodleUser cm = new CreateMoodleUser();
	/**
	 * Create user method request need below field
	 * {
"dataParam":{
"agentId":"NIPOS0001",
"firstName":"Abhas",
"lastName":"jain",
"email":"abhasjain@infintus.com",
"mobileNumber":"8956756709"
},
"documentType":"pospUserProfile"
}
	 * 
	 * **/
	
	
	public String CreateUser(String request){
		String resService =null;
		ObjectNode objNode = objectMapper.createObjectNode();
			try {
				JsonNode requestNode = objectMapper.readTree(request);
				((ObjectNode)requestNode).put("urlConfigDocId", "POSPExternalServiceUrl");
				((ObjectNode)requestNode).put("ConfigDocId", "CreateMoodleUser");
				((ObjectNode)requestNode).put("functionName", "CreateMoodleUser");
				
				((ObjectNode)requestNode).put("methodType","POST");
				JsonNode response = objectMapper.createObjectNode();
				String req= rsc.CallRestService(requestNode);
				JsonNode resNode = objectMapper.readTree(req);
				if(resNode.has("exception") || resNode.has("errorcode")){
					log.error("unable to create user in Moodle using service : "+resNode);
					return null;
				}else{
					ArrayNode moodleInfo = (ArrayNode)resNode;
					if(moodleInfo.get(0).has("id")){
						((ObjectNode)response).put("id",moodleInfo.get(0).get("id").asText());
						((ObjectNode)response).put("username",moodleInfo.get(0).get("username").asText());
						objNode.put("moodleDetails", response);	
						objNode.put("request",requestNode);	
						resService=db.SaveMoodleDetails(objNode.toString());
						if(resService.equalsIgnoreCase("success")){
							((ObjectNode)requestNode).put("moodleDetails", response);
							log.info("user Enrolment process Started : "+resNode);
							JsonNode courseRes = aur.assignUserRole(requestNode.toString());
							((ObjectNode)requestNode.get("moodleDetails")).put("courseDetails",courseRes);
							resService=db.SaveCourseDetails(requestNode.toString());
							log.info("user Enrolment process Completed");
						}
					}
				}
				return resService;
				
			}catch (Exception e) {
				log.error("unable to create moodle user : ",e);
			}
			return null;
	}
	
	public static void main(String[] args) {
	String req ="{ \"dataParam\":{ \"agentId\":\"NIPOS00034\", \"firstName\":\"pospuser\", \"lastName\":\"pospuser\", \"email\":\"pospuser@infintus.com\", \"mobileNumber\":\"9270087444\" },\"documentType\":\"POSPUserProfile\" }";
		CreateMoodleUser cm = new CreateMoodleUser();
	cm.CreateUser(req);
	}
}