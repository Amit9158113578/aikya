package com.idep.pospservice.authentication;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserAuthenticationResponse implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UserAuthenticationResponse.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
	try{
		JsonNode resNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
		exchange.getIn().setBody(resNode);
		
	}catch(Exception e){
		log.error("Unable to send response posp  : ",e);
	}

	}

}
