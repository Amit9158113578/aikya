package com.idep.travelquote.logger;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.travelquote.util.TravelQuoteConstants;


public class CarrierQuoteServiceReqLogger implements Processor {
	
	Logger log = Logger.getLogger(CarrierQuoteServiceReqLogger.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String carrierQuoteRequest = exchange.getIn().getBody(String.class);
		log.debug(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.SERVICEINVOKE+"|SUCCESS|"+"carrier service invoked : "+carrierQuoteRequest);
	}


}