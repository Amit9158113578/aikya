package com.idep.lifequote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeQuoteDBSender implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeQuoteDBSender.class.getName());
	
	 public void process(Exchange exchange){
		 try{
			 String message = exchange.getIn().getBody().toString();
			 JsonNode quoteResponseNode = this.objectMapper.readTree(message);
			 
			 if(quoteResponseNode.get("responseCode").intValue() == 1000){
				 JsonNode quoteResNode = quoteResponseNode.get("data").get("quotes").get(0);
				 String carQuoteId = exchange.getProperty(LifeQuoteConstants.QUOTE_ID).toString();
				 String encryptedQuoteId = exchange.getProperty(LifeQuoteConstants.ENCRYPT_QUOTE_ID).toString();
				 JsonNode inputReqNode = this.objectMapper.readTree(exchange.getProperty(LifeQuoteConstants.UI_CARQUOTEREQUEST).toString());
				 ((ObjectNode)inputReqNode).remove(LifeQuoteConstants.PRODUCT_INFO);	// remove product information
				  
				 // created to store formatted quote results in database
				 ObjectNode carQuoteResNode = this.objectMapper.createObjectNode();
				 carQuoteResNode.put(LifeQuoteConstants.QUOTE_ID,carQuoteId);
				 carQuoteResNode.put(LifeQuoteConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
				 carQuoteResNode.put("lifeQuoteRequest",inputReqNode);
				 carQuoteResNode.put("carrierQuoteResponse",quoteResNode);
				 log.debug("quote results sent to DB : " + carQuoteResNode);
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(carQuoteResNode));
			 }else{
				 log.error(LifeQuoteDBSender.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"- LifeQuoteDBSender ignored quote due to error in response");	 
				 ObjectNode carQuoteResNode = this.objectMapper.createObjectNode();
				 carQuoteResNode.put(LifeQuoteConstants.QUOTE_ID,"ERROR");
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(carQuoteResNode));
			 }
		 }catch(Exception e){
			 log.error(LifeQuoteDBSender.class+" - "+Thread.currentThread().getStackTrace()[1].getMethodName()+" - "+ Thread.currentThread().getStackTrace()[2].getLineNumber()+"- Exception at LifeQuoteDBSender : "+e);
		 }
	 }
}