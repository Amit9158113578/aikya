package com.idep.restapi.utils;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.restapi.request.ConfigurationLoaderProcessor;
import org.apache.log4j.Logger;

public class ResponseMessageProcessor {
  public static JsonNode responseMsg = null;
  
  static {
    try {
      String responseMessages = ((JsonObject)RestAPIConstants.serverConfig.getDocBYId("ResponseMessages").content()).toString();
      responseMsg = RestAPIConstants.objectMapper.readTree(responseMessages);
    } catch (Exception e) {
      Logger.getLogger(ConfigurationLoaderProcessor.class.getName()).error("ResponseMessages Document Not Found ");
    } 
  }
  
  public static ObjectNode returnConfigDocResponse(int carrierId) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("ConfigDocMissingCode"));
    object.put("status", "Failure");
    object.put("message", responseMsg.get("ConfigDocMissingMsg"));
    object.put("data", responseMsg.get("ConfigDocMissingMsg"));
    object.put("carrierId", carrierId);
    return object;
  }
  
  public static ObjectNode returnConfigDocResponse(String message, int carrierId) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("ConfigDocMissingCode"));
    object.put("status", "Failure");
    object.put("message", message);
    object.put("data", message);
    object.put("carrierId", carrierId);
    return object;
  }
  
  public static ObjectNode returnFailedResponse(int carrierId) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("ResponseCodeFailure"));
    object.put("status", "Failure");
    object.put("message", responseMsg.get("failureMsg"));
    object.put("data", responseMsg.get("failureMsg"));
    object.put("carrierId", carrierId);
    return object;
  }
  
  public static ObjectNode returnFailedResponseWithData(int carrierId, JsonNode data) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("ResponseCodeFailure"));
    object.put("status", "Failure");
    object.put("message", responseMsg.get("failureMsg"));
    object.put("data", data);
    object.put("carrierId", carrierId);
    return object;
  }
  
  public static ObjectNode returnFailedResponse(String message, int carrierId) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("ResponseCodeFailure"));
    object.put("status", "Failure");
    object.put("message", message);
    object.put("data", message);
    object.put("carrierId", carrierId);
    return object;
  }
  
  public static ObjectNode returnDynamicResponse(String message, int carrierId) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get(String.valueOf(message) + "Code"));
    object.put("status", "Failure");
    object.put("message", responseMsg.get(String.valueOf(message) + "Msg"));
    object.put("data", responseMsg.get(String.valueOf(message) + "Msg"));
    object.put("carrierId", carrierId);
    return object;
  }
  
  public static ObjectNode returnDynamicResponse(String message) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("ResponseCodeFailure"));
    object.put("status", "Failure");
    object.put("message", message);
    object.put("data", message);
    return object;
  }
  
  public static ObjectNode returnNotValidcarrierResponse(int carrierId, JsonNode data) {
    ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
    object.put("responseCode", responseMsg.get("properResponseNotReceivedCode"));
    object.put("status", "Failure");
    object.put("message", responseMsg.get("properResponseNotReceivedMsg"));
    object.put("data", data);
    object.put("carrierId", carrierId);
    return object;
  }
}
