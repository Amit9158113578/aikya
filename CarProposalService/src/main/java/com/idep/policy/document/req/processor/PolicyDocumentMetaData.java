 package com.idep.policy.document.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import java.io.IOException;
 
 public class PolicyDocumentMetaData {
   CBService policyTransService = CBInstanceProvider.getPolicyTransInstance();
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   public JsonObject createMetaData(JsonNode proposalDoc, String proposalNo, String PolicyNumber) throws JsonProcessingException, IOException {
     JsonObject metaDataNode = JsonObject.create();
     if (proposalDoc == null) {
       proposalDoc = this.objectMapper.readTree(((JsonObject)this.policyTransService.getDocBYId(proposalNo).content()).toString());
       if (PolicyNumber != null && PolicyNumber.equalsIgnoreCase("")) {
         metaDataNode.put("policyNumber", PolicyNumber);
       } else {
         metaDataNode.put("policyNumber", proposalDoc.findValue("policyNo").asText());
       } 
       metaDataNode.put("customerId", proposalDoc.get("proposalId").asText());
       metaDataNode.put("emailId", proposalDoc.get("emailId").asText());
       metaDataNode.put("mobileNumber", proposalDoc.get("mobile").asText());
     } else {
       if (PolicyNumber != null && PolicyNumber.equalsIgnoreCase("")) {
         metaDataNode.put("policyNumber", PolicyNumber);
       } else {
         metaDataNode.put("policyNumber", proposalDoc.findValue("policyNo").asText());
       } 
       metaDataNode.put("customerId", proposalDoc.get("proposalId").asText());
       metaDataNode.put("emailId", proposalDoc.get("emailId").asText());
       metaDataNode.put("mobileNumber", proposalDoc.get("mobileNumber").asText());
       metaDataNode.put("policyBond", "Car Policy");
     } 
     return metaDataNode;
   }
 }


