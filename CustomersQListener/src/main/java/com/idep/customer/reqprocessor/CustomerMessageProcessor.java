package com.idep.customer.reqprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CustomerMessageProcessor implements Processor{
	static Logger log = Logger.getLogger(CustomerMessageProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		if(reqNode.has("isOffline") && (!reqNode.has("proposalId"))){
			exchange.getIn().setHeader("isOffline", "Y");
		}else{
			exchange.getIn().setHeader("isOffline", "N");
		}
		exchange.setProperty("request", reqNode);	
		exchange.getIn().setBody(reqNode);
	}
}