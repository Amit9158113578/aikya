 package com.idep.PBRating.Car;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.PBRating.LoadComponentList;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import org.apache.log4j.Logger;

 
 
 public class CalculateCarRating
 {
   public CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   public Logger log = Logger.getLogger(LoadComponentList.class.getName());
   public ObjectMapper objectMapper = new ObjectMapper();
 
   
   public JsonNode generateCarRating(String request) {
     ArrayNode carConfig = null;
     ArrayNode healthConfig = null;
     ObjectNode carRating = this.objectMapper.createObjectNode();
     
     try {
       JsonNode requestNode = this.objectMapper.readTree(request);
       JsonDocument pbratingList = this.serverConfig.getDocBYId("PBRatingComponent");
       
       if (pbratingList != null) {
         
         JsonNode paramList = this.objectMapper.readTree(((JsonObject)pbratingList.content()).toString());
         
         if (paramList.has("car")) {
           carConfig = (ArrayNode)paramList.get("car");
         }
         
         for (JsonNode param : carConfig)
         {
           
           if (param.has("componentName") && param.has("calculationAt") && 
             param.get("calculationAt").asText().equalsIgnoreCase("service"))
           {
             if (param.get("componentName").asText().equalsIgnoreCase("cashlessGarages")) {
               CashLessGargeRating clg = new CashLessGargeRating();
               JsonNode garagerating = clg.generateCashLessGargeRating(requestNode, param);
               if (garagerating != null && garagerating.size() > 0) {
                 carRating.put("garageRating", garagerating);
               }
             }
           
           }
         }
       
       } 
     } catch (Exception e) {
       this.log.error("unabl to process request :   ", e);
       e.printStackTrace();
     } 
     return (JsonNode)carRating;
   }
 }