package com.idep.lifequote.carrier.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.lifequote.exception.processor.ExecutionTerminator;
//import com.idep.lifequote.ext.form.req.ExternalServiceRespHandler;
import com.idep.lifequote.util.LifeQuoteConstants;

public class KotakQuoteResponseHandler implements Processor {


	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(KotakQuoteResponseHandler.class.getName());
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		 try
		 {
			 String carrierResponse = exchange.getIn().getBody(String.class);
			 JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse); //read carrier response
			 
			// log.info("Inside KotakQuoteResponseHandler. Carrier Response:-"+carrierResNode);
			 
			 JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(LifeQuoteConstants.CARRIER_INPUT_REQUEST).toString());
			 //log.info("Request Document: "+requestDocNode);
			 JsonNode productInfo = requestDocNode.get(LifeQuoteConstants.PRODUCT_INFO);
			 ((ObjectNode)requestDocNode).put(LifeQuoteConstants.CARRIER_RESPONSE, carrierResNode);
			 ((ObjectNode)requestDocNode).put("requestType", "LifeQuoteResponse");
			 //log.info("Response Node is:-"+requestDocNode);
			  
			 // set response configuration document id -->LifeQuoteRequest-Kotak-53-1
			 exchange.getIn().setHeader("documentId", LifeQuoteConstants.CARRIER_LIFE_RES_CONF+productInfo.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue()+
						  "-"+productInfo.get(LifeQuoteConstants.PRODUCTID).intValue());
			 
			 log.info("Document to fetch :-"+LifeQuoteConstants.CARRIER_LIFE_RES_CONF+""+productInfo.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue()+
						  "-"+productInfo.get(LifeQuoteConstants.PRODUCTID).intValue());
		      
			 exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
		      
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at ExternalServiceRespHandler : ", e);
			 throw new ExecutionTerminator();
		 }
		 
	 }
	

	
}
