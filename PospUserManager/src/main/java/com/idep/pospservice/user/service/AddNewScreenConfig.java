package com.idep.pospservice.user.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.Functions;
import com.idep.pospservice.util.POSPServiceConstant;

public class AddNewScreenConfig implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AddNewScreenConfig.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String request = exchange.getIn().getBody(String.class);
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			log.info("RequestNode for add new Screen Config  : "+requestNode);
			String docId = "PospScreenConfig";
			JsonDocument configDoc = PospData.getDocBYId(docId);
			if(configDoc!=null){
				JsonObject docObj = JsonObject.fromJson(request);
			JsonArray screenConfigArray = configDoc.content().getArray("pospScreenConfig");
			screenConfigArray.add(docObj.get("pospScreenConfig"));
			configDoc.content().put("pospScreenConfig", screenConfigArray);
			configDoc.content().put("updatedBy", requestNode.get("adminId").asText());
			configDoc.content().put("updatedDate",Functions.getDateAndTime());
			String doc_Status = PospData.replaceDocument(docId,configDoc.content());
				log.info("Document Updated : "+doc_Status);
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,objectMapper.readTree(configDoc.content().get("screenConfig").toString()));
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			}else{
				log.error("unable to load POSPScreenPermissionSetup document fromm DB ");	
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NO_RECORDS_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NORECORD_MESSAGES).asText());
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				throw new ExecutionTerminator();
			}
		}catch(Exception e){
			log.error("unable to add screen details in POSP : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}
}
