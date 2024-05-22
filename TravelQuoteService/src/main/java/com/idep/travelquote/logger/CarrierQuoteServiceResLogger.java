package com.idep.travelquote.logger;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.travelquote.util.TravelQuoteConstants;

public class CarrierQuoteServiceResLogger implements Processor {
	
	Logger log = Logger.getLogger(CarrierQuoteServiceResLogger.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {

		log.info(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.QUOTERES+"|SUCCESS|"+"quote calculated successfully");
		
	}


}
