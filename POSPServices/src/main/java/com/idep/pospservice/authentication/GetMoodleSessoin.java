package com.idep.pospservice.authentication;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
/*import com.idep.posp.connection.RestServiceClient;
import com.idep.posp.request.MoodleUserLogin;*/
import com.idep.pospservice.util.POSPServiceConstant;

public class GetMoodleSessoin implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GetMoodleSessoin.class.getName());
	//MoodleUserLogin mul = new MoodleUserLogin();
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	static CBService serverConfig = CBInstanceProvider.getBucketInstance("ServerConfig");
	JsonNode errorNode;
	static JsonNode resCodeDoc = null;
	static{
		try {
			resCodeDoc = objectMapper.readTree(serverConfig.getDocBYId("ResponseMessages").content().toString());
		} catch (IOException e) {
			Logger.getLogger(GetMoodleSessoin.class).error("Unabel to cach document : ResponseMessages GetMoodleSessoin ",e);
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode requestNode =  objectMapper.readTree(exchange.getIn().getBody(String.class));
			ObjectNode moodleResponse = objectMapper.createObjectNode();
			if(requestNode.has("agentId")){/*
				
				String res = mul.UserLogin(objectMapper.writeValueAsString(requestNode));
				log.info("Moodle Login Url : "+res);
				
				JsonNode moodleRes = objectMapper.readTree(res);
				
				if(moodleRes.has("loginurl")){
					String loginUrl = moodleRes.get("loginurl").asText();
					
					
					String[] moodleResArray = loginUrl.split("=");
					log.info("User Login : "+moodleResArray);
					if(moodleResArray.length>0){
						log.info("Login User Key : "+moodleResArray[1].toString());
						moodleResponse.put("moodleSessionKey", moodleResArray[1].toString());
						moodleResponse.put("moodleurl",loginUrl );
					}
					
					ObjectNode objectNode = objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,resCodeDoc.get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,resCodeDoc.get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,moodleResponse);
					exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
					
					
				}else{
					errorNode = objectMapper.createObjectNode();
					if(moodleRes.has("debuginfo")){
						((ObjectNode)errorNode).put("message", moodleRes.get("debuginfo").asText());
					}else{
						((ObjectNode)errorNode).put("message", moodleRes.get("message").asText());
					}
					
					ObjectNode objectNode = objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.ERROR_CODE_VAL);
					objectNode.put(POSPServiceConstant.RES_MSG,POSPServiceConstant.ERROR_MSG_VAL);
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
				}
			*/}else{
				ObjectNode objectNode = objectMapper.createObjectNode();
				errorNode = objectMapper.createObjectNode();
				((ObjectNode)errorNode).put("message", "failed to process please try again");
				objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.ERROR_CODE_VAL);
				objectNode.put(POSPServiceConstant.RES_MSG,POSPServiceConstant.ERROR_MSG_VAL);
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
			}
		}catch(Exception e){
			log.error("unable to get Moodle Session for user : ",e);
		}

	}

}
