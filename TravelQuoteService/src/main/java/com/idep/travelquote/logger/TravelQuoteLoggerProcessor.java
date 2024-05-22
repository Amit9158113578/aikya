package com.idep.travelquote.logger;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelQuoteLoggerProcessor implements Processor {
	
	Logger log = Logger.getLogger(TravelQuoteLoggerProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode errorNode;
	
	 public void process(Exchange exchange) throws IOException {
		 
		 String data = new String();
			
		 if(exchange.getProperty(TravelQuoteConstants.DEFAULT_LOG)!=null)
			{
				data = data + exchange.getProperty(TravelQuoteConstants.DEFAULT_LOG);
			}
			if(exchange.getProperty(TravelQuoteConstants.STAGE)!=null){
				data = data + exchange.getProperty(TravelQuoteConstants.STAGE).toString()+"|";
			}
			if(exchange.getProperty(TravelQuoteConstants.STATUS)!=null){
				data = data + exchange.getProperty(TravelQuoteConstants.STATUS).toString()+"|";
			}
			log.debug(data);
	 }

}