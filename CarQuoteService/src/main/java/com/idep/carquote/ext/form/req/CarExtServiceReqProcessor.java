 package com.idep.carquote.ext.form.req;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarExtServiceReqProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarExtServiceReqProcessor.class.getName());
   
   JsonNode errorNode;
   
   SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
   
   public void process(Exchange exchange) throws JsonProcessingException, IOException {
     try {
       String quotedata = exchange.getIn().getBody().toString();
       JsonNode reqNode = this.objectMapper.readTree(quotedata);
       JsonNode productInfoNode = reqNode.get("productInfo");
       if (reqNode.get("quoteParam").has("riders")) {
         ArrayNode finalRiders = this.objectMapper.createArrayNode();
         ArrayNode selectedRiders = (ArrayNode)reqNode.get("quoteParam").get("riders");
         double vehicleAge = reqNode.get("quoteParam").get("vehicleAge").doubleValue();
         ObjectNode productriderList = this.objectMapper.createObjectNode();
         for (JsonNode priders : productInfoNode.get("riderDetails"))
           productriderList.put(priders.get("riderId").asText(), priders); 
         ObjectNode processedRiderList = this.objectMapper.createObjectNode();
         ArrayNode eligibleRiders = this.objectMapper.createArrayNode();
         ArrayNode nonEligibleRiders = this.objectMapper.createArrayNode();
         ArrayList<Integer> arr = new ArrayList<>();
         if (reqNode.findValue("carrierId").asInt() == 53) {
           for (JsonNode selRider : selectedRiders) {
             if (selRider.has("riderId"))
               arr.add(Integer.valueOf(selRider.get("riderId").asInt())); 
           } 
           if (arr.contains(Integer.valueOf(6)) || arr.contains(Integer.valueOf(8)) || arr.contains(Integer.valueOf(10)) || arr.contains(Integer.valueOf(24))) {
             if (!arr.contains(Integer.valueOf(6))) {
               ObjectNode newRiders = this.objectMapper.createObjectNode();
               newRiders.put("riderId", 6);
               newRiders.put("riderName", "Zero Depreciation cover");
               newRiders.put("riderAmount", 0);
               selectedRiders.add((JsonNode)newRiders);
             } 
             if (!arr.contains(Integer.valueOf(8))) {
               ObjectNode newRiders = this.objectMapper.createObjectNode();
               newRiders.put("riderId", 8);
               newRiders.put("riderName", "Engine Protector");
               newRiders.put("riderAmount", 0);
               selectedRiders.add((JsonNode)newRiders);
             } 
             if (!arr.contains(Integer.valueOf(9))) {
               ObjectNode newRiders = this.objectMapper.createObjectNode();
               newRiders.put("riderId", 9);
               newRiders.put("riderName", "24X7 Road Side Assistance");
               newRiders.put("riderAmount", 0);
               selectedRiders.add((JsonNode)newRiders);
             } 
             if (!arr.contains(Integer.valueOf(10))) {
               ObjectNode newRiders = this.objectMapper.createObjectNode();
               newRiders.put("riderId", 10);
               newRiders.put("riderName", "Invoice Cover");
               newRiders.put("riderAmount", 0);
               selectedRiders.add((JsonNode)newRiders);
             } 
             if (!arr.contains(Integer.valueOf(24))) {
               ObjectNode newRiders = this.objectMapper.createObjectNode();
               newRiders.put("riderId", 24);
               newRiders.put("riderName", "Consumables cover");
               newRiders.put("riderAmount", 0);
               selectedRiders.add((JsonNode)newRiders);
             } 
           } 
         } 
         for (JsonNode selRider : selectedRiders) {
           if (productriderList.has(selRider.get("riderId").asText())) {
             JsonNode prodRider = productriderList.get(selRider.get("riderId").asText());
             if (vehicleAge <= prodRider.get("allowedVehicleAge").doubleValue()) {
               eligibleRiders.add(selRider);
               continue;
             } 
             nonEligibleRiders.add(selRider);
             continue;
           } 
           nonEligibleRiders.add(selRider);
         } 
         for (JsonNode riderNode : eligibleRiders) {
           if (processedRiderList.has(riderNode.get("riderId").asText())) {
             this.log.info("rider skipped as it is already processed");
             continue;
           } 
           JsonNode prodRiders = productriderList.get(riderNode.get("riderId").asText());
           processedRiderList.put(riderNode.get("riderId").asText(), "Y");
           finalRiders.add(riderNode);
           ArrayNode dependentRiders = (ArrayNode)prodRiders.get("dependant");
           if (dependentRiders != null)
             for (JsonNode dRiders : dependentRiders) {
               if (processedRiderList.has(dRiders.get("riderId").asText())) {
                 this.log.info("dependent rider skipped as it is already processed");
                 continue;
               } 
               JsonNode prodRider = productriderList.get(dRiders.get("riderId").asText());
               if (vehicleAge <= prodRider.get("allowedVehicleAge").doubleValue()) {
                 ObjectNode newRiders = this.objectMapper.createObjectNode();
                 newRiders.put("riderId", dRiders.get("riderId").intValue());
                 newRiders.put("riderName", dRiders.get("riderName").textValue());
                 newRiders.put("riderAmount", 0);
                 processedRiderList.put(dRiders.get("riderId").asText(), "Y");
                 finalRiders.add((JsonNode)newRiders);
                 continue;
               } 
               nonEligibleRiders.add(dRiders);
             }  
         } 
         ((ObjectNode)reqNode.get("quoteParam")).put("riders", (JsonNode)finalRiders);
         ((ObjectNode)reqNode.get("quoteParam")).put("UIRiders", (JsonNode)eligibleRiders);
         ((ObjectNode)reqNode.get("quoteParam")).put("nonEligibleUIRiders", (JsonNode)nonEligibleRiders);
       } 
 
 
 
 
 
       
       exchange.setProperty("carrierInputRequest", this.objectMapper.writeValueAsString(reqNode));
       exchange.getIn().setHeader("reqFlag", "True");
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(reqNode));
     } catch (NullPointerException e) {
       this.log.error("NullPointerException at CarExtServiceReqProcessor : ", e);
       exchange.getIn().setHeader("reqFlag", "False");
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(objectNode);
     } catch (Exception e) {
       this.log.error("Exception at CarExtServiceReqProcessor : ", e);
       exchange.getIn().setHeader("reqFlag", "False");
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(objectNode);
     } 
   }
 }


