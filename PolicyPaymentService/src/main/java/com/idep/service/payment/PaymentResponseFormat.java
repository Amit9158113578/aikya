 package com.idep.service.payment;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.JsonNodeFactory;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 public class PaymentResponseFormat
 {
   public static String createResponse(JsonNode paymentConstantNode, int responseCode, String message, JsonNode data) {
     JsonNodeFactory factory = new JsonNodeFactory(true);
     ObjectNode responseNode = new ObjectNode(factory);
     responseNode.put(paymentConstantNode.get("PAYMENT_RES_CODE").textValue(), responseCode);
     responseNode.put(paymentConstantNode.get("PAYMENT_RES_MSG").textValue(), message);
     responseNode.put(paymentConstantNode.get("PAYMENT_RES_DATA").textValue(), data);
     return responseNode.toString();
   }
 }


