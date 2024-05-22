package com.idep.healthquote.ext.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;

public class CarrierTransformReqCollector implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarrierTransformReqCollector.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try
		{
			String quoteRequest = exchange.getIn().getBody(String.class);
			JsonNode modQuoteReq = this.objectMapper.readTree(quoteRequest);
			// set carrier transformed request
			exchange.setProperty("carrierTransformedReq", modQuoteReq);
			exchange.getIn().setBody(quoteRequest);
		}
		catch(Exception e)
		{
			log.error(e);
			throw new ExecutionTerminator();
		}
		
	}
}

