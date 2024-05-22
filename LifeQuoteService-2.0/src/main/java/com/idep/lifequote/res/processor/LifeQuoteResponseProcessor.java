package com.idep.lifequote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.lifequote.util.LifeQuoteConstants;

public class LifeQuoteResponseProcessor implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeQuoteResponseProcessor.class.getName());
	//JsonObject responseConfigNode = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.RESPONSE_CONFIG_DOC);
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String quoteResdata = exchange.getIn().getBody().toString();
		JsonNode quoteResponseNode = this.objectMapper.readTree(quoteResdata);
		
		ArrayNode lifeQuoteResult = objectMapper.createArrayNode();
		
		/**
		 * Added for professional journey , Response in sending Profession quote Id which id property set in LifeRequestQProcessor.java
		 * **/
		if(exchange.getProperty("PROF_QUOTE_ID")!=null){
			((ObjectNode)quoteResponseNode).put("PROF_QUOTE_ID", exchange.getProperty("PROF_QUOTE_ID").toString());	
		}
		if(quoteResponseNode.has("riderList")&&quoteResponseNode.get("riderList").size()>0){
			long riderToatalAmount = 0;
			ArrayNode processedRidersList =(ArrayNode)quoteResponseNode.get("riderList");
			for(JsonNode resRider : processedRidersList){
				if(resRider.has("riderPremiumAmount")){
					riderToatalAmount = riderToatalAmount+resRider.get("riderPremiumAmount").asLong();
				}
			}
			
			if(quoteResponseNode.has("basicPremium")){
				
				long basicPremiumwWithRider = quoteResponseNode.get("basicPremium").asLong()+riderToatalAmount;
				((ObjectNode)quoteResponseNode).put("basicPremiumWithRider",basicPremiumwWithRider);
				((ObjectNode)quoteResponseNode).put("riderTotalAmt",riderToatalAmount);
				log.info("Rider AMOUNT Added in Basic Premium : "+basicPremiumwWithRider);
			}
			
		}else{
			((ObjectNode)quoteResponseNode).put("basicPremiumWithRider", quoteResponseNode.get("basicPremium").asLong());
			((ObjectNode)quoteResponseNode).put("riderTotalAmt",0);
		}
		
		lifeQuoteResult.add(quoteResponseNode);
		ObjectNode quoteResultNode = this.objectMapper.createObjectNode();
			
		quoteResultNode.put("quotes", lifeQuoteResult);
		
		ObjectNode objectNode = this.objectMapper.createObjectNode();
		
		objectNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.SUCC_CONFIG_CODE).asInt());
		objectNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.SUCC_CONFIG_MSG).asText());
		objectNode.put(LifeQuoteConstants.QUOTE_RES_DATA, quoteResultNode);
		exchange.getIn().setBody(objectNode);
		
	}
	

}
