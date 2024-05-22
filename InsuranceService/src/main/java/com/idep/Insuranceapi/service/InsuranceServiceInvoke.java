 package com.idep.Insuranceapi.service;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class InsuranceServiceInvoke implements Processor {
   private static final Logger log = Logger.getLogger(InsuranceServiceInvoke.class.getName());
   
   private static final ObjectMapper mapper = new ObjectMapper();
   
   private final CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   public void process(Exchange exchange) {
     ObjectNode finalResultNode = mapper.createObjectNode();
     try {
       String selectedCarrierId = extractSelectedCarrierId(exchange);
       if (selectedCarrierId == null)
         throw new IllegalArgumentException("Selected carrier ID not found in the payload"); 
       log.info("Selected Carrier ID: " + selectedCarrierId);
       List<Map<String, String>> insurerIdsWithNames = getSelectedCarrierIds(selectedCarrierId);
       log.info("Insurer IDs with Names: " + insurerIdsWithNames);
       ArrayNode insuranceMappingArrayNode = mapper.createArrayNode();
       for (Map<String, String> insurer : insurerIdsWithNames) {
         ObjectNode insuranceMappingNode = mapper.createObjectNode();
         insuranceMappingNode.put("insurerId", insurer.get("insurerId"));
         insuranceMappingNode.put("InsurerCode", insurer.get("InsurerCode"));
         insuranceMappingNode.put("insurerName", insurer.get("insurerName"));
         
         insuranceMappingArrayNode.add((JsonNode)insuranceMappingNode);
       } 
       finalResultNode.set("InsuranceMappingDocuments", (JsonNode)insuranceMappingArrayNode);
       finalResultNode.put("responseCode", "P365RES100");
       finalResultNode.put("message", "response");
       exchange.getIn().setBody(finalResultNode);
     } catch (Exception e) {
       log.error("Error in Insurance service invoke class", e);
       finalResultNode.put("responseCode", "P365RES101");
       finalResultNode.put("message", "error");
       finalResultNode.put("errorDetails", e.getMessage());
       exchange.getIn().setBody(finalResultNode);
     } 
   }
   
   private String extractSelectedCarrierId(Exchange exchange) {
     try {
       String requestBody = (String)exchange.getIn().getBody(String.class);
       JsonNode bodyNode = mapper.readTree(requestBody);
       JsonNode selectedCarrierNode = bodyNode.get("selectedCarrier");
       if (selectedCarrierNode == null || selectedCarrierNode.isNull())
         throw new IllegalArgumentException("Selected carrier ID not found in the payload"); 
       return selectedCarrierNode.asText();
     } catch (Exception e) {
       log.error("Error extracting selected carrier ID from the payload", e);
       return null;
     } 
   }
   private List<Map<String, String>> getSelectedCarrierIds(String selectedCarrierId) {
     try {
       String query;
       int carrierId = Integer.parseInt(selectedCarrierId);
       
       if (carrierId == 25 ) {
         query = "SELECT carrierId AS insurerId, insurerName, InsurerCode FROM ServerConfig WHERE documentType = 'InsuranceMapping' AND carrierId = " + carrierId + " ORDER BY insurerName ASC";
       } 
      //  else if (carrierId == 28 ) {
      //    query = "SELECT InsurerId AS insurerId, insurerName, InsurerCode FROM ServerConfig WHERE documentType = 'InsuranceMapping' AND carrierId = " + carrierId + " ORDER BY insurerName ASC";
      //  } 
       else{
         query = "SELECT InsurerCode as insurerId, insurerName,InsurerCode FROM ServerConfig WHERE documentType = 'InsuranceMapping' AND carrierId = " + carrierId + " ORDER BY insurerName ASC";
       } 
       List<Map<String, Object>> executeQuery = this.serverConfig.executeQuery(query);
       Set<String> uniqueInsurerCombinations = new HashSet<>();
       List<Map<String, String>> insurerIdsWithNames = new ArrayList<>();
       for (Map<String, Object> document : executeQuery) {
         String insurerId = String.valueOf(document.get("insurerId"));
         String InsurerCode = String.valueOf(document.get("InsurerCode"));
         String insurerName = (String)document.get("insurerName");
         
         String uniqueKey = String.valueOf(insurerId) + "-" + insurerName;
         if (!uniqueInsurerCombinations.contains(uniqueKey)) {
           Map<String, String> insurerMap = new HashMap<>();
           insurerMap.put("insurerId", insurerId);
           insurerMap.put("InsurerCode", InsurerCode);
           insurerMap.put("insurerName", insurerName);
           insurerIdsWithNames.add(insurerMap);
           uniqueInsurerCombinations.add(uniqueKey);
         } 
       } 
       return insurerIdsWithNames;
     } catch (NumberFormatException e) {
       log.error("Error parsing selected carrier ID to integer", e);
       return Collections.emptyList();
     } catch (Exception e) {
       log.error("Error fetching insurer IDs for selected carrier", e);
       return Collections.emptyList();
     } 
   }
 }


