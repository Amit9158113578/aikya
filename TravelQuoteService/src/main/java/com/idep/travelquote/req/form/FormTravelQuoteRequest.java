package com.idep.travelquote.req.form;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author akash.kumawat
 *
 */
public class FormTravelQuoteRequest {
	Logger log = Logger.getLogger(FormTravelQuoteRequest.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	int carrierId = 0;
	
	public String formRequest(JsonNode quoteParamNode,JsonNode product ){
		String quoteRequestString = "";
		try{
			 this.carrierId = product.get(TravelQuoteConstants.DROOLS_CARRIERID).intValue();
			((ObjectNode)quoteParamNode).put(TravelQuoteConstants.DROOLS_CARRIER, product.get(TravelQuoteConstants.DROOLS_CARRIER_NAME).asText());
			((ObjectNode)quoteParamNode).put(TravelQuoteConstants.DROOLS_CARRIERID, this.carrierId);
			((ObjectNode)quoteParamNode).put(TravelQuoteConstants.DROOLS_INSURER_INDEX, product.get(TravelQuoteConstants.DROOLS_INSURER_INDEX).doubleValue());
			quoteRequestString = quoteRequestString + quoteParamNode.toString() + ",";
			quoteRequestString = quoteRequestString.substring(0, quoteRequestString.length() - 1);
			quoteRequestString = TravelQuoteConstants.DROOLS_TRAVELQUOTE_REQUEST_PART1 + quoteRequestString + TravelQuoteConstants.DROOLS_TRAVELQUOTE_REQUEST_PART2;
			String quoteEngineRequest = TravelQuoteConstants.DROOLS_TRAVELQUOTE_REQUEST_PART3  + quoteRequestString + TravelQuoteConstants.DROOLS_TRAVELQUOTE_REQUEST_PART4 ;
			this.log.info("Generated request in FormTravelQuoteRequest : " + quoteEngineRequest);
			return quoteEngineRequest;
			
		}catch(Exception ex){
			this.log.error("Exception at FormTravelQuoteRequest for Travel : ", ex);
			return quoteRequestString;
		}
		
	}
}
