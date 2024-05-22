package com.idep.lifequote.logger.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.lifequote.util.LifeQuoteConstants;

public class LifeQuoteLoggerProcessor implements Processor {
	
	
	static ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
	
		Logger log = Logger.getLogger(LifeQuoteLoggerProcessor.class.getName());
			String data = new String();
			
			 if(exchange.getProperty(LifeQuoteConstants.DEFAULT_LOG_DATA)!=null)
			 {
				 data = data + exchange.getProperty(LifeQuoteConstants.DEFAULT_LOG_DATA);
			 }
			if(exchange.getProperty(LifeQuoteConstants.STAGE)!=null){
				data = data + exchange.getProperty(LifeQuoteConstants.STAGE).toString()+"|";
			}
			if(exchange.getProperty(LifeQuoteConstants.STATUS)!=null){
				data = data + exchange.getProperty(LifeQuoteConstants.STATUS).toString()+"|";
			}
			log.info(data);
	}
}
