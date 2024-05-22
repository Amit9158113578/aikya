package com.idep.healthquote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * @author sandeep jadhav
 * Set UI request,carrier transformed request and response.
 */
public class HealthQuoteDBSender implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthQuoteDBSender.class.getName());
	
	 @Override
	public void process(Exchange exchange) {
		 
		 try {
			 
			 String message = exchange.getIn().getBody().toString();
			 JsonNode quoteResponseNode = this.objectMapper.readTree(message);
			
			 if(quoteResponseNode.get("responseCode").intValue()==1000)
			 {
				 
				 JsonNode quoteResNode = quoteResponseNode.get("data").get("quotes").get(0);
				 // remove features list from response node
				 //((ObjectNode)quoteResNode).remove(HealthQuoteConstants.FEATURES_LIST);
				 // get quote id
				 String quoteId = exchange.getProperty(HealthQuoteConstants.QUOTE_ID).toString();
				 String encryptedQuoteId = exchange.getProperty(HealthQuoteConstants.ENCRYPT_QUOTE_ID).toString();
				 // get user input request from exchange property
				 JsonNode inputReqNode = this.objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.UI_QUOTEREQUEST).toString());
				 // remove product information
				 ((ObjectNode)inputReqNode).remove(HealthQuoteConstants.PRODUCT_INFO);
				 // get carrier transformed request
				 JsonNode carrierTransReqNode = this.objectMapper.readTree(exchange.getProperty("carrierTransformedReq").toString());
				 
				 // created to store formatted quote results in database
				 ObjectNode quoteReqResNode = this.objectMapper.createObjectNode();
				 quoteReqResNode.put(HealthQuoteConstants.QUOTE_ID,quoteId);
				 quoteReqResNode.put(HealthQuoteConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
				 quoteReqResNode.put("quoteRequest",inputReqNode);
				 quoteReqResNode.put("carrierQuoteResponse",quoteResNode);
				 quoteReqResNode.put("carrierTransformedReq",carrierTransReqNode);
				 
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(quoteReqResNode));
				 
			 }
			 else
			 {
				 this.log.error("HealthQuoteDBSender : ignored quote due to error in response");
				 ObjectNode carQuoteResNode = this.objectMapper.createObjectNode();
				 carQuoteResNode.put(HealthQuoteConstants.QUOTE_ID,"ERROR");
				 carQuoteResNode.put(HealthQuoteConstants.ENCRYPT_QUOTE_ID,"ERROR");
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(carQuoteResNode));
			 }
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at HealthQuoteDBSender : ",e);
		 }
	 }


}
