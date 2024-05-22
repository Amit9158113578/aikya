package com.idep.healthquote.carrier.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.healthquote.util.HealthQuoteConstants;

public class CarrierQuoteServiceReqLogger implements Processor {
	
	Logger log = Logger.getLogger(CarrierQuoteServiceReqLogger.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {

		String carrierQuoteRequest = exchange.getIn().getBody(String.class);
		log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"SERVICEINVOKE|SUCCESS|"+"carrier service invoked : "+carrierQuoteRequest);
		
	}


}