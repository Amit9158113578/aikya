package com.idep.customer.reqprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OfflineCustomerReqProcessor implements Processor{
	static Logger log = Logger.getLogger(OfflineCustomerReqProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("offline customer request :"+reqNode);
		exchange.getIn().setBody(reqNode.toString());
	}
}
