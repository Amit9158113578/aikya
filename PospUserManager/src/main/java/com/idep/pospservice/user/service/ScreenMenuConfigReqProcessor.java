package com.idep.pospservice.user.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class ScreenMenuConfigReqProcessor  implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ScreenMenuConfigReqProcessor.class.getName());
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			log.info("RequestNode Of POSP Super Admin:"+requestNode);
					
			if(requestNode.has("documentType")){
				
				if(requestNode.get("documentType").asText().equalsIgnoreCase("pospScreenConfig")){
					JsonDocument configDoc = pospData.getDocBYId("PospScreenConfig");
					if(configDoc!=null){
					JsonNode configDocNode = this.objectMapper.readTree(configDoc.content().toString());
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,configDocNode.get("pospScreenConfig"));
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
					}else{
						log.error("Unable to send screen Config in POSP : ");
					}
				}
				if(requestNode.get("documentType").asText().equalsIgnoreCase("pospMenuConfig")){
					JsonDocument configDoc = pospData.getDocBYId("PospMenuConfig");
					if(configDoc!=null){
						JsonNode configDocNode = this.objectMapper.readTree(configDoc.content().toString());
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,configDocNode.get("pospMenuConfig"));
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));	
					}else{
						log.error("Unable to send menu Config in POSP : ");
					}
				}
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


