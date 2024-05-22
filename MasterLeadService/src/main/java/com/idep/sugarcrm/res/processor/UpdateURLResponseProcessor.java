package com.idep.sugarcrm.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UpdateURLResponseProcessor implements Processor{
	Logger log = Logger.getLogger(UpdateURLResponseProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	public void process(Exchange exchange) throws Exception {
		ObjectNode responseNode = objectMapper.createObjectNode();
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		if(reqNode.has("mobile") && reqNode.get("mobile") != null  &&
				reqNode.has("ImagURL") && reqNode.get("ImagURL") != null )
		{
			responseNode.put("responseCode", 1000);
			responseNode.put("message", "success");
		}else{
			responseNode.put("responseCode", 1002);
			responseNode.put("message", "failure");
		}
		exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));		
	}

}
