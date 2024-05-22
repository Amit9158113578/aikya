package com.idep.travelquote.multijson.support;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

public class RequestTransformSaver  implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(RequestTransformSaver.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String quoteRequest = exchange.getIn().getBody(String.class);
			JsonNode modQuoteReq = this.objectMapper.readTree(quoteRequest);

			// set carrier transformed request
			exchange.setProperty("carrierTransformedReq", modQuoteReq);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(modQuoteReq));
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.REQTRNASFORM+"|ERROR|"+" Exception at RequestTransformSaver  :",e);
			throw new ExecutionTerminator();
		}

	}

}
