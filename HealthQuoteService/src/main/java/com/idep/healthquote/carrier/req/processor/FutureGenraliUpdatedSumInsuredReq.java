package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.util.HealthQuoteConstants;

public class FutureGenraliUpdatedSumInsuredReq implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FutureGenraliUpdatedSumInsuredReq.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception
	{
		try{
		String mapperRes = exchange.getIn().getBody(String.class);
		JsonNode mapperReqNode = this.objectMapper.readTree(mapperRes);
		JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST).toString());
		String SumInsured = mapperReqNode.findValue("PHK6").asText();
		String carrierQuoteId = mapperReqNode.findValue("PHA2").asText();
		((ObjectNode)requestDocNode.findValue("productInfo")).put("sumInsured", SumInsured);
		((ObjectNode)requestDocNode.findValue("productInfo")).put("carrierQuoteId", carrierQuoteId);
		log.info("SumInsured updated in property request : "+requestDocNode);
		 exchange.setProperty(HealthQuoteConstants.CARRIER_INPUT_REQUEST,objectMapper.writeValueAsString(requestDocNode));
		 exchange.getIn().setBody(objectMapper.writeValueAsString(mapperReqNode));
		}catch(Exception e){
		log.error("Exception at CarrierUpdatedSumInsured : ",e);	
		}
	}

}
