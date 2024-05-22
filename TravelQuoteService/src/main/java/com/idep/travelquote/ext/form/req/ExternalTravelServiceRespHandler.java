package com.idep.travelquote.ext.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;


public class ExternalTravelServiceRespHandler implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ExternalTravelServiceRespHandler.class.getName());
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		 try
		 {
			 String carrierResponse = exchange.getIn().getBody(String.class);
			 JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			 JsonNode requestDocNode = this.objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
			
			 JsonNode productInfo = requestDocNode.get(TravelQuoteConstants.PRODUCT_INFO);
			 ((ObjectNode)requestDocNode).put(TravelQuoteConstants.QUOTE_ID, exchange.getProperty(TravelQuoteConstants.QUOTE_ID).toString());
			 ((ObjectNode)requestDocNode).put("carrierResponse", carrierResNode.get("carrierResponse"));
			 ((ObjectNode)requestDocNode).put("requestType", TravelQuoteConstants.CARRIER_QUOTE_RESPONSE);
			 log.debug("requestDocNode in ExternalFutureTravelServiceRespHandler :"+requestDocNode);
		      exchange.getIn().setHeader("documentId", TravelQuoteConstants.CARRIER_QUOTE_RESPONSE+"-"+productInfo.get(TravelQuoteConstants.CARRIER_ID).intValue()+
					  "-"+productInfo.get(TravelQuoteConstants.PLANID).intValue());
		      log.debug("requestDocNode input to mapper: "+requestDocNode);
			  exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
		      
		 }
		 catch(Exception e)
		 {
			 this.log.error("Exception at ExternalServiceRespHandler : ", e);
			 throw new ExecutionTerminator();
		 }
		 
	 }
	

}
