package com.idep.healthquote.carrier.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.healthquote.util.HealthQuoteConstants;

public class HealthQuoteLoggerProcessor implements Processor {
	
	Logger log = Logger.getLogger(HealthQuoteLoggerProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {
		
		 String data = new String();
		 
		 JsonNode quoteReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		 if(exchange.getProperty(HealthQuoteConstants.DEFAULT_LOG)!=null)
		 {
			 data = data + exchange.getProperty(HealthQuoteConstants.DEFAULT_LOG);
		 }
		 if(exchange.getProperty(HealthQuoteConstants.STAGE)!=null){
				data = data + exchange.getProperty(HealthQuoteConstants.STAGE).toString()+"|";
			}
			if(exchange.getProperty(HealthQuoteConstants.STATUS)!=null){
				data = data + exchange.getProperty(HealthQuoteConstants.STATUS).toString()+"|";
			}
			log.info(data);
	}

}
