 package com.idep.PBRating.Car;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import java.text.DecimalFormat;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 
 
 
 
 
 
 public class CashLessGargeRating
 {
   public CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   public Logger log = Logger.getLogger(CashLessGargeRating.class.getName());
   public ObjectMapper objectMapper = new ObjectMapper();
   
   public JsonNode generateCashLessGargeRating(JsonNode requestNode, JsonNode param) {
     String documentId = null;
     double gargaeCount = 0.0D;
     double minimumRating = 0.0D;
     ArrayNode garageRating = this.objectMapper.createArrayNode();
     
     try {
       documentId = "PBRating-" + param.get("componentName").asText();
       JsonDocument componentConfig = this.serverConfig.getDocBYId(documentId);
       List<Map<String, Object>> garageCount = null;
       ArrayNode carrierWisefactor = this.objectMapper.createArrayNode();
       if (componentConfig != null) {
         if (param.has("minimumRating")) {
           minimumRating = param.get("minimumRating").asDouble();
         }
         JsonNode component = this.objectMapper.readTree(((JsonObject)componentConfig.content()).toString());
         if (component.has("query")) {
           String query = component.get("query").asText();
           if (component.has("paramList")) {
             ArrayNode queryParamList = (ArrayNode)component.get("paramList");
             for (JsonNode data : queryParamList) {
               if (data.has("default") && data.get("default").asText().equalsIgnoreCase("N")) {
                 query = query.replace(data.get("replaceKey").asText(), requestNode.findValue(data.get("requestField").asText()).asText());
               }
             } 
           } 
           this.log.info("param Object query : " + query);
           garageCount = this.serverConfig.executeQueryCouchDB(query);
         } 
         
         if (garageCount.size() > 0) {
           String garageDetailsInString = this.objectMapper.writeValueAsString(garageCount);
           JsonNode list = this.objectMapper.readTree(garageDetailsInString);
 
 
 
 
           
           for (JsonNode carrierWiseData : list) {
             if (carrierWiseData.get("garageCount").asInt() > 0)
             {
               gargaeCount += carrierWiseData.get("garageCount").asInt();
             }
           } 
           double avgDeviationFact = gargaeCount / garageCount.size();
           this.log.info("Garage Rating avgDeviationFact : " + avgDeviationFact);
 
 
           
           double maxDeviation = 0.0D;
           for (JsonNode carrierWiseData : list) {
             if (carrierWiseData.get("garageCount").asInt() > 0) {
               ObjectNode carrierNode = this.objectMapper.createObjectNode();
               carrierNode.put("carrierId", carrierWiseData.get("carrierId").asText());
               double deviationFact = Double.parseDouble((new DecimalFormat("##.##")).format(carrierWiseData.get("garageCount").asDouble() / avgDeviationFact));
               if (deviationFact > 0.0D) {
                 carrierNode.put("deviationFactor", deviationFact);
                 if (deviationFact > maxDeviation) {
                   maxDeviation = deviationFact;
                 }
               } 
               carrierWisefactor.add((JsonNode)carrierNode);
             } 
           } 
           this.log.info("Count : " + gargaeCount + " carrier Count : " + garageCount.size());
 
           
           for (JsonNode deviationList : carrierWisefactor) {
             
             ObjectNode deviationFactor = this.objectMapper.createObjectNode();
             double factor = deviationList.get("deviationFactor").asDouble() / maxDeviation;
             double actualDeviation = Double.parseDouble((new DecimalFormat("##.##")).format(factor * 5.0D));
             System.out.println("Actual Deviation : " + actualDeviation + " \t " + minimumRating);
             if (actualDeviation < minimumRating) {
               actualDeviation = minimumRating;
             }
             deviationFactor.put("carrierId", deviationList.get("carrierId").asInt());
             deviationFactor.put("rating", actualDeviation);
             garageRating.add((JsonNode)deviationFactor);
           } 
           this.log.info("Final Garage rateing List CarrierWise : " + garageRating);
         } 
       } else {
         this.log.error("unable to load document " + documentId);
       } 
     } catch (Exception e) {
       this.log.error("unabl to process request :   ", e);
       e.printStackTrace();
     } 
     return (JsonNode)garageRating;
   }
 }