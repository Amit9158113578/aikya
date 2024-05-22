package com.idep.travelquote.dbstore;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelQuoteDBSender implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelQuoteDBSender.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try
		{
			String message = exchange.getIn().getBody().toString();
			 JsonNode quoteResponseNode = this.objectMapper.readTree(message);
			 if(quoteResponseNode.get("responseCode").intValue()==1000)
			 {
				 JsonNode quoteResNode = quoteResponseNode.get("data").get("quotes").get(0);
				 String bikeQuoteId = exchange.getProperty(TravelQuoteConstants.QUOTE_ID).toString();
				
				 
				 JsonNode inputReqNode = this.objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
				/* if(inputReqNode.get("productInfo").has("productId"))
				 { 
				 ((ObjectNode)quoteResNode).put("planId",inputReqNode.get("productInfo").get("productId"));
				 }*/
				 ((ObjectNode)inputReqNode).remove(TravelQuoteConstants.PRODUCT_INFO);
				 JsonNode carrierTransReqNode = this.objectMapper.readTree(exchange.getProperty("carrierTransformedReq").toString());
				 String encryptedQuoteId = exchange.getProperty(TravelQuoteConstants.ENCRYPT_QUOTE_ID).toString();				 
				
				 ObjectNode travelQuoteResNode = this.objectMapper.createObjectNode();
				 if(exchange.getProperty(TravelQuoteConstants.LEAD_MESSAGE_ID) != null){
					 String messageId = exchange.getProperty(TravelQuoteConstants.LEAD_MESSAGE_ID).toString();
					 travelQuoteResNode.put(TravelQuoteConstants.MESSAGE_ID,messageId);
				 }
				 travelQuoteResNode.put(TravelQuoteConstants.QUOTE_ID,bikeQuoteId);
				 travelQuoteResNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
				 travelQuoteResNode.put("quoteInputRequest",inputReqNode);
				 travelQuoteResNode.put("carrierQuoteResponse",quoteResNode);
				 travelQuoteResNode.put("carrierTransformedReq",carrierTransReqNode);
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(travelQuoteResNode));
			 }
			 else
			 {
				 log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+"TravelQuoteDBSender"+"|ERROR|"+" Exception at BikeQuoteDBSender :");
				 ObjectNode bikeQuoteResNode = this.objectMapper.createObjectNode();
				 bikeQuoteResNode.put(TravelQuoteConstants.QUOTE_ID,"ERROR");
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(bikeQuoteResNode));
			 }
		}
		catch(Exception e)
		 {
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+"TravelQuoteDBSender"+"|ERROR|"+" Exception at BikeQuoteDBSender :");
		 }
		
	}

}