package com.idep.quote.result.listener;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QuoteResultHandler implements Processor
{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(QuoteResultHandler.class.getName());
	JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			
			String request = exchange.getIn().getBody(String.class);
			JsonNode reqNode = objectMapper.readTree(request);
			//String messageId = reqNode.findValue("messageId").asText();
			 
			 String requestType = reqNode.findValue("requestType").asText();
		    log.info("requestType in QuoteResultHandler:" +requestType);
		     if(requestType.equalsIgnoreCase("LifeQuoteRequest"))
		    	 {
		    	 	if(reqNode.get("lifeQuoteRequest").has("messageId"))
		    	 	{
		    	 		exchange.getIn().setHeader("carrierMessageID","True");
		    	 		exchange.getIn().setBody(reqNode);
		    	 		//log.info("carrierMessageID in QuoteResultHandler is True :");
		    	 	}		    	     
		    	 }
		    	 else if(requestType.equalsIgnoreCase("BikeQuoteRequest"))
		    	 {                   
		    		 if(reqNode.get("quoteInputRequest").has("messageId"))
		    		 {
		    			 //log.info("carrierMessageID in QuoteResultHandler is True :");
		    			 exchange.getIn().setHeader("carrierMessageID","True");
		    			 exchange.getIn().setBody(reqNode);
			    	 }
		    	 }
		    	 else if(requestType.equalsIgnoreCase("CarQuoteRequest"))
		    	 {
		    		 if (reqNode.get("carQuoteRequest").has("messageId"))
		    		 { 
		    			 //log.info("carrierMessageID in QuoteResultHandler is True :");
		    			 exchange.getIn().setHeader("carrierMessageID","True");
		    			 exchange.getIn().setBody(reqNode);
		    		 }
		    		
		    	 }
		    	 else if(requestType.equalsIgnoreCase("HealthQuoteRequest"))
		    	 {
		    		 if (reqNode.get("quoteRequest").has("messageId"))
		    		 { 
		    			 //log.info("carrierMessageID in QuoteResultHandler is True :");
		    			 exchange.getIn().setHeader("carrierMessageID","True");
		    			 exchange.getIn().setBody(reqNode);
		    		 }
		    	 }
		    	 else if(requestType.equalsIgnoreCase("TravelQuoteRequest"))
		    	 {
		    		 if (reqNode.get("quoteRequest").has("messageId"))
		    		 { 
		    			 //log.info("carrierMessageID in QuoteResultHandler is True :");
		    			 exchange.getIn().setHeader("carrierMessageID","True");
		    			 exchange.getIn().setBody(reqNode);
		    		 }
		    	 }
		    	 else if(requestType.equalsIgnoreCase("PersonalAccidentQuoteRequest"))
		    	 {
		    		 if (reqNode.get("quoteRequest").has("messageId"))
		    		 { 
		    			 //log.info("carrierMessageID in QuoteResultHandler is True :");
		    			 exchange.getIn().setHeader("carrierMessageID","True");
		    			 exchange.getIn().setBody(reqNode);
		    		 }
		    	 }
		    	 else
		    	 {
		    		log.error("requestType missing in Lead Q message");
		    	 }
					
		}
		catch(NullPointerException e)
		{
			log.error("NullPointer Exception in QuoteResultHandler : : "+e);
			
		}
		catch(Exception e)
		{
			log.error("Exception in QuoteResultHandler : : "+e);
		}

	}
}
