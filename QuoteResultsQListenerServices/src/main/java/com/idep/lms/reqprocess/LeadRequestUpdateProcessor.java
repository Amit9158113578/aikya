/**
 * 
 */
package com.idep.lms.reqprocess;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.idep.encrypt.EncryptionDecryption;

/**
 * @author vipin.patil
 *
 */
public class LeadRequestUpdateProcessor implements Processor{
	
	Logger log = Logger.getLogger(LeadRequestUpdateProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	//JsonNode leadReqNode = null;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		 String request = (String)exchange.getIn().getBody(String.class);
		 String requestType = null;
	     JsonNode reqNode = objectMapper.readTree(request);
	     ObjectNode headerNode = objectMapper.createObjectNode();
	     ObjectNode bodyNode = objectMapper.createObjectNode();
	    // EncryptionDecryption encrypt = new EncryptionDecryption();
	     ObjectNode leadReqNode = objectMapper.createObjectNode();
	     requestType = reqNode.findValue("requestType").asText();
	     if(requestType.equalsIgnoreCase("LifeQuoteRequest"))
	    	 {
	    		 headerNode.put("transactionName","getLifeQuoteResult");
	    	     headerNode.put("messageId",reqNode.get("lifeQuoteRequest").get("messageId").asText());
	    	     bodyNode.put("QUOTE_ID",reqNode.get("QUOTE_ID"));
	    	    // bodyNode.put("ENC_QUOTE_ID",encrypt.encrypt(reqNode.get("QUOTE_ID").asText()));
	    	     
	    	     bodyNode.put("messageId",reqNode.get("lifeQuoteRequest").get("messageId").asText());
	    	     //((ObjectNode)reqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("body",bodyNode);
	    	     //log.info("Added header Node for LifeQuoteRequest: "+leadReqNode);
	    	 }
	    	 else if(requestType.equalsIgnoreCase("BikeQuoteRequest"))
	    	 {
	    		 headerNode.put("transactionName","getBikeQuoteResult");
	    	     headerNode.put("messageId",reqNode.get("quoteInputRequest").get("messageId").asText());
	    	     bodyNode.put("QUOTE_ID",reqNode.get("QUOTE_ID"));
	    	     //bodyNode.put("ENC_QUOTE_ID",encrypt.encrypt(reqNode.get("QUOTE_ID").asText()));
	    	     bodyNode.put("messageId",reqNode.get("quoteInputRequest").get("messageId").asText());
	    	     //((ObjectNode)reqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("body",bodyNode);
	    	     //log.info("Added header Node for BikeQuoteRequest : "+leadReqNode);
	    	 }
	    	 else if(requestType.equalsIgnoreCase("CarQuoteRequest"))
	    	 {
	    		
	    		 headerNode.put("transactionName","getCarQuoteResult");
	    	     headerNode.put("messageId",reqNode.get("carQuoteRequest").get("messageId").asText());
	    	     bodyNode.put("QUOTE_ID",reqNode.get("QUOTE_ID"));
	    	    // bodyNode.put("ENC_QUOTE_ID",encrypt.encrypt(reqNode.get("QUOTE_ID").asText()));
	    	     bodyNode.put("messageId",reqNode.get("carQuoteRequest").get("messageId").asText());
	    	     //((ObjectNode)reqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("body",bodyNode);
	    	     //log.info("Added header Node for CarQuoteRequest: "+leadReqNode);
	    		
	    	 }
	    	 else if(requestType.equalsIgnoreCase("HealthQuoteRequest"))
	    	 {
	    		 headerNode.put("transactionName","getHealthQuoteResult");
	    	     headerNode.put("messageId",reqNode.get("quoteRequest").get("messageId").asText());
	    	     bodyNode.put("QUOTE_ID",reqNode.get("QUOTE_ID"));
	    	     //bodyNode.put("ENC_QUOTE_ID",encrypt.encrypt(reqNode.get("QUOTE_ID").asText()));
	    	     bodyNode.put("messageId",reqNode.get("quoteRequest").get("messageId").asText());
	    	     //((ObjectNode)reqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("header",headerNode);
	    	     ((ObjectNode)leadReqNode).put("body",bodyNode);
	    	     //log.info("Added header Node for HealthQuoteRequest: "+leadReqNode);
	    	 }
	    	 else
	    	 {
	    		log.error("requestType missing in Lead Q message");
	    	 }
	     	     
	     exchange.getIn().setBody(objectMapper.writeValueAsString(leadReqNode));
	     //log.info("Request Updation Done");
	}

}
