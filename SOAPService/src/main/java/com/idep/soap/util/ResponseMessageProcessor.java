 package com.idep.soap.util;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.soap.service.ConfigurationLoaderProcessor;
 import org.apache.log4j.Logger;
 
 public class ResponseMessageProcessor {
   public static JsonNode responseMsg = null;
   
   static {
     try {
       String responseMessages = ((JsonObject)SoapUtils.serverConfig.getDocBYId("ResponseMessages").content()).toString();
       responseMsg = SoapUtils.objectMapper.readTree(responseMessages);
     } catch (Exception e) {
       Logger.getLogger(ConfigurationLoaderProcessor.class.getName()).error("ResponseMessages Document Not Found ");
     } 
   }
   
   public static ObjectNode returnConfigDocResponse(int carrierId) {
     ObjectNode object = SoapUtils.objectMapper.createObjectNode();
     object.put("responseCode", responseMsg.get("ConfigDocMissingCode"));
     object.put("status", "failure");
     object.put("message", responseMsg.get("ConfigDocMissingMsg"));
     object.put("data", "");
     object.put("carrierId", carrierId);
     return object;
   }
   
   public static ObjectNode returnConfigDocResponse(String message, int carrierId) {
     ObjectNode object = SoapUtils.objectMapper.createObjectNode();
     object.put("responseCode", responseMsg.get("ConfigDocMissingCode"));
     object.put("status", "failure");
     object.put("message", message);
     object.put("data", "");
     object.put("carrierId", carrierId);
     return object;
   }
   
   public static ObjectNode returnFailedResponse(int carrierId) {
     ObjectNode object = SoapUtils.objectMapper.createObjectNode();
     object.put("responseCode", responseMsg.get("ResponseCodeFailure"));
     object.put("status", "failure");
     object.put("message", responseMsg.get("failureMsg"));
     object.put("data", "");
     object.put("carrierId", carrierId);
     return object;
   }
   
   public static ObjectNode returnFailedResponse(String message, int carrierId) {
     ObjectNode object = SoapUtils.objectMapper.createObjectNode();
     object.put("responseCode", responseMsg.get("ResponseCodeFailure"));
     object.put("status", "failure");
     object.put("message", message);
     object.put("data", "");
     object.put("carrierId", carrierId);
     return object;
   }
   
   public static ObjectNode returnCarrierFailedResponse(JsonNode message, int carrierId) {
     ObjectNode object = SoapUtils.objectMapper.createObjectNode();
     object.put("responseCode", responseMsg.get("properResponseNotReceivedCode"));
     object.put("status", "failure");
     object.put("message", message);
     object.put("data", "");
     object.put("carrierId", carrierId);
     return object;
   }
   
   public static ObjectNode returnDynamicResponse(String message, int carrierId) {
     ObjectNode object = SoapUtils.objectMapper.createObjectNode();
     object.put("responseCode", responseMsg.get(String.valueOf(String.valueOf(message)) + "Code"));
     object.put("status", "failure");
     object.put("message", responseMsg.get(String.valueOf(String.valueOf(message)) + "Msg"));
     object.put("data", "");
     object.put("carrierId", carrierId);
     return object;
   }
 }


