package com.idep.healthquote.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.healthquote.util.HealthQuoteConstants;

public class QuoteServiceResponse {
	
	
	 public static String createResponse(int responseCode, String message, JsonNode node)
	  {
	    JsonNodeFactory factory = new JsonNodeFactory(true);
	    ObjectNode responseNode = new ObjectNode(factory);
	    responseNode.put(HealthQuoteConstants.QUOTE_RES_CODE, responseCode);
	    responseNode.put(HealthQuoteConstants.QUOTE_RES_MSG, message);
	    responseNode.put(HealthQuoteConstants.QUOTE_RES_DATA, node);
	    
	    return responseNode.toString();
	  }

}
