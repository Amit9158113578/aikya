package com.idep.pospservice.user.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.MD5Encryption;
import com.idep.pospservice.util.POSPServiceConstant;

public class AdminProfileReqProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AdminProfileReqProcessor.class.getName());
	JsonNode errorNode;
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("RequestNode Of POSP Admin:"+requestNode);
			exchange.getIn().setHeader(POSPServiceConstant.DOCUMENT_ID,"AdminRequest");
			((ObjectNode)requestNode).put(POSPServiceConstant.REQUEST_TYPE,"POSPRequest");
			
			
			
			if(requestNode.has("updateInfo")){
				if(requestNode.get("updateInfo").asText().equalsIgnoreCase("Yes")){
					
					
					JsonDocument adminProfile = pospData.getDocBYId(POSPServiceConstant.POSP_ADMIN_PROFILE+requestNode.get("mobileNumber").asText());
					if(adminProfile!=null){
						
						JsonNode adminInfo = objectMapper.readTree(adminProfile.content().toString());
						if(requestNode.has("password")){
							((ObjectNode)requestNode).remove("password");
						}
						if(requestNode.has("mobileNumber")){
							((ObjectNode)requestNode).remove("mobileNumber");
						}
						
						String password = adminInfo.get("password").asText(); 
						String adminId = adminInfo.get("adminId").asText();
						((ObjectNode)requestNode).put("password",password);
						((ObjectNode)requestNode).put("adminId",adminId);
						((ObjectNode)requestNode).put("mobileNumber",adminInfo.get("mobileNumber").asText());
						((ObjectNode)requestNode).put("profileCreationDate",adminInfo.get("profileCreationDate").asText());
					}else{
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						errorNode = objectMapper.createObjectNode();
						((ObjectNode)errorNode).put("message", "unable to find user Profile : "+requestNode.get("mobileNumber").asText());
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
						throw new ExecutionTerminator();
					}
				}
			}
			
			if(requestNode.has("resetPassword")){
				exchange.getIn().setHeader("resetPassword", "Yes");
			}else{
				exchange.getIn().setHeader("resetPassword", "No");
			}
			
			
			exchange.getIn().setBody(requestNode);
		}
		catch(Exception e)
		{
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}
}
