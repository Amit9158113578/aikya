package com.idep.pospservice.request;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.authentication.OTPValidator;
import com.idep.pospservice.authentication.UserAuthentication;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.MD5Encryption;
import com.idep.pospservice.util.POSPServiceConstant;

public class AdminLogin implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AdminLogin.class.getName());
	static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	static JsonNode queryConfig;
	OTPValidator otpVal  = new OTPValidator();
	JsonNode errorNode;
	static {
		try {
			queryConfig = objectMapper.readTree(pospData.getDocBYId("POSPQueryServerConfig").content().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			if(requestNode.findValue("requestSource").textValue().equals("web")){
				if(requestNode.findValue("userName").textValue().equals("Admin")){
					JsonDocument adminData = pospData.getDocBYId("POSPAdminProfile");{
						JsonObject adminJson = adminData.content();
						if(adminJson.getString("userName").equals(requestNode.findValue("userName").textValue()) && adminJson.getString("password").equals(MD5Encryption.MD5(requestNode.findValue("password").textValue())))
						{
							JsonNode jsonNode = objectMapper.readTree(adminJson.removeKey("password").toString());
							String Authorization="";
							if(exchange.getIn().getHeader("Authorization")!=null){
								Authorization = exchange.getIn().getHeader("Authorization").toString();
								log.info("Authorization found in headers : "+Authorization);
							}else{
								log.info("Authorization not found in headeres");
							}
								if(Authorization.length()==0){
								/**
								 * if Authorization not found in exchange headers then generating new and storing in DB and sending to UI.
								 * **/
								String token = otpVal.generateUserToken(jsonNode.findValue("mobileNumber").asText(),jsonNode.findValue("adminId").asText());
								((ObjectNode)jsonNode).put("Authorization",token);	
							}
							ObjectNode objectNode = objectMapper.createObjectNode();
							objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
							objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
							objectNode.put(POSPServiceConstant.RES_DATA,jsonNode);
							exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
						}
						else
						{
							ObjectNode objectNode = objectMapper.createObjectNode();
							objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.LOGIN_FAILED_CODE).asInt());
							objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.LOGIN_FAILED_MESSAGES).asText());
							exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
						}
					}
				}else{
					String adminId = POSPServiceConstant.POSP_ADMIN_PROFILE+requestNode.findValue("userName").asText();
					log.info("DocumetId fetching for Adminl ogin : "+adminId);
					JsonDocument adminData = pospData.getDocBYId(adminId);
					if(adminData==null){
						log.error("unable to load DB document : "+adminId);
						ObjectNode objectNode = objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
						throw new ExecutionTerminator();
					}
					JsonObject adminJson = adminData.content();
					if(adminJson.getString("mobileNumber").equals(requestNode.findValue("userName").textValue()) && adminJson.getString("password").equals(MD5Encryption.MD5(requestNode.findValue("password").textValue())))
					{
						JsonNode jsonNode = objectMapper.readTree(adminJson.removeKey("password").toString());
						if((jsonNode.get("isVerified").asText().equalsIgnoreCase("No") || jsonNode.get("isVerified").asText().equalsIgnoreCase("N")) && (jsonNode.get("isActive").asText().equalsIgnoreCase("No") || jsonNode.get("isActive").asText().equalsIgnoreCase("N"))){
							errorNode = objectMapper.createObjectNode();
							ObjectNode objectNode = objectMapper.createObjectNode();
							objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
							objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
							objectNode.put(POSPServiceConstant.RES_DATA,"User not verified by Admin");
							exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
							throw new ExecutionTerminator();
						}
						
						log.info("jsonNode for Login : "+jsonNode);
						if(jsonNode.has("userRole")){
							if(jsonNode.get("userRole").asText().length() > 0 ){
								JsonArray paramobj = JsonArray.create();
								paramobj.add(jsonNode.get("userRole").asText());
								JsonNode groupConfig = objectMapper.readTree(pospData.executeConfigParamArrQuery(queryConfig.get("GetRoleDoc").get("query").asText(),paramobj).toString());
								if(groupConfig!=null){
									((ObjectNode)jsonNode).put("groupConfig",groupConfig);
									log.info("Group Configuration added into response : "+groupConfig);
								}
							}
						}
						String Authorization="";
						if(exchange.getIn().getHeader("Authorization")!=null){
							Authorization = exchange.getIn().getHeader("Authorization").toString();
							log.info("Authorization found in headers : "+Authorization);
						}else{
							log.info("Authorization not found in headeres");
						}
							if(Authorization.length()==0){
							/**
							 * if Authorization not found in exchange headers then generating new and storing in DB and sending to UI.
							 * **/
							String token = otpVal.generateUserToken(jsonNode.findValue("mobileNumber").asText(),jsonNode.findValue("adminId").asText());
							((ObjectNode)jsonNode).put("Authorization",token);	
						}
						ObjectNode objectNode = objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,jsonNode);
						exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
					}
					else
					{
						ObjectNode objectNode = objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.LOGIN_FAILED_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.LOGIN_FAILED_MESSAGES).asText());
						exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
					}
				}
			}	else{
				throw new ExecutionTerminator();
			}
			
		}catch(Exception e){
		log.error("unable to prcess request for adminLogin : POSP",e);	
		throw new ExecutionTerminator();
		}

	}

}
