package com.idep.pospservice.authentication;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class GetUserTokenProcessor implements Processor{

	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	Logger log = Logger.getLogger(GetUserTokenProcessor.class.getName()); 
	JsonNode errorNode;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	static JsonNode queryConfig;
	static {
		try {
			queryConfig = objectMapper.readTree(PospData.getDocBYId("POSPQueryServerConfig").content().toString());
		} catch (Exception e) {
			Logger.getLogger(GetUserTokenProcessor.class.getName()).error("Unabele to cache POSPQueryServerConfig : ",e);
		}
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		try{

			JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
			if (requestNode.has("agentId")){
				String query = queryConfig.get("getAgentProfile").asText();
				JsonArray paramobj = JsonArray.create();
				paramobj.add(requestNode.get("agentId").asText());
				log.info("Query Agent data : "+query);
				log.info("Query Agent data param Obj : "+paramobj);
				ArrayNode AgentProfileArray= (ArrayNode) objectMapper.readTree(PospData.executeConfigParamArrQuery(query,paramobj).toString());
				
				if(AgentProfileArray.size()>0){
					JsonNode AgentProfile = AgentProfileArray.get(0);
					log.info("Query Agent Config Found : "+AgentProfile);
					query = queryConfig.get("findPospUserToken").asText();
					paramobj = JsonArray.create();
					paramobj.add(requestNode.get("agentId").asText());
					paramobj.add(AgentProfile.get("mobileNumber").asText());
					JsonNode findPospUserToken = objectMapper.readTree(PospData.executeConfigParamArrQuery(query,paramobj).toString());
					log.info("genetated Query userToken : "+query);
					log.info("Query Agent AgentProfile Found : "+findPospUserToken);
					OTPValidator oTPValidator = new OTPValidator();
					String Authorization ="";
					if(findPospUserToken.size()<=0){
						Authorization = oTPValidator.generateUserToken(AgentProfile.get("mobileNumber").asText(), requestNode.get("agentId").asText());
						log.info("Authorization created : "+Authorization);
					}else{
						ArrayNode userTokenArray = (ArrayNode)findPospUserToken;
						String createdDate =sdf.format(sdf.parse(userTokenArray.get(0).get("creationDate").asText())) ;
						String today =sdf.format(new Date()) ;
						log.info("Created Date : "+createdDate);
						log.info("todays Date : "+today);
						if(createdDate.equals(today)){
							Authorization = userTokenArray.get(0).get("Authorization").asText();
							log.info("Authorization found : "+Authorization);
						}else{
							Authorization = oTPValidator.generateUserToken(AgentProfile.get("mobileNumber").asText(),requestNode.get("agentId").asText());
							log.info("Authorization created : "+Authorization);
						}

					}
					ObjectNode AgentResNode = objectMapper.createObjectNode();
					AgentResNode.put("agentId", requestNode.get("agentId").asText());
					AgentResNode.put("Authorization",Authorization);

					if(AgentProfile.has("isProfileVerified")){
						AgentResNode.put("isProfileVerified", AgentProfile.get("isProfileVerified").asText());
					}else if(AgentProfile.has("isVerified")){
						AgentResNode.put("isProfileVerified", AgentProfile.get("isVerified").asText());	
					}
					if(AgentProfile.has("isTraningCompleted")){
						AgentResNode.put("isTraningCompleted", AgentProfile.get("isTraningCompleted").asText());
					}
					if(AgentProfile.has("comment")){
						AgentResNode.put("comment", AgentProfile.get("comment").asText());
					}else{
						AgentResNode.put("comment", "");	
					}
					if(AgentProfile.has("email")){
						AgentResNode.put("emailId", AgentProfile.get("email").asText());
					}else{
						AgentResNode.put("emailId", "");	
					}
					if(AgentProfile.has("firstName")){
						AgentResNode.put("firstName", AgentProfile.get("firstName").asText());
					}else{
						AgentResNode.put("firstName", "");	
					}
					if(AgentProfile.has("lastName")){
						AgentResNode.put("lastName", AgentProfile.get("lastName").asText());
					}else{
						AgentResNode.put("lastName", "");	
					}
					if(AgentProfile.has("organisationName")){
						AgentResNode.put("organisationName", AgentProfile.get("organisationName").asText());
					}else{
						AgentResNode.put("organisationName", "");	
					}
					if(AgentProfile.has("userRole")){
						AgentResNode.put("userRole", AgentProfile.get("userRole").asText());
					}else{
						AgentResNode.put("userRole", "");	
					}
					if(AgentProfile.has("mobileNumber")){
						AgentResNode.put("mobileNumber", AgentProfile.get("mobileNumber").asText());
					}else{
						AgentResNode.put("mobileNumber", "");	
					}
					ObjectNode resNode = objectMapper.createObjectNode();
					resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					resNode.put(POSPServiceConstant.RES_DATA,AgentResNode);
					exchange.getIn().setBody(resNode);
				}else
				{
					log.error("User Profile not found : "+requestNode);
					ObjectNode resNode = objectMapper.createObjectNode();
					resNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_CODE).asInt());
					resNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.ERROR_CONFIG_MSG).asText());
					resNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(resNode);
				}
			}else{
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
			}
		}
		catch(Exception e){
			log.error("Unable to process : ",e);
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));

		}


	}
}
