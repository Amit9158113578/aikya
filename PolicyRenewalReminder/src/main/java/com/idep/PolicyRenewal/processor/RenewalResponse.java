package com.idep.PolicyRenewal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RenewalResponse implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	
	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(RenewalResponse.class.getName());
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = this.objectMapper.readTree(request);
		//((ObjectNode) reqNode).put("messageId",reqNode.get("messageId").asText());
		log.info("Final Renewal Resp : "+reqNode);
		exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
}
}
