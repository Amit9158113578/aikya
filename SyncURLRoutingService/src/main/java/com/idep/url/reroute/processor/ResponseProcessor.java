package com.idep.url.reroute.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ResponseProcessor  implements Processor {

	Logger log = Logger.getLogger(ResponseProcessor.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		//exchange.getContext().getStreamCachingStrategy().setSpoolThreshold(-1);
		//exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json; charset=utf-16");
		
		
		/*exchange.getOut().setHeader(Exchange.CONTENT_ENCODING, "UTF-8");
		exchange.getOut().setHeader(Exchange.HTTP_CHARACTER_ENCODING, "UTF-8");
		exchange.getOut().setHeader(Exchange.SKIP_GZIP_ENCODING, false);*/
		
		String serviceResponse = exchange.getIn().getBody(String.class);
		//log.info("Service Response : "+serviceResponse);
		exchange.getIn().setBody(serviceResponse);
		
	}

}
