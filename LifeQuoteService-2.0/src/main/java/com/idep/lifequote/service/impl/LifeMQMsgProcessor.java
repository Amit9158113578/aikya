package com.idep.lifequote.service.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeMQMsgProcessor implements Processor{
	public void process(Exchange exchange){
		String message = exchange.getIn().getBody(String.class);
		exchange.getIn().setHeader(LifeQuoteConstants.JMSCORRELATION_ID, exchange.getProperty(LifeQuoteConstants.CORRELATION_ID));
		exchange.getIn().setBody(message);
	}
}
