package com.idep.healthquote.req.transformer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;


public class XMLCharEmitter implements Processor {
	
	  Logger log = Logger.getLogger(XMLCharEmitter.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		  String request  = exchange.getIn().getBody(String.class);
		  log.debug("input XML request in XMLCharEmitter : "+request);
		  String modifiedRequest = request.replaceAll("<o>","");
		  modifiedRequest = modifiedRequest.replaceAll("</o>", "")
				                           .replaceAll("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		  log.debug("modified XML request in XMLCharEmitter : "+modifiedRequest);
		  exchange.getIn().setBody(modifiedRequest);
			
		}
		catch(Exception e)
		{
			 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"NullPointerException at XMLCharEmitter  : ",e);
			 throw new ExecutionTerminator();
		}
		  
	}

}
