package com.idep.travelquote.req.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;

public class PBQUpdateRequest {

	static Logger log = Logger.getLogger(PBQUpdateRequest.class.getName());
	
	public  static void sendPBQUpdateRequest(JsonNode request , Exchange exchange){
		
		try{
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String uri = "activemqSecondary:queue:pbqupdatereqQ";
			exchange.getIn().setBody(request.toString());
			exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
			template.send(uri, exchange);
			log.info("Updated for Travel Quote request added in activemqSecondary:queue:pbqupdatereqQ : ");
			
		}catch(Exception e){
			log.error("unable to send request to activemqSecondary:queue:pbqupdatereqQ ",e);
		}
	}
	
	
}