package com.idep.lifequote.req.form;

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
public class LifeQuoteExceptionProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeQuoteExceptionProcessor.class.getName());
	JsonNode errorNode;

	public void process(Exchange exchange){
		log.info("ExceptionProcessor Handler called ....");
		ObjectNode objectNode = this.objectMapper.createObjectNode();
		objectNode.put(LifeQuoteConstants.QUOTE_RES_CODE, 3000);
		objectNode.put(LifeQuoteConstants.QUOTE_RES_MSG, "drool server seems to be down");
		objectNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		exchange.getIn().setBody(objectNode);
	}
}