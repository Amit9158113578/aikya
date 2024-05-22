package com.idep.PBRating.Health;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class CalculateHealthRating {
  public ObjectMapper objectMapper = new ObjectMapper();
  
  public CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public Logger log = Logger.getLogger(CalculateHealthRating.class.getName());
  
  static HashMap<String, String> cashlessRatingMap = new HashMap<>();
  
  static JsonNode cashlessHospitalArrayNode;
  
  static List<Map<String, Object>> productDataQuery;
  
  static {
    ObjectMapper mapper = new ObjectMapper();
    CBService server = CBInstanceProvider.getServerConfigInstance();
    CBService productData = CBInstanceProvider.getProductConfigInstance();
    Logger logger = Logger.getLogger(CalculateHealthRating.class.getName());
    JsonNode pbratingList = null;
    try {
      pbratingList = mapper.readTree(((JsonObject)server.getDocBYId("PBRatingComponent").content()).toString());
    } catch (IOException e1) {
      e1.printStackTrace();
    } 
    List<Map<String, Object>> executeQuery = server.executeQuery(pbratingList.get("hospitalRatingQuery").textValue());
    productDataQuery = productData.executeQuery(pbratingList.get("healthProductQuery").textValue());
    logger.info("productDataQuery :" + productDataQuery);
    try {
      cashlessHospitalArrayNode = mapper.readTree(mapper.writeValueAsString(executeQuery));
      for (JsonNode cashlessHospital : cashlessHospitalArrayNode)
        cashlessRatingMap.put(cashlessHospital.get("docId").textValue(), cashlessHospital.get("hospitalcount").asText()); 
      logger.info("cashlessRatingMap :" + cashlessRatingMap);
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public JsonNode generateHealthRating(String request) {
    ArrayNode carConfig = null;
    ArrayNode healthConfig = null;
    ObjectNode carRating = this.objectMapper.createObjectNode();
    try {
      JsonNode requestNode = this.objectMapper.readTree(request);
      this.log.info("requestNode PBRating service :" + requestNode);
      JsonDocument pbratingList = this.serverConfig.getDocBYId("PBRatingComponent");
      if (pbratingList != null) {
        JsonNode paramList = this.objectMapper.readTree(((JsonObject)pbratingList.content()).toString());
        if (paramList.has("health"))
          healthConfig = (ArrayNode)paramList.get("health"); 
        for (JsonNode param : healthConfig) {
          if (param.has("componentName") && param.has("calculationAt") && 
            param.get("calculationAt").asText().equalsIgnoreCase("service") && 
            param.get("componentName").asText().equalsIgnoreCase("cashlessHospital")) {
            CalculateHealthRating clg = new CalculateHealthRating();
            JsonNode hospitalRating = clg.generateCashLessHospitalRating(requestNode, param);
            if (hospitalRating != null && hospitalRating.size() > 0)
              carRating.put("hosptalRating", hospitalRating); 
          } 
          this.log.info("Calclauted Health Rating : " + carRating);
        } 
      } 
    } catch (Exception e) {
      this.log.error("unabl to process request :   ", e);
      e.printStackTrace();
    } 
    return (JsonNode)carRating;
  }
  
  public JsonNode generateCashLessHospitalRating(JsonNode requestNode, JsonNode param) {
    String documentId = null;
    double hospCount = 0.0D;
    double minimumRating = 0.0D;
    ArrayNode hospitalRating = this.objectMapper.createArrayNode();
    try {
      documentId = "PBRating-" + param.get("componentName").asText();
      JsonDocument componentConfig = this.serverConfig.getDocBYId(documentId);
      ArrayNode hospitalCount = this.objectMapper.createArrayNode();
      ArrayNode carrierWisefactor = this.objectMapper.createArrayNode();
      if (componentConfig != null) {
        if (param.has("minimumRating"))
          minimumRating = param.get("minimumRating").asDouble(); 
        JsonNode carrierIdArrayNode = this.objectMapper.readTree(this.objectMapper.writeValueAsString(productDataQuery));
        String city = requestNode.findValue("city").asText().toUpperCase();
        for (JsonNode productDataNode : carrierIdArrayNode) {
          String findCityKey = String.valueOf(city) + "-" + productDataNode.get("carrierId").asText();
          this.log.info("findCityKey :" + findCityKey);
          if (cashlessRatingMap.get(findCityKey) != null) {
            ObjectNode createObjectNode = this.objectMapper.createObjectNode();
            String cityRatingCount = cashlessRatingMap.get(findCityKey);
            createObjectNode.put("hospitalCount", cityRatingCount);
            createObjectNode.put("carrierId", productDataNode.get("carrierId").asText());
            hospitalCount.add((JsonNode)createObjectNode);
            continue;
          } 
          this.log.error("hospital city count not found for city :" + city + " and carrierId :" + productDataNode.get("carrierId").asText() + " key :" + findCityKey);
        } 
      } 
      this.log.info("hospital count size :" + hospitalCount.size());
      if (hospitalCount.size() > 0) {
        String hospitalCountInString = this.objectMapper.writeValueAsString(hospitalCount);
        JsonNode list = this.objectMapper.readTree(hospitalCountInString);
        for (JsonNode carrierWiseData : list) {
          if (carrierWiseData.get("hospitalCount").asInt() > 0)
            hospCount += carrierWiseData.get("hospitalCount").asInt(); 
        } 
        double avgDeviationFact = hospCount / hospitalCount.size();
        this.log.info("Garage Rating avgDeviationFact : " + avgDeviationFact);
        double maxDeviation = 0.0D;
        for (JsonNode carrierWiseData : list) {
          if (carrierWiseData.get("hospitalCount").asInt() > 0) {
            ObjectNode carrierNode = this.objectMapper.createObjectNode();
            carrierNode.put("carrierId", carrierWiseData.get("carrierId").asText());
            double deviationFact = Double.parseDouble((new DecimalFormat("##.##")).format(carrierWiseData.get("hospitalCount").asDouble() / avgDeviationFact));
            if (deviationFact > 0.0D) {
              carrierNode.put("deviationFactor", deviationFact);
              if (deviationFact > maxDeviation)
                maxDeviation = deviationFact; 
            } 
            carrierWisefactor.add((JsonNode)carrierNode);
          } 
        } 
        this.log.info("Count : " + hospCount + " carrier Count : " + hospitalCount.size());
        for (JsonNode deviationList : carrierWisefactor) {
          ObjectNode deviationFactor = this.objectMapper.createObjectNode();
          double factor = deviationList.get("deviationFactor").asDouble() / maxDeviation;
          double actualDeviation = Double.parseDouble((new DecimalFormat("##.##")).format(factor * 5.0D));
          System.out.println("Actual Deviation : " + actualDeviation + " \t " + minimumRating);
          if (actualDeviation < minimumRating)
            actualDeviation = minimumRating; 
          deviationFactor.put("carrierId", deviationList.get("carrierId").asInt());
          deviationFactor.put("rating", actualDeviation);
          hospitalRating.add((JsonNode)deviationFactor);
        } 
        this.log.info("Final hospital rateing List CarrierWise : " + hospitalRating);
      } else {
        this.log.error("unable to load document " + documentId);
      } 
      return (JsonNode)hospitalRating;
    } catch (Exception e) {
      this.log.error("unabl to process request :   ", e);
      e.printStackTrace();
      return (JsonNode)hospitalRating;
    } 
  }
}
