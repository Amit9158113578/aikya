package com.idep.PolicyRenewal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmailResponseProcessor implements Processor{

	public void process(Exchange exchange) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		@SuppressWarnings("unused")
		Logger log = Logger.getLogger(EmailResponseProcessor.class.getName());
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		/*
		 * It is blank because , removed previous code
		 */
		exchange.getIn().setBody(reqNode);
	}

}
