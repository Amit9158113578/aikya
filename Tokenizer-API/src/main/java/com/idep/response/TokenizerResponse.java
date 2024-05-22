package com.idep.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TokenizerResponse
{
  public static String createResponse(int responseCode, String message, JsonNode node)
  {
    JsonNodeFactory factory = new JsonNodeFactory(true);
    ObjectNode responseNode = new ObjectNode(factory);
    responseNode.put("responseCode", responseCode);
    responseNode.put("message", message);
    responseNode.put("data", node);
    
    return responseNode.toString();
  }
}
