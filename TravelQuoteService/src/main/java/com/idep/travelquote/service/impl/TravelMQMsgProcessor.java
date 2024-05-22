package com.idep.travelquote.service.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelMQMsgProcessor implements Processor{
	public void process(Exchange exchange){
		String message = exchange.getIn().getBody(String.class);
		exchange.getIn().setHeader(TravelQuoteConstants.JMSCORRELATION_ID, exchange.getProperty(TravelQuoteConstants.CORRELATION_ID));
		exchange.getIn().setBody(message);
	}
}