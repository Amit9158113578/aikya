package com.idep.travelquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.util.TravelQuoteConstants;

public class FutureGeneraliInputRequestProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FutureGeneraliInputRequestProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		try{
		String mapperRes = exchange.getIn().getBody(String.class);
		JsonNode mapperReqNode = this.objectMapper.readTree(mapperRes);
		JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
		String carrierQuoteId = mapperReqNode.findValue("PHA2").asText();
		((ObjectNode)requestDocNode.findValue("productInfo")).put("carrierQuoteId", carrierQuoteId);
		log.info("SumInsured updated in property request : "+requestDocNode);
		 exchange.setProperty(TravelQuoteConstants.UI_QUOTEREQUEST,objectMapper.writeValueAsString(requestDocNode));
		 exchange.getIn().setBody(objectMapper.writeValueAsString(mapperReqNode));
		}catch(Exception e){
		log.error("Exception at CarrierUpdatedSumInsured : ",e);	
		}
	}

}

