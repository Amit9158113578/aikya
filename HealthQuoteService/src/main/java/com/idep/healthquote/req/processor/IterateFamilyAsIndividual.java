package com.idep.healthquote.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.util.HealthQuoteConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
	
	public class IterateFamilyAsIndividual implements Processor {
		
		Logger log = Logger.getLogger(IterateFamilyAsIndividual.class.getName());
		  ObjectMapper objectMapper = new ObjectMapper();

		@Override
		public void process(Exchange exchange) throws Exception {
			
			String quotedata = exchange.getIn().getBody().toString();
		    JsonNode reqNode = this.objectMapper.readTree(quotedata);
		    ObjectNode planType = (ObjectNode) reqNode.get("inputMessage").get("quoteParam");
		    ObjectNode productInfo = (ObjectNode)reqNode.get("inputMessage").get("productInfo");
		      
		    CBService productConfig = CBInstanceProvider.getProductConfigInstance();
		    JsonNode responseConfigNode = objectMapper.readTree(productConfig.getDocBYId(("HealthPlan-"+productInfo.get("carrierId").asText()+"-"+productInfo.get("planId").asText()).toString()).content().toString());
		    
		    if(responseConfigNode.has("iterateAsIndividual") && responseConfigNode.get("iterateAsIndividual").asText().equalsIgnoreCase("Y") ){
				planType.put("planType","I");
				productInfo.put("planType", "I");
		    }
		    
		    exchange.getIn().setBody(reqNode);
		}
}
