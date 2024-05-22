package com.idep.pospservice.authentication;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.pospservice.util.ExecutionTerminator;

public class SMSEmailRequestGenerater implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	 Logger log = Logger.getLogger(SMSEmailRequestGenerater.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
			try{
				String req = exchange.getProperty("PospRegReq").toString() ;
				
				if(req==null){
					throw new ExecutionTerminator();
				}
				//JsonNode smsEmailReq  = objectMapper.readTree(req);
				exchange.getIn().setBody(req);
				
			}catch(Exception e){
				log.error("unable to send request for SMS & email : ",e);
			}
		
	}

}
