 package com.idep.proposal.exception.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.util.Utils;
 
 public class ExceptionResponse {
   ExtendedJsonNode obj;
   
   public ExceptionResponse() {
     try {
       this.obj = new ExtendedJsonNode((JsonNode)Utils.mapper.createObjectNode());
     } catch (Exception e) {
       e.printStackTrace();
     } 
   }
   
   public ExtendedJsonNode configDocMissing() throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ConfigDocMissingCode").asText());
     this.obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ConfigDocMissingMsg").asText());
     this.obj.put("data", "");
     return this.obj;
   }
   
   public ExtendedJsonNode configDocMissing(String message) throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ConfigDocMissingCode").asText());
     this.obj.put("message", message);
     this.obj.put("data", "");
     return this.obj;
   }
   
   public ExtendedJsonNode quoteInfoNotFound(String message) throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ConfigDocMissingCode").asText());
     this.obj.put("message", message);
     this.obj.put("data", "");
     return this.obj;
   }
   
   public ExtendedJsonNode failure() throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeFailure").asText());
     this.obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
     this.obj.put("data", "");
     return this.obj;
   }
   
   public ExtendedJsonNode failure(String message) throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeFailure").asText());
     this.obj.put("message", message);
     this.obj.put("data", "");
     return this.obj;
   }
   
   public ExtendedJsonNode invokeServiceDown() throws Exception {
     this.obj.put("responseCode", "P365RES103");
     this.obj.put("message", "invoke service down");
     this.obj.put("data", "error");
     return this.obj;
   }
   
   public ExtendedJsonNode properResponseNotFound(JsonNode responseNode) throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("properResponseNotReceivedCode").asText());
     this.obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("properResponseNotReceivedMsg").asText());
     this.obj.put("data", responseNode);
     return this.obj;
   }
   
   public ExtendedJsonNode parseException(String message) throws Exception {
     this.obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeFailure").asText());
     this.obj.put("message", message);
     this.obj.put("data", "");
     return this.obj;
   }
 }


