package com.idep.travelquote.response.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class QuoteServiceResponse{
	 public static String createResponse(int responseCode, String message, JsonNode node){
	    JsonNodeFactory factory = new JsonNodeFactory(true);
	    ObjectNode responseNode = new ObjectNode(factory);
	    responseNode.put(TravelQuoteConstants.QUOTE_RES_CODE, responseCode);
	    responseNode.put(TravelQuoteConstants.QUOTE_RES_MSG, message);
	    responseNode.put(TravelQuoteConstants.QUOTE_RES_DATA, node);
	    
	    return responseNode.toString();
	  }
}