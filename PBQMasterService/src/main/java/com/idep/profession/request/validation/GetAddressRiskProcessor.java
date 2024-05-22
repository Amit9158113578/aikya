package com.idep.profession.request.validation;

import java.io.IOException;
import java.util.List;

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

/**
 * @author pravin.jakhi
 *
 */
public class GetAddressRiskProcessor implements Processor {

	static ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GetAddressRiskProcessor.class);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static JsonNode queryConfigDoc = null;
	static{
		
		try {
			queryConfigDoc = objectMapper.readTree(serverConfig.getDocBYId("PBQueryConfig").content().toString());
		}catch (Exception e) {
			Logger.getLogger(GetAddressRiskProcessor.class).error("unable to catch Query Document : PBQueryConfig ");
		}
	}
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode requestNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			//ObjectNode pincodeLst = PBJDBCacheConfigProcessor.getPincodeList();
		/****
		 * for tetsing purpose ADDED BELOW CODE
		 * 
		 * */
		 if(requestNode.get("commonInfo").get("address").has("pincode"))
			{
			 List<JsonObject> pincodeList = null;
			 
			 
			 String query = null;
			 
			 
			 if(queryConfigDoc==null){
				 queryConfigDoc = objectMapper.readTree(serverConfig.getDocBYId("PBQueryConfig").content().toString()); 
				 query = queryConfigDoc.get("getProfessionPincode").asText();
			 }else{
				 query = queryConfigDoc.get("getProfessionPincode").asText();
			 }
			 
			 JsonArray paramobj = JsonArray.create();
			 paramobj.add(requestNode.get("commonInfo").get("address").get("pincode").asText());
			 pincodeList = serverConfig.executeConfigParamArrQuery(query, paramobj);
			 log.info("DB Risk Finer Processor : "+pincodeList);
			 //JsonDocument pincodeDoc = serverConfig.getDocBYId("Pincode-"+requestNode.get("commonInfo").get("address").get("pincode").asText());
			
			if(pincodeList!=null){
				log.info("Address Risk Found in DB : "+pincodeList.get(0));
				JsonNode pincode = objectMapper.readTree(pincodeList.get(0).toString());
						if(pincode.has("Risk")){
							((ObjectNode)requestNode).put("addressRisk", pincode.get("Risk").asText());
						}else{
							((ObjectNode)requestNode).put("addressRisk", "Medium");
						}
			}else{
				((ObjectNode)requestNode).put("addressRisk", "Medium");
			}
			}else{
				((ObjectNode)requestNode).put("addressRisk", "Medium");
			}
			
			
			if(requestNode.has("QUOTE_ID")){
				exchange.setProperty("PROF_QUOTE_ID",requestNode.get("QUOTE_ID").asText());	
			}
			if(requestNode.has("PROF_QUOTE_ID")){
				exchange.setProperty("PROF_QUOTE_ID",requestNode.get("PROF_QUOTE_ID").asText());	
			}
			exchange.getIn().setBody(requestNode);
			
		}catch(Exception e){
			log.error("unable to find address risk : ",e);
		}

	}

}
