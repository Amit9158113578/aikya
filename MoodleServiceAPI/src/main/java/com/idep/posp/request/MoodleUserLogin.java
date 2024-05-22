package com.idep.posp.request;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.posp.connection.RestServiceClient;

public class MoodleUserLogin {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(MoodleUserLogin.class.getName());
	RestServiceClient rsc = new RestServiceClient();
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
			
	public String UserLogin(String request){
		
		try{
			
			JsonNode reqNode = objectMapper.readTree(request);

			String res = "";
			if(reqNode.has("agentId")){
				
				((ObjectNode)reqNode).put("urlConfigDocId", "POSPExternalServiceUrl");
				((ObjectNode)reqNode).put("ConfigDocId", "moodleUserLogin");
				((ObjectNode)reqNode).put("functionName", "moodleUserLogin");
				((ObjectNode)reqNode).put("methodType","POST");
				 res = rsc.CallRestService(reqNode);
				log.info("Login Url Received from Moodle : "+res);
			}
			
			
			return res;
		}catch(Exception e){
			log.error("Unabel to login in Moodle for : "+request);
			log.error("Unabel to login in Moodle for : ",e);
		}
		
		
		
		
		
		
		
		return null;
	}
	
	
	
}
