package com.idep.policyrenewprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.policyrenewprocessor.RenewResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RenewResponse implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	
	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(RenewResponse.class.getName());
		String response = exchange.getIn().getBody().toString();
		JsonNode responseNode = this.objectMapper.readTree(response);
		log.info("Final Renew Resp : "+responseNode);
		exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
}
}