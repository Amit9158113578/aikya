 package com.idep.proposal.impl.service;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.util.Utils;
 import java.io.IOException;
 import org.apache.log4j.Logger;
 
 public class CarPolicyCreatorImpl {
   ObjectMapper objectMapper = new ObjectMapper();
   
   public String createCarPolicy(String policy) {
     JsonNode reqNode = null;
     try {
       reqNode = this.objectMapper.readTree(policy);
       return reqNode.toString();
     } catch (Exception e) {
       return reqNode.toString();
     } 
   }
   public String sendMessage(String proposal) {
     return proposal;
   }
   public String policyResponse(String response) throws JsonProcessingException, IOException {
     Logger log = Logger.getLogger(CarPolicyCreatorImpl.class);
     try {
       JsonNode responseNode = Utils.mapper.readTree(response);
       if (responseNode.get("responseCode").asText().equals(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText())) {
         ObjectNode createObjectNode = Utils.mapper.createObjectNode();
         log.info("responseNode :" + responseNode);
         if (responseNode.get("data").has("policyNo"))
           createObjectNode.put("policyNo", responseNode.get("data").get("policyNo").asText()); 
         if (responseNode.get("data").has("policyNumber"))
           createObjectNode.put("policyNo", responseNode.get("data").get("policyNumber").asText()); 
         createObjectNode.put("proposalId", responseNode.get("data").get("proposalId").asText());
         ((ObjectNode)responseNode).put("data", (JsonNode)createObjectNode);
         log.info("responseNode end :" + responseNode);
         return Utils.mapper.writeValueAsString(responseNode);
       } 
     } catch (Exception e) {
       e.printStackTrace();
     } 
     return response;
   }
 }


