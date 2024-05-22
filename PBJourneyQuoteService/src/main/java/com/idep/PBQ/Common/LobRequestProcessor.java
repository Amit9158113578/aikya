package com.idep.PBQ.Common;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LobRequestProcessor implements Processor {

	
	ObjectMapper objectMapper =new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		 String stringQuoteRequest = exchange.getIn().getBody().toString();
		 JsonNode quoteRequestNode = objectMapper.readTree(stringQuoteRequest);
		 exchange.setProperty("LobQuoteRequest", quoteRequestNode);
		 exchange.getIn().setBody(quoteRequestNode);
	}

}
