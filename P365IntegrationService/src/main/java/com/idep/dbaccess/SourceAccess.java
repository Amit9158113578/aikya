package com.idep.dbaccess;
import java.io.IOException;

import com.idep.bean.Source;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class SourceAccess {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(SourceAccess.class.getName());
	static CBService service = null;
	static JsonNode serviceConfigNode = null;
	
	
	public JsonNode validateSource(String deviceId) {
		log.info("Inside validateSource()--SourceAccess ");
		JsonNode responseDataByDeviceId = null;
		service = CBInstanceProvider.getServerConfigInstance();
		Source sourceobj=null;
		try {
			serviceConfigNode = objectMapper.readTree(((JsonObject) service.getDocBYId("P365IntegrationList").content()).toString());
			responseDataByDeviceId = serviceConfigNode.get(deviceId);
			
			sourceobj=new Source();
			sourceobj.setDescription(responseDataByDeviceId.get("description").asText());
			sourceobj.setSource(responseDataByDeviceId.get("source").asText());
			sourceobj.setName(responseDataByDeviceId.get("name").asText());
			
		}catch (IOException e) {
           log.info("Exception in validateSource()-SourceAccess  ");  
			e.printStackTrace();
		}

		return responseDataByDeviceId;
	}
}
