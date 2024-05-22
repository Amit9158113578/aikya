package com.idep.rest.impl.smsEmailOtp;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateEmailOtpRequest implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	 Logger log = Logger.getLogger(GenerateEmailOtpRequest.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode Req = objectMapper.readTree(exchange.getIn().getBody(String.class));
			JsonNode emailRequest = objectMapper.createObjectNode();
			if(Req.has("emailRequest")){
				emailRequest = Req.get("emailRequest");
			}
			log.info("Email Request send for Processing : "+emailRequest);
			exchange.getIn().setBody(emailRequest);
			
			
		}catch(Exception e){
			log.error("Unable to send OTP on Email  : ",e);
		}
		
	}


}
