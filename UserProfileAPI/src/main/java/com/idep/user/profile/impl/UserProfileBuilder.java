 package com.idep.user.profile.impl;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.user.profile.util.MD5Encryption;
 import org.apache.log4j.Logger;
 
 
 public class UserProfileBuilder
 {
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   Logger log = Logger.getLogger(UserProfileServices.class.getName());
   ObjectMapper objectMapper = new ObjectMapper();
   UserProfileServices profileServices = new UserProfileServices();
 
 
   
   public JsonNode buildUserProfile(JsonNode data) {
     try {
       ObjectNode userProfileResNode = this.objectMapper.createObjectNode();
 
 
       
       if (data.has("proposalId")) {
         
         JsonDocument proposalDocument = null;
         
         try {
           proposalDocument = this.transService.getDocBYId(data.get("proposalId").asText());
         }
         catch (Exception e) {
           
           this.log.error("failed to read proposal document : " + data.get("proposalId").asText(), e);
         } 
         if (proposalDocument != null) {
           
           JsonObject proposalData = (JsonObject)proposalDocument.content();
           JsonNode proposalDataNode = this.objectMapper.readTree(proposalData.toString());
           if (proposalDataNode.has("mobile")) {
             
             String mobile = proposalDataNode.get("mobile").asText();
 
             
             userProfileResNode.put("proposalId", data.get("proposalId").asText());
             if (data.has("policyNo"))
             {
               userProfileResNode.put("policyNo", data.get("policyNo").asText());
             }
 
 
 
             
             this.log.info("User Profile doc creation initiated");
             String uKey = createUserProfile(mobile, proposalDataNode);
             userProfileResNode.put("uKey", uKey);
 
 
 
             
             this.log.info("User Relation Mapping doc creation initiated");
             createUserRelationMapping(mobile, uKey);
 
 
 
 
             
             this.log.info("User Policy doc creation initiated");
             String pKey = createUserPolicyRecord(mobile, proposalDataNode);
             userProfileResNode.put("pKey", pKey);
             
             return (JsonNode)userProfileResNode;
           } 
 
           
           this.log.error("mobile field is missing in proposal document");
           return null;
         } 
 
 
 
         
         this.log.error("proposal document not found in DB: " + data.get("proposalId").asText());
         return null;
       } 
 
 
 
       
       this.log.error("proposalId is missing in request hence cannot proceed to create user profile: " + data);
       return null;
 
 
 
     
     }
     catch (Exception e) {
       
       this.log.error("Exception at UserProfileBuilder", e);
       return null;
     } 
   }
 
 
 
 
 
   
   private String createUserProfile(String mobile, JsonNode proposalDataNode) {
     try {
       ObjectNode userProfileNode = this.objectMapper.createObjectNode();
       userProfileNode.put("firstName", proposalDataNode.get("firstName").asText());
       userProfileNode.put("lastName", proposalDataNode.get("lastName").asText());
       userProfileNode.put("dateOfBirth", proposalDataNode.get("dateOfBirth").asText());
       userProfileNode.put("emailId", proposalDataNode.get("emailId").asText());
       userProfileNode.put("gender", proposalDataNode.get("gender").asText());
       userProfileNode.put("maritalStatus", proposalDataNode.get("maritalStatus").asText());
       
       if (proposalDataNode.has("aadharId"))
       {
         userProfileNode.put("aadharId", proposalDataNode.get("aadharId").asText());
       }
       if (proposalDataNode.has("PAN"))
       {
         userProfileNode.put("PAN", proposalDataNode.get("PAN").asText());
       }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
       
       String uKey = this.profileServices.updateUserProfile(mobile, userProfileNode);
       
       return uKey;
     
     }
     catch (Exception e) {
       
       this.log.error("Exception while creating user profile", e);
       return null;
     } 
   }
 
 
 
 
   
   private String createUserPolicyRecord(String mobile, JsonNode proposalDataNode) {
     try {
       String proposalSecretKey = proposalDataNode.get("proposalId").asText();
 
 
       
       String pKey = MD5Encryption.MD5(proposalSecretKey);
       
       int businessLineId = proposalDataNode.get("businessLineId").asInt();
       ObjectNode policyDocNode = null;
       
       if (businessLineId == 1) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("lifePolicyResponse");
       }
       else if (businessLineId == 2) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("bikePolicyResponse");
         policyDocNode.put("carrierVariantDisplayName", proposalDataNode.findValue("carrierVariantDisplayName"));
       
       }
       else if (businessLineId == 3) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("carPolicyResponse");
         policyDocNode.put("carrierVariantDisplayName", String.valueOf(proposalDataNode.findValue("make").asText()) + " " + proposalDataNode.findValue("model").asText() + " " + proposalDataNode.findValue("variant").asText() + " " + proposalDataNode.findValue("fuelType").asText());
       }
       else if (businessLineId == 4) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("healthPolicyResponse");
         policyDocNode.put("carrierVariantDisplayName", proposalDataNode.findValue("insuranceCompany"));
       }
       else if (businessLineId == 5) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("travelPolicyResponse");
       }
       else if (businessLineId == 7) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("homePolicyResponse");
       }
       else if (businessLineId == 8) {
         
         policyDocNode = (ObjectNode)proposalDataNode.get("personalAccidentPolicyResponse");
       }
       else {
         
         this.log.error("businessLineId not configured : " + businessLineId);
       } 
       
       policyDocNode.put("secretKey", pKey);
       policyDocNode.put("policyStartDate", proposalDataNode.get("policyStartDate"));
       if (proposalDataNode.has("userName"))
       {
         policyDocNode.put("userName", proposalDataNode.findValue("userName").asText());
       }
       if (proposalDataNode.has("agencyId"))
       {
         policyDocNode.put("agencyId", proposalDataNode.findValue("agencyId").asText());
       }
       if (proposalDataNode.has("requestSource"))
       {
         policyDocNode.put("requestSource", proposalDataNode.findValue("requestSource").asText());
       }
       if (proposalDataNode.has("firstName"))
       {
         policyDocNode.put("Name", String.valueOf(proposalDataNode.findValue("firstName").asText()) + " " + proposalDataNode.findValue("lastName").asText());
       }
       if (proposalDataNode.has("emailId"))
       {
         policyDocNode.put("emailId", proposalDataNode.findValue("emailId").asText());
       }
       if (proposalDataNode.findValue("insuranceCompany").asText() != null)
       {
         policyDocNode.put("insurerName", proposalDataNode.findValue("insuranceCompany").asText());
       }
       policyDocNode.put("policyExpiryDate", proposalDataNode.get("policyExpiryDate"));
       policyDocNode.put("totalPremium", proposalDataNode.get("totalPremium"));
       policyDocNode.put("policyActive", "Y");
       policyDocNode.put("policyDocumentLoc", "NA");
 
 
 
       
       if (policyDocNode.has("documentType"))
       {
         policyDocNode.remove("documentType");
       }
       if (policyDocNode.has("transactionStatusCode"))
       {
         policyDocNode.remove("transactionStatusCode");
       }
 
       
       this.profileServices.addPolicyRecord(mobile, policyDocNode);
       
       return pKey;
     }
     catch (Exception e) {
       
       this.log.error("failed to create user policy record, seems carrier policy response is missing in proposal document : ", e);
       return null;
     } 
   }
 
 
 
 
   
   private void createUserRelationMapping(String mobile, String uKey) {
     try {
       ObjectNode userRelationMapNode = this.objectMapper.createObjectNode();
       
       userRelationMapNode.put("userProfile", "UserProfile-" + mobile);
       userRelationMapNode.put("policyDetails", "PolicyDetails-" + mobile);
       userRelationMapNode.put("userAddress", "UserAddress-" + mobile);
       userRelationMapNode.put("vehicleDetails", "VehicleDetails-" + mobile);
       userRelationMapNode.put("familyDetails", "FamilyDetails-" + mobile);
       
       this.profileServices.updateUserRelationMappingRecord(mobile, userRelationMapNode);
     }
     catch (Exception e) {
       
       this.log.error("failed to create user relation mapping document", e);
     } 
   }
 
 
 
 
 
 
 
 
 
 
   
   public static void main(String[] args) {
     UserProfileServices ser = new UserProfileServices();
     
     System.out.println("view result : " + ser.getUserProfileByMobile("9028326704"));
   }
 }


