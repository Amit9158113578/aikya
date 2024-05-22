package com.idep.travelquote.req.transformer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;


public class XMLCharEmitter implements Processor {

	Logger log = Logger.getLogger(XMLCharEmitter.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {

		try {

			String request  = exchange.getIn().getBody(String.class);
			String modifiedRequest = request.replaceAll("<o>","");
			modifiedRequest = modifiedRequest.replaceAll("</o>", "");
			exchange.getIn().setBody(modifiedRequest);

		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.XMLCHARTEMI+"|ERROR|"+" Exception at XMLCharEmitter :",e);
			new ExecutionTerminator();
		}

	}

}
