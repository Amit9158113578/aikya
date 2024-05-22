package com.idep.rest.impl.smsEmailOtp;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateSMSRequestProcessor implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	 Logger log = Logger.getLogger(GenerateSMSRequestProcessor.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode Req = objectMapper.readTree(exchange.getIn().getBody(String.class));
			JsonNode smsRequest = objectMapper.createObjectNode();
			if(Req.has("smsRequest")){
				smsRequest = Req.get("smsRequest");
			}
			log.info("SMS Request send for Processing : "+smsRequest);
			exchange.getIn().setBody(smsRequest);
			
			
		}catch(Exception e){
			
			log.error("Unable to send OTP SMS : ",e);
		}
		
	}

}
