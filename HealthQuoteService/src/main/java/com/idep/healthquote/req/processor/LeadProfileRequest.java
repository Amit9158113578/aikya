package com.idep.healthquote.req.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class LeadProfileRequest {

	static Logger log = Logger.getLogger(LeadProfileRequest.class.getName());
	
	public  static void sendLeadProfileRequest(ObjectNode request , Exchange exchange){
		
		try{
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String uri = "activemqSecondary:queue:P365LeadQuoteUpdationQ";
			log.info("snding to LeadProfileQuoteUpdationQ for Health"+exchange);
			exchange.getIn().setBody(request.toString());
			exchange.setPattern(ExchangePattern.InOnly); 
			template.send(uri, exchange);
			
		}catch(Exception e){
			log.error("unable to send request to activemqSecondary:queue:P365LeadQuoteUpdationQ ",e);
		}
	}
	
	
}
