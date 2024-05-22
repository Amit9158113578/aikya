package com.idep.createPolicyDoc;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseClass implements Processor{
	static Logger log = Logger.getLogger(ResponseClass.class);
	static ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
		
		JsonNode reqNode=null;

		String req = (String)exchange.getIn().getBody(String.class);
		log.info("Request : " + req);
		JsonNode request = objectMapper.readTree(req);
		exchange.getIn().removeHeaders("JMS*");
		exchange.getIn().removeHeaders("Content-Length");
			
	}
}
