package com.idep.url.reroute.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class HttpErrorProcessor implements Processor {
	
	Logger log = Logger.getLogger(HttpErrorProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		log.info("HTTP ERROR : =>: "+exchange.getIn().getHeaders());
		log.info("HTTP Response code =>: "+Exchange.HTTP_RESPONSE_CODE);
		Exception exception = (Exception)exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
		log.info("error message details =>: "+exception.getMessage());
		
		ObjectNode httpErrorNode = objectMapper.createObjectNode();
		httpErrorNode.put("error", "not_found");
		httpErrorNode.put("reason", "missing");
		exchange.getIn().setBody(objectMapper.writeValueAsString(httpErrorNode));
		
	}

}
