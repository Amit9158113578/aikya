package com.idep.travelquote.res.transformer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TataAIGResTransformer implements Processor {

	ObjectMapper objectMapper=new ObjectMapper();
	Logger log = Logger.getLogger(TataAIGResTransformer.class.getName());
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String carrierResponse = exchange.getIn().getBody(String.class);
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			JsonNode uiInputRequest = (JsonNode) exchange.getProperty("quoteInputRequest");
			((ObjectNode)uiInputRequest).put("carrierResponse", carrierResNode);
			log.debug("Final Carrier UI Response"+ carrierResNode);
			exchange.getIn().setBody(uiInputRequest);
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.FUTUREGENRESTRANS+"|ERROR|"+" Exception at FutureGenResTransformer for response :",e);
			throw new ExecutionTerminator();
		}
		
	}
	
	
	
	
	
	
	
}
