package com.idep.travelquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelQuoteExResHandler implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelQuoteExResHandler.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
        String carrierResponse=null;
		try
		 {
			  carrierResponse = exchange.getIn().getBody(String.class);
			
			 JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			 
			 JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.CARRIER_INPUT_REQUEST).toString());
			
			 JsonNode productInfo = requestDocNode.get(TravelQuoteConstants.PRODUCT_INFO);
		    
			
				 ((ObjectNode)requestDocNode).put(TravelQuoteConstants.CARRIER_RESPONSE, carrierResNode);
			
			 
			 exchange.setProperty(TravelQuoteConstants.CARRIER_REQ_MAP_CONF,TravelQuoteConstants.CARRIER_TRAVEL_RES_CONF+productInfo.get(TravelQuoteConstants.DROOLS_CARRIERID).intValue()+
					  "-"+productInfo.get(TravelQuoteConstants.DROOLS_PRODUCT_ID).intValue());
			 exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
			 
		 }
		 catch(Exception e)
		 {
			 log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+"|ERROR|"+" Exception at TravelQuoteExResHandler for response :"+carrierResponse);
			 throw new ExecutionTerminator();
		 }
		 
	 }
}