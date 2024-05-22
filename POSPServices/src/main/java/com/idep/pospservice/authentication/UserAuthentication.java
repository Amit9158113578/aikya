package com.idep.pospservice.authentication;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.MD5Encryption;
import com.idep.pospservice.util.POSPServiceConstant;

public class UserAuthentication  implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UserAuthentication.class.getName());
	//CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData"); 
	//CBService PolicyTransaction = CBInstanceProvider.getPolicyTransInstance();
	JsonNode errorNode;
	UserProfileLoader upl = new UserProfileLoader();
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("RequestNode Of POSP User Auth:"+requestNode);
			
			if(requestNode.findValue("requestSource").textValue().equals("mobile") || requestNode.findValue("requestSource").textValue().equals("agentPortal")){
				exchange.setProperty("PospRegReq", requestNode);
				if(requestNode.has("mobileNumber")){
					String userId = POSPServiceConstant.POSP_USER_PROFILE+requestNode.findValue("mobileNumber").textValue();
					log.info("POSP User Doc Id:"+userId);
					JsonDocument userData = pospData.getDocBYId(userId);
					
					if(userData == null)
					{
						JsonNode regUser= upl.RegisterNewUser(requestNode);
						((ObjectNode)regUser).put("otp", true);
						exchange.getIn().setHeader("otpValidation", "YES");
						((ObjectNode)regUser).put("registrationMsg","");
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,regUser);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					}
					else
					{  
						JsonNode jsonNode = this.objectMapper.readTree(userData.content().toString());
						ObjectNode responseNode = this.objectMapper.createObjectNode();
						if(jsonNode.has("personalDetails")){
							if(jsonNode.get("personalDetails").get("firstName")!= null){
								responseNode.put("firstName",jsonNode.get("personalDetails").get("firstName"));
							}
						}						
						if(jsonNode.has("isProfileVerified")){
							responseNode.put("isVerified",jsonNode.get("isProfileVerified"));	
							if(jsonNode.get("isProfileVerified").asText().equals("true")){
								responseNode.put("otp",false);	
								exchange.getIn().setHeader("otpValidation", "NO");
								responseNode.put("registrationMsg","Your Profile Verfied please Login...");	
							}else{
								exchange.getIn().setHeader("otpValidation", "NO");
								responseNode.put("otp",false);	
								responseNode.put("registrationMsg","user details alredy Present  please Login...");	
							}
						}else{
							responseNode.put("isVerified","");	
						}
						if(jsonNode.has("stage")){
							responseNode.put("stage",jsonNode.get("stage"));	
						}else{
							responseNode.put("stage","");
						}
						if(jsonNode.has("status")){
							responseNode.put("status",jsonNode.get("status"));	
						}else{
							responseNode.put("status","pending");	
						}
						if(jsonNode.has("isTraningCompleted")){
							responseNode.put("isTraningCompleted",jsonNode.get("isTraningCompleted"));	
						}else{
							responseNode.put("isTraningCompleted","");	
						}
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.USER_EXIST_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.USER_EXIST_MESSAGES).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,responseNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					}

				}
			}else{
				log.error("Unable to process POSP registratioon Process : requestSource not found");
				throw new ExecutionTerminator();
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

