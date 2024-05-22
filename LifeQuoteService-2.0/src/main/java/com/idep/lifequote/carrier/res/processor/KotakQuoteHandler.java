package com.idep.lifequote.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.lifequote.req.transformer.XMLCharEmitter;
import com.idep.lifequote.util.LifeQuoteConstants;

public class KotakQuoteHandler implements Processor {

	Logger log = Logger.getLogger(XMLCharEmitter.class.getName());
	ObjectMapper objectMapper =  new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		String request  = exchange.getIn().getBody(String.class);
		  /*
		  JsonNode InputReqNode  = objectMapper.readTree(request.toString());
		  log.info("Kotak QuotationNumber  : "+InputReqNode.get("CreateQuoteResponse").get("QuotationNumber").asText());
			 
		  JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(LifeQuoteConstants.CARRIER_INPUT_REQUEST).toString());
		  log.info("Carrier Request  : "+requestDocNode);
		  
		  JsonNode QuoteNode = this.objectMapper.createObjectNode();
		  ArrayNode QuoteList = this.objectMapper.createArrayNode();
		  
		  QuoteNode =InputReqNode.get("CreateQuoteResponse").get("QuotationNumber"); 
	      {
	    	  //Monthly necessary parameter added
	    	  ((ObjectNode)RiderNode).put("riderMonthlyPremium", carrierResNode.get("quoteResult").get("TraditionalRating").get("Premium").get("CIBRiderModalPremium").asText());
	    	
		  
			 
			?*/
		  
	}

}
