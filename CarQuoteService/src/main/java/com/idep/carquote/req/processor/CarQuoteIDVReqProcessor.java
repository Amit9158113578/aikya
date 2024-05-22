 package com.idep.carquote.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import org.apache.log4j.Logger;
 
 public class CarQuoteIDVReqProcessor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarQuoteIDVReqProcessor.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   JsonNode errorNode;
   
   String encQuoteId;
   
   CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
   
   public JsonNode process(JsonNode reqNode, double userIDV, JsonNode carQuoteResponses, JsonNode product) throws Exception {
     try {
       if (carQuoteResponses != null) {
         double systemIDV = 0.0D;
         this.log.info("userIDV :" + userIDV);
         this.log.info("carQuoteResponses :" + carQuoteResponses);
         this.log.info("product :" + product);
         for (JsonNode quoteResponse : carQuoteResponses) {
           if (product.get("carrierId").intValue() == quoteResponse.get("carrierId").intValue()) {
             if (userIDV <= quoteResponse.get("minIdvValue").asDouble()) {
               systemIDV = quoteResponse.get("minIdvValue").asLong();
             } else if (userIDV >= quoteResponse.get("maxIdvValue").asDouble()) {
               systemIDV = quoteResponse.get("maxIdvValue").asLong();
             } else if (userIDV >= quoteResponse.get("minIdvValue").asDouble() && userIDV <= quoteResponse
               .get("maxIdvValue").asDouble()) {
               systemIDV = userIDV;
             } 
             ((ObjectNode)reqNode.get("vehicleInfo")).put("IDV", systemIDV);
             if (28 == product.get("carrierId").intValue()) {
               this.log.info("reqNode :" + reqNode);
               
               this.log.info("vehicleInfo idv :" + reqNode.get("vehicleInfo").get("IDV"));
             } 
 
 
             
             return reqNode;
           } 
         } 
       } else {
         return reqNode;
       } 
     } catch (Exception e) {
       this.log.error("Exception at BikeQuoteIDVReqProcessor ", e);
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       objectNode.put("data", this.errorNode);
     } 
     return reqNode;
   }
   
   public ArrayNode getBestQuoteIdDetails(JsonNode reqNode) throws Exception {
     ArrayNode bikeQuoteResponses = null;
     try {
       if (reqNode.get("vehicleInfo").get("IDV").asInt() > 0 && reqNode
         .get("vehicleInfo").has("best_quote_id") && !reqNode.get("vehicleInfo").get("best_quote_id").asText().isEmpty()) {
         JsonDocument quoteDoc = this.quoteData.getDocBYId(reqNode.get("vehicleInfo").get("best_quote_id").textValue());
         if (quoteDoc == null)
           this.log.error("best_quote_id document details not found in database :" + reqNode.get("vehicleInfo").get("best_quote_id").textValue()); 
         bikeQuoteResponses = (ArrayNode)this.objectMapper.readTree(((JsonObject)quoteDoc.content()).toString()).get("carQuoteResponse");
       } 
     } catch (Exception e) {
       this.log.error("Exception at getBestQuoteIdDetails ", e);
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       objectNode.put("data", this.errorNode);
     } 
     return bikeQuoteResponses;
   }
 }


