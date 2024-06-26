 package com.idep.soap.quote;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.soap.util.SoapUtils;
 import java.text.DecimalFormat;
 import org.apache.log4j.Logger;
 
 public class IDVRangeCalculatorProcessor {
   static Logger log = Logger.getLogger(IDVRangeCalculatorProcessor.class.getName());
   
   JsonObject idvConfig = null;
   
   public JsonNode process(JsonNode inputRequest, JsonNode responseNode) {
     try {
       if (this.idvConfig == null) {
         CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
         this.idvConfig = (JsonObject)serverConfig.getDocBYId("BikeCarrierIDVCalcConfig").content();
       } 
       String carrierId = responseNode.findValue("carrierId").asText();
       double idv = responseNode.findValue("insuredDeclareValue").asDouble();
       JsonNode vehicleInfo = inputRequest.get("vehicleInfo");
       double minIdvValue = 0.0D;
       double maxIdvValue = 0.0D;
       log.info("inputRequest:" + inputRequest);
       log.info("responseNode:" + responseNode);
       if (vehicleInfo.get("IDV").asInt() > 0) {
         String quoteIDValue = vehicleInfo.get("best_quote_id").textValue();
         log.info("best_quote_id :" + quoteIDValue);
         JsonDocument quoteDocument = SoapUtils.quoteData.getDocBYId(quoteIDValue);
         if (quoteDocument != null) {
           JsonNode quoteNode = SoapUtils.objectMapper.readTree(((JsonObject)quoteDocument.content()).toString());
           ArrayNode carQuoteProductResList = (ArrayNode)quoteNode.get("bikeQuoteResponse");
           log.info("carQuoteProductResList :" + carQuoteProductResList);
           if (carQuoteProductResList == null) {
             carQuoteProductResList = (ArrayNode)quoteNode.get("carQuoteResponse");
             log.info("carQuoteProductResList again:" + carQuoteProductResList);
           } 
           for (JsonNode carQuoteResponse : carQuoteProductResList) {
             try {
               log.info("carQuoteResponse :" + carQuoteResponse);
               log.info("inputRequest :" + inputRequest);
               log.info("responseNode :" + responseNode);
               if (carQuoteResponse.get("carrierId").intValue() == inputRequest.get("productInfo").get("carrierId").intValue()) {
                 ((ObjectNode)responseNode.get("data").get("quotes").get(0)).put("minIdvValue", carQuoteResponse.get("minIdvValue").doubleValue());
                 ((ObjectNode)responseNode.get("data").get("quotes").get(0)).put("maxIdvValue", carQuoteResponse.get("maxIdvValue").doubleValue());
               } 
             } catch (Exception e) {
               log.error("error in best_quote_id min and mix idv calculation");
             } 
           } 
           responseNode = process(responseNode);
           return responseNode;
         } 
       } else if (this.idvConfig.containsKey(carrierId)) {
         log.info("carrierId :" + carrierId);
         log.info("idvConfig :" + this.idvConfig);
         JsonNode idvCalcConfig = SoapUtils.objectMapper.readTree(this.idvConfig.get(carrierId).toString());
         JsonNode idvCalcPolicyType = idvCalcConfig.get(inputRequest.findValue("policyType").asText());
         int minidvPerc = idvCalcPolicyType.get("minusIDVPercVehicle").asInt();
         int maxidvPerc = idvCalcPolicyType.get("plusIDVPercVehicle").asInt();
         minIdvValue = (100 - minidvPerc) * idv / 100.0D;
         maxIdvValue = (100 + maxidvPerc) * idv / 100.0D;
         ((ObjectNode)responseNode.get("data").get("quotes").get(0)).put("minIdvValue", minIdvValue);
         ((ObjectNode)responseNode.get("data").get("quotes").get(0)).put("maxIdvValue", maxIdvValue);
         log.info("responseNode after min max idv:" + responseNode);
         responseNode = process(responseNode);
       } else {
         responseNode = process(responseNode);
         return responseNode;
       } 
     } catch (Exception e) {
       e.printStackTrace();
       log.error("error in calculate min and max idv for carrier :");
     } 
     return responseNode;
   }
   
   public JsonNode process(JsonNode quoteResultNode) throws Exception {
     double basicCoverage = 0.0D;
     try {
       JsonNode quoteResult = quoteResultNode.get("data").get("quotes").get(0);
       if (quoteResult.has("odpremium") && quoteResult.has("tppremium")) {
         if (quoteResult.has("paidDriverCover")) {
           basicCoverage = quoteResult.get("odpremium").asDouble() + quoteResult.get("tppremium").asDouble() + quoteResult.get("paidDriverCover").asDouble();
         } else {
           basicCoverage = quoteResult.get("odpremium").asDouble() + quoteResult.get("tppremium").asDouble();
         } 
         DecimalFormat formatting = new DecimalFormat("##.00");
         ((ObjectNode)quoteResult).put("basicCoverage", formatting.format(basicCoverage));
       } else {
         log.error("odpremium, tppremium or paidDriverCover missing in response : " + quoteResult);
       } 
       ((ObjectNode)quoteResult).put("totalDiscountAmount", TotalDiscountAmount(quoteResult));
       if (quoteResult.has("ridersList"))
         ((ObjectNode)quoteResult).put("totalRiderAmount", totalRiderAmount(quoteResult)); 
       ((ObjectNode)quoteResultNode.get("data").get("quotes").get(0)).putAll((ObjectNode)quoteResult);
     } catch (JsonParseException e) {
       log.error(" received response is not appropriate : ", (Throwable)e);
     } catch (NullPointerException e) {
       log.error("unable to fetch quote result : ", e);
     } catch (Exception e) {
       log.error("Exception at BasicCoverageCalculation : ", e);
     } 
     return quoteResultNode;
   }
   
   public String TotalDiscountAmount(JsonNode quoteResult) throws Exception {
     double totalDiscountAmount = 0.0D;
     String totalDiscountAmountStr = null;
     try {
       if (quoteResult.has("discountList"))
         for (JsonNode discountNode : quoteResult.get("discountList"))
           totalDiscountAmount += discountNode.get("discountAmount").asDouble();  
       DecimalFormat formatting = new DecimalFormat("##.00");
       totalDiscountAmountStr = formatting.format(totalDiscountAmount);
     } catch (Exception e) {
       log.error("unable to calculate totalDiscountAmount ", e);
     } 
     return totalDiscountAmountStr;
   }
   
   public String totalRiderAmount(JsonNode quoteResult) throws Exception {
     double totalRiderAmount = 0.0D;
     String totalRiderAmountStr = null;
     try {
       if (quoteResult.has("ridersList") && quoteResult.get("ridersList").size() > 0) {
         ArrayNode createArrayNode = SoapUtils.objectMapper.createArrayNode();
         log.info("calculating totalRiderAmount");
         for (JsonNode riderNode : quoteResult.get("ridersList")) {
           if (!riderNode.isNull()) {
             createArrayNode.add(riderNode);
             if (riderNode.has("riderValue"))
               totalRiderAmount += Double.parseDouble(riderNode.get("riderValue").asText()); 
           } 
         } 
         log.info("createArrayNode :" + createArrayNode);
         ((ObjectNode)quoteResult).put("ridersList", (JsonNode)createArrayNode);
         if (quoteResult.get("carrierId").asInt() == 53) {
           int totalnet = (int)(quoteResult.get("netPremium").asInt() + totalRiderAmount);
           int servicetax = (int)Math.round(totalnet * 0.18D);
           int totalgross = totalnet + servicetax;
           ((ObjectNode)quoteResult).put("serviceTax", servicetax);
           ((ObjectNode)quoteResult).put("netPremium", totalnet);
           ((ObjectNode)quoteResult).put("grossPremium", totalgross);
         } 
       } 
       DecimalFormat formatting = new DecimalFormat("##.00");
       totalRiderAmountStr = formatting.format(totalRiderAmount);
     } catch (Exception e) {
       log.error("unable to calculate totalDiscountAmount ", e);
     } 
     return totalRiderAmountStr;
   }
 }


