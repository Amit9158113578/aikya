package com.idep.customer.reqprocessor;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.customer.util.CustomerConstants;

public class DataProvider {
	static Logger log = Logger.getLogger(DataProvider.class);
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null;
	//static JsonNode CrossSellNode = null;
	static CBService quoteDataInstance ;
	JsonNode headerNode = null;
	JsonNode bodyNode = null;
	JsonNode quoteDataNode = null;

	static {
		if(serverConfig == null || quoteDataInstance == null) {
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			quoteDataInstance = CBInstanceProvider.getBucketInstance(CustomerConstants.QUOTE_BUCKET);
		}
	}

	public JsonNode getQuoteDoc(String docName){
		try {
			log.info("Fetching quote DOC :"+docName);
			JsonDocument QuoteDocId = quoteDataInstance.getDocBYId(docName);
			if(QuoteDocId !=null){
				return objectMapper.readTree(QuoteDocId.content().toString());
			}else{
				new Exception();
				return null;
			}
		} catch (JsonProcessingException e) {
			log.info("Error while getting quote doc",e);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			log.info("Error while getting quote doc",e);
			e.printStackTrace();
			return null;
		}
	}

	public ObjectNode filterMapData(ObjectNode responseNode,JsonNode reqNode,Map<String, String> configDataNodeMap){
		for (Map.Entry<String, String> field : configDataNodeMap.entrySet()){
			try{
				if(reqNode.findValue(field.getKey()).isTextual()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).textValue());
				} else if (reqNode.findValue(field.getKey()).isInt()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).intValue());
				} else if (reqNode.findValue(field.getKey()).isLong()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).longValue());
				} else if (reqNode.findValue(field.getKey()).isDouble()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).doubleValue());
				} else if (reqNode.findValue(field.getKey()).isBoolean()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).booleanValue());
				} else if (reqNode.findValue(field.getKey()).isFloat()) {
					responseNode.put(field.getValue(), reqNode.findValue(field.getKey()).floatValue());
				} 
			}
			catch(NullPointerException e){
				log.error("Null Pointer Exception at filter");
			}
		}
		log.info("After Filter :"+responseNode.toString());
		return responseNode;
	}

	public ObjectNode putCustomeFields(ObjectNode reqNode , Map<String,String>  renewalLeadConfigNodeMap ){
		for(Map.Entry<String,String> fields : renewalLeadConfigNodeMap.entrySet()){
			try{
				reqNode.put(fields.getKey().toString(),fields.getValue());
			}catch (Exception e){
				log.info("exception for "+fields.getKey());
				log.info("exception is "+e);
			}
		}
		return reqNode;
	}

	public String prepareQuery(JsonNode reqNode, JsonNode queryConfig){
		log.info("reqNode in query :" + reqNode);
		log.info("queryConfig in query:" + queryConfig);
		String policySearchQuery = queryConfig.get("query").asText();
		for (JsonNode fieldName : queryConfig.get("conditionParameter")) {
			if (reqNode.has(fieldName.get("sourceFieldName").asText())) {
				policySearchQuery = policySearchQuery.replace(fieldName.get("destFieldName").asText(), reqNode.get(fieldName.get("sourceFieldName").asText()).asText());
			} else {
				policySearchQuery = policySearchQuery.replace(fieldName.get("destFieldName").asText(), "404");
			}
		}
		log.info("prepared query :" + policySearchQuery);
		return policySearchQuery;
	}
}
