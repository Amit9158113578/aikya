package com.idep.bikequote.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.util.BikeQuoteConstants;

public class QuoteServiceResponse {
  public static String createResponse(int responseCode, String message, JsonNode node) {
    JsonNodeFactory factory = new JsonNodeFactory(true);
    ObjectNode responseNode = new ObjectNode(factory);
    responseNode.put(BikeQuoteConstants.QUOTE_RES_CODE, responseCode);
    responseNode.put(BikeQuoteConstants.QUOTE_RES_MSG, message);
    responseNode.put(BikeQuoteConstants.QUOTE_RES_DATA, node);
    return responseNode.toString();
  }
}
