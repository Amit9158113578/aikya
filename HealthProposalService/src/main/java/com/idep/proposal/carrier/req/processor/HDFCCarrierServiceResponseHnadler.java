package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.policy.exception.processor.ExecutionTerminator;


public class HDFCCarrierServiceResponseHnadler implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HDFCCarrierServiceResponseHnadler.class.getName());
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		 try
		 {
			 String carrierResponse = exchange.getIn().getBody(String.class);
			 String finalstring = carrierResponse.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "").replace("<?xml version=\"1.0\" encoding=\"utf-16\"?>", "");
			 finalstring = finalstring.replace("a:", "").replace("i:nil=\"true\"", "");
		     exchange.getIn().setBody(finalstring);
		      
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at ExternalServiceRespHandler : ", e);
			 throw new ExecutionTerminator();
		 }
		 
	 }
	

}
