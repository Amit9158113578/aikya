 package com.idep.Insuranceapi.service;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class InsuranceFilter
 {
   private static final ObjectMapper mapper = new ObjectMapper();
   
   private ObjectNode filterInsuranceData(List<Map<String, Object>> allInsuranceData, String selectedCarrierId, List<String> selectedInsurerIds) {
     ObjectNode responseObject = mapper.createObjectNode();
     ArrayNode insurerNamesArray = mapper.createArrayNode();
     
     try {
       boolean isCarrierPresent = false;
       
       for (Map<String, Object> insurance : allInsuranceData) {
         String carrierId = String.valueOf(insurance.get("carrierId"));
         String carrierName = (String)insurance.get("carrierName");
         String insurerId = String.valueOf(insurance.get("insurerId"));
         
         log.info("Processing insurance document: Carrier ID - {}, Carrier Name - {}, Insurer ID - {}", new Object[] { carrierId, carrierName, insurerId });
 
         
         if (selectedCarrierId.equals(carrierId) && selectedInsurerIds.contains(insurerId)) {
           log.info("Found matching document: Carrier ID - {}, Carrier Name - {}, Insurer ID - {}", new Object[] { carrierId, carrierName, insurerId });
           ObjectNode insurerNode = mapper.createObjectNode();
           insurerNode.put("carrierName", carrierName);
           insurerNode.put("insurerId", insurerId);
           insurerNamesArray.add((JsonNode)insurerNode);
           isCarrierPresent = true;
         } 
       } 
       
       responseObject.put("responseCode", "P365RES100");
       responseObject.put("message", "response");
       responseObject.set("InsuranceMappingDocuments", (JsonNode)insurerNamesArray);
       
       if (!isCarrierPresent) {
         log.info("Carrier with the specified carrierId and insurer IDs not found in the database", new Object[0]);
       }
     } catch (Exception e) {
       log.error("Error filtering insurance data", e);
     } 
     
     log.info("Filtering insurance data completed", new Object[0]);
     return responseObject;
   }
 
   
   public static void main(String[] args) {
     InsuranceFilter insuranceFilter = new InsuranceFilter();
 
     
     List<Map<String, Object>> allInsuranceData = new ArrayList<>();
 
     
     Map<String, Object> insurance1 = new HashMap<>();
     insurance1.put("carrierId", "47");
     insurance1.put("carrierName", "ICICI");
     insurance1.put("insurerId", "48");
     allInsuranceData.add(insurance1);
     
     Map<String, Object> insurance2 = new HashMap<>();
     insurance2.put("carrierId", "47");
     insurance2.put("carrierName", "Future");
     insurance2.put("insurerId", "45");
     allInsuranceData.add(insurance2);
     
     Map<String, Object> insurance3 = new HashMap<>();
     insurance3.put("carrierId", "47");
     insurance3.put("carrierName", "Reliance");
     insurance3.put("insurerId", "32");
     allInsuranceData.add(insurance3);
     
     Map<String, Object> insurance4 = new HashMap<>();
     insurance4.put("carrierId", "47");
     insurance4.put("carrierName", "Go digit");
     insurance4.put("insurerId", "17");
     allInsuranceData.add(insurance4);
     
     Map<String, Object> insurance5 = new HashMap<>();
     insurance5.put("carrierId", "47");
     insurance5.put("carrierName", "Reliance");
     insurance5.put("insurerId", "16");
     allInsuranceData.add(insurance5);
     
     String selectedCarrierId = "47";
 
 
     
     List<String> selectedInsurerIds = Arrays.asList(new String[] { "48", "45", "32", "17", "16" });
     
     ObjectNode filteredData = insuranceFilter.filterInsuranceData(allInsuranceData, selectedCarrierId, selectedInsurerIds);
     System.out.println(filteredData);
 
     
     System.out.println(filteredData);
   }
 
   
   private static class log
   {
     public static void info(String message, Object... params) {
       System.out.println(String.format(message, params));
     }
     
     public static void error(String message, Throwable throwable) {
       System.err.println(message);
       throwable.printStackTrace();
     }
   }
 }


