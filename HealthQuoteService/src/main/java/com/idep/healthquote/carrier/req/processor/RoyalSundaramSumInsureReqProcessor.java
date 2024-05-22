package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.util.HealthQuoteConstants;

public class RoyalSundaramSumInsureReqProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(RoyalSundaramSumInsureReqProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		String mapperRes = exchange.getIn().getBody(String.class);
		JsonNode mapperReqNode = this.objectMapper.readTree(mapperRes);
		log.info("RoyalSumInsured inputReqNode"+mapperReqNode);
		JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST).toString());
		log.info("requestDocNode Royal"+requestDocNode);
		String SumInsured = mapperReqNode.findValue("sumInsured").asText();
		((ObjectNode)requestDocNode.findValue("productInfo")).put("sumInsured", SumInsured);
		 exchange.setProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST,objectMapper.writeValueAsString(requestDocNode));
		 exchange.getIn().setBody(objectMapper.writeValueAsString(mapperReqNode));
		
	}

}
