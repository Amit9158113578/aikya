package com.idep.healthquote.carrier.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.healthquote.util.HealthQuoteConstants;

public class CarrierQuoteServiceResLogger implements Processor {
	
	Logger log = Logger.getLogger(CarrierQuoteServiceResLogger.class.getName());
	
	public void process(Exchange exchange) throws Exception {

		log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTERES|SUCCESS|"+"quote calculated successfully");
		
	}


}

