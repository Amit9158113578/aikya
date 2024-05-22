package com.idep.OfflinePolicyRenewal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OfflineRenewalResponseProcessor implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(OfflineRenewalResponseProcessor.class.getName());
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("Final Offine Renewal Response : "+reqNode);
		exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
	}
}

