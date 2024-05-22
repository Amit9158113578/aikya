package com.idep.healthquote.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.util.HealthQuoteConstants;

public class HealthQuoteExceptionProcessor implements Processor {
	
	 ObjectMapper objectMapper = new ObjectMapper();
	 Logger log = Logger.getLogger(HealthQuoteExceptionProcessor.class.getName());
	 JsonNode errorNode;
	
	 @Override
	public void process(Exchange exchange) {
		 
		  this.log.error("ExceptionProcessor Handler called ....");
	      ObjectNode objectNode = this.objectMapper.createObjectNode();
	      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, 3000);
	      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, "drool server seems to be down");
	      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
	      exchange.getIn().setBody(objectNode);
	 }


}
