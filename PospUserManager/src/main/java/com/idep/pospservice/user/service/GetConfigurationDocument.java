package com.idep.pospservice.user.service;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.ExecutionTerminator;
import com.idep.pospservice.util.POSPServiceConstant;

public class GetConfigurationDocument implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GetConfigurationDocument.class.getName());
	static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	JsonNode errorNode;
	static JsonDocument configDocumeent=null;
	static{
		
		configDocumeent =  pospData.getDocBYId("POSPQueryServerConfig");
		
	}
	@Override
	public void process(Exchange exchange) throws Exception {
			
			JsonNode requestNode = objectMapper.readTree( exchange.getIn().getBody().toString());
			List<Map<String, Object>> list  = null;
			String query ="";
			try{
				JsonNode queryDoc = null;	
				if(configDocumeent!=null){
					queryDoc =objectMapper.readTree(  configDocumeent.content().toString());
				}else{
					configDocumeent =  pospData.getDocBYId("POSPQueryServerConfig");
					queryDoc =objectMapper.readTree(  configDocumeent.content().toString());
				}
			log.info("RequestNode Of Doc: "+requestNode);
			if(requestNode.has("documentType") && queryDoc!=null){
				if(queryDoc.has(requestNode.get("documentType").asText())){
					list = pospData.executeQueryCouchDB(queryDoc.get(requestNode.get("documentType").asText()).get("Query").asText());
				}
			}else{
				log.error("Unable to get document , documentType not present in request : "+requestNode);
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,1002);
				objectNode.put(POSPServiceConstant.RES_MSG,"failure");
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
				throw new ExecutionTerminator();
			}
			this.log.info("query formed : " + query);
			String ListDoc = objectMapper.writeValueAsString(list);
			JsonNode DocDetails = objectMapper.readTree(ListDoc);

			this.log.info("list : " + DocDetails);
			if (list.isEmpty()) {
				log.error("Unable to get document using req  : "+requestNode);
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,1002);
				objectNode.put(POSPServiceConstant.RES_MSG,"failure");
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
				exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
				throw new ExecutionTerminator();
			}
			else
			{
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,1000);
				objectNode.put(POSPServiceConstant.RES_MSG ,"success");
				objectNode.put(POSPServiceConstant.RES_DATA,DocDetails);
				exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
			}
			
		}catch(Exception e){
			log.error("unable to get configuration document  : ",e);
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,1002);
			objectNode.put(POSPServiceConstant.RES_MSG,"failure");
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(objectNode));
			throw new ExecutionTerminator();
		}
	}
}
