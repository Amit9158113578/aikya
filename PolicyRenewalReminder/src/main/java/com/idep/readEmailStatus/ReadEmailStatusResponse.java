package com.idep.readEmailStatus;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReadEmailStatusResponse implements Processor{
    ObjectMapper objectMapper = new ObjectMapper();

	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(ReadEmailStatusResponse.class.getName());
		ObjectNode responseNode = objectMapper.createObjectNode();
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("ReadEmailStatusResponse Req :"+reqNode);
		responseNode.put("status", "success");
		exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
	}
}
