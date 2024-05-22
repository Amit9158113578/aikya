package com.idep.healthquote.impl.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.idep.healthquote.util.HealthQuoteConstants;

public class MQMsgProcessor implements Processor {
	
	 @Override
	public void process(Exchange exchange) {
		 
		 String message = exchange.getIn().getBody().toString();
		 exchange.getIn().setHeader(HealthQuoteConstants.JMSCORRELATION_ID, exchange.getProperty(HealthQuoteConstants.CORRELATION_ID));
		 exchange.getIn().setBody(message);
	 }

}
