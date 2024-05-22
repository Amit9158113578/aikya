 package com.idep.user.profile.impl;
 
 import com.couchbase.client.java.document.json.JsonArray;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.sync.service.impl.SyncGatewayServices;
 import com.idep.user.profile.util.MD5Encryption;
 import org.apache.log4j.Logger;
 
 
 
 public class UserProfileServices
 {
   static SyncGatewayServices syncService = new SyncGatewayServices();
   static Logger log = Logger.getLogger(UserProfileServices.class.getName());
   static ObjectMapper objectMapper = new ObjectMapper();
 
   
   public String createProfile(String mobile, ObjectNode data) {
     String uKey = "";
 
     
     try {
       uKey = MD5Encryption.MD5(mobile);
       data.put("secretKey", uKey);
       data.put("documentType", "userProfileDetails");
       data.put("mobile", mobile);
       String docId = "UserProfile-" + mobile;
       String docStatus = syncService.createTransDocumentBySync(docId, 
           JsonObject.fromJson(objectMapper.writeValueAsString(data)));
       log.info("User Profile document: " + docId + " status : " + docStatus);
       
       return uKey;
     }
     catch (Exception e) {
       
       log.error("failed to create user profile : ", e);
       return null;
     } 
   }
   
   public String updateUserProfile(String mobile, ObjectNode data) {
     String uKey = "";
 
     
     try {
       String docId = "UserProfile-" + mobile;
       JsonNode userProfile = syncService.getTransDocumentBySync(docId);
       
       if (userProfile != null) {
         
         log.info("userProfile value :" + userProfile);
         uKey = MD5Encryption.MD5(mobile);
         data.put("secretKey", uKey);
         data.put("documentType", "userProfileDetails");
         data.put("mobile", mobile);
         
         data.put("_id", userProfile.get("_id"));
         data.put("_rev", userProfile.get("_rev"));
         log.info("data value after adding userProfile:" + data);
         String docStatus = syncService.replaceTransDocumentBySync(docId, 
             JsonObject.fromJson(objectMapper.writeValueAsString(data)));
         log.info("User Profile document : " + docId + " status :" + docStatus);
       }
       else {
         
         uKey = createProfile(mobile, data);
       } 
       
       return uKey;
     }
     catch (Exception e) {
       
       log.error("failed to update user profile : ", e);
       return null;
     } 
   }
 
 
   
   public void createUserRelationMappingRecord(String mobile, ObjectNode data) {
     try {
       data.put("documentType", "userRelationMapping");
       data.put("mobile", mobile);
       data.put("secretKey", MD5Encryption.MD5(mobile));
       String docId = "UserRelationMapping-" + mobile;
       String docStatus = syncService.createTransDocumentBySync(docId, 
           JsonObject.fromJson(objectMapper.writeValueAsString(data)));
       log.info("User Relation Mapping document: " + docId + " status: " + docStatus);
     }
     catch (Exception e) {
       
       log.error("Failed to create User Relation Mapping document : ", e);
     } 
   }
 
 
   
   public void updateUserRelationMappingRecord(String mobile, ObjectNode data) {
     try {
       String docId = "UserRelationMapping-" + mobile;
       JsonNode userRelMapDoc = syncService.getTransDocumentBySync(docId);
       if (userRelMapDoc != null)
       {
         data.putAll((ObjectNode)userRelMapDoc);
         data.put("documentType", "userRelationMapping");
         data.put("mobile", mobile);
         data.put("secretKey", MD5Encryption.MD5(mobile));
         String docStatus = syncService.replaceTransDocumentBySync(docId, JsonObject.fromJson(objectMapper.writeValueAsString(data)));
         log.info("User Relation Mapping document: " + docId + " status: " + docStatus);
       }
       else
       {
         createUserRelationMappingRecord(mobile, data);
       }
     
     }
     catch (Exception e) {
       
       log.error("failed to update relation mapping record : ", e);
     } 
   }
 
 
   
   public JsonNode getUserProfileById(String docId) {
     try {
       return syncService.getTransDocumentBySync(docId);
     }
     catch (Exception e) {
       
       log.error("failed to get user profile by id:" + docId, e);
       return null;
     } 
   }
 
 
 
 
 
 
 
   
   public JsonNode collectUserProfileViewResults(JsonNode viewResultsNode) {
     try {
       JsonNode userRelationMap = syncService.getTransDocumentBySync(viewResultsNode.get("id").asText());
       System.out.println("userRelationMap : " + userRelationMap);
       if (userRelationMap != null) {
         
         ObjectNode userProfileResults = objectMapper.createObjectNode();
 
 
         
         userProfileResults.put("userProfile", syncService.getTransDocumentBySync(userRelationMap.get("userProfile").asText()));
 
 
         
         JsonNode policyDetails = syncService.getTransDocumentBySync(userRelationMap.get("policyDetails").asText());
         if (policyDetails != null) {
           userProfileResults.put("policyDetails", policyDetails.get("policyDetails"));
         }
 
         
         JsonNode vehicleDetails = syncService.getTransDocumentBySync(userRelationMap.get("vehicleDetails").asText());
         if (vehicleDetails != null) {
           userProfileResults.put("vehicleDetails", vehicleDetails.get("vehicleDetails"));
         }
 
         
         JsonNode addressDetails = syncService.getTransDocumentBySync(userRelationMap.get("userAddress").asText());
         if (addressDetails != null) {
           userProfileResults.put("userAddress", addressDetails.get("addressDetails"));
         }
 
         
         JsonNode familyDetails = syncService.getTransDocumentBySync(userRelationMap.get("familyDetails").asText());
         if (familyDetails != null) {
           userProfileResults.put("familyDetails", familyDetails.get("familyDetails"));
         }
         
         return (JsonNode)userProfileResults;
       } 
 
 
       
       log.error("user relation mapping document not found : " + viewResultsNode.get("id").asText());
       return null;
     
     }
     catch (Exception e) {
       
       log.error("Exception while collecting user profile information : ", e);
       e.printStackTrace();
       return null;
     } 
   }
 
 
 
 
   
   public JsonNode getUserProfileByMobile(String mobile) {
     try {
       JsonNode viewResultsNode = syncService.executeTransSyncGatewayView("userProfileByMobile", mobile);
       if (viewResultsNode != null)
       {
         return collectUserProfileViewResults(viewResultsNode);
       }
 
       
       log.error("VIEW failed to get results for mobile :" + mobile);
       Thread.sleep(10000L);
       viewResultsNode = syncService.executeTransSyncGatewayView("userProfileByMobile", mobile);
       if (viewResultsNode != null)
       {
         return collectUserProfileViewResults(viewResultsNode);
       }
 
       
       return null;
 
 
 
     
     }
     catch (Exception e) {
       
       log.error("failed to get user profile by mobile:" + mobile, e);
       return null;
     } 
   }
 
 
 
 
 
 
   
   public JsonNode getUserProfileByUkey(String uKey) {
     try {
       JsonNode viewResultsNode = syncService.executeTransSyncGatewayView("userProfileByUkey", uKey);
       if (viewResultsNode != null)
       {
         return collectUserProfileViewResults(viewResultsNode);
       }
 
       
       log.error("VIEW failed to get results for ukey :" + uKey);
       Thread.sleep(10000L);
       viewResultsNode = syncService.executeTransSyncGatewayView("userProfileByUkey", uKey);
       if (viewResultsNode != null)
       {
         return collectUserProfileViewResults(viewResultsNode);
       }
 
       
       return null;
 
     
     }
     catch (Exception e) {
       
       log.error("failed to get user profile by ukey:" + uKey, e);
       return null;
     } 
   }
 
 
 
   
   public void createNewPolicyRecord(String mobile, ObjectNode data) {
     try {
       String docId = "PolicyDetails-" + mobile;
       JsonObject policyDataNode = JsonObject.create();
       policyDataNode.put("documentType", "userPolicyDetails");
       policyDataNode.put("mobile", mobile);
       
       JsonArray policy = JsonArray.create();
       data.put("policyId", 1);
       policy.add(JsonObject.fromJson(objectMapper.writeValueAsString(data)));
       
       policyDataNode.put("policyDetails", policy);
       String docStatus = syncService.createTransDocumentBySync(docId, policyDataNode);
       log.info("User Policy Record document: " + docId + " status:" + docStatus);
     }
     catch (Exception e) {
       
       log.error("failed to create policy record:" + mobile, e);
     } 
   }
 
 
   
   public void addPolicyRecord(String mobile, ObjectNode data) {
     try {
       String docId = "PolicyDetails-" + mobile;
       JsonNode userPolicyDoc = syncService.getTransDocumentBySync(docId);
       
       if (userPolicyDoc != null)
       {
         JsonObject userPolicyNode = JsonObject.fromJson(objectMapper.writeValueAsString(userPolicyDoc));
         JsonArray policyArray = userPolicyNode.getArray("policyDetails");
         
         data.put("policyId", policyArray.size() + 1);
         policyArray.add(JsonObject.fromJson(objectMapper.writeValueAsString(data)));
         
         userPolicyNode.put("policyDetails", policyArray);
         String docStatus = syncService.replaceTransDocumentBySync(docId, userPolicyNode);
         log.info("User Policy Record document: " + docId + " status:" + docStatus);
       }
       else
       {
         createNewPolicyRecord(mobile, data);
       }
     
     } catch (Exception e) {
       
       log.error("failed to update policy record:" + mobile, e);
     } 
   }
 
 
 
 
 
 
 
 
 
 
 
   
   public boolean updatePolicyRecordByPkey(String mobile, String pKey, ObjectNode data) {
     try {
       if (data.has("DownloadLink") && data.has("contentRepoType")) {
         
         String docId = "PolicyDetails-" + mobile;
         JsonNode userPolicyDoc = syncService.getTransDocumentBySync(docId);
         
         if (userPolicyDoc != null) {
           
           boolean matchFound = false;
           JsonObject userPolicyNode = JsonObject.fromJson(objectMapper.writeValueAsString(userPolicyDoc));
           JsonArray policyArray = userPolicyNode.getArray("policyDetails");
 
 
           
           for (int i = 0; i < policyArray.size(); i++) {
             
             if (policyArray.getObject(i).containsKey("secretKey"))
             {
               if (policyArray.getObject(i).getString("secretKey").equals(pKey)) {
 
                 
                 policyArray.getObject(i).put("DownloadLink", data.get("DownloadLink").asText());
                 policyArray.getObject(i).put("contentRepoType", data.get("contentRepoType").asText());
                 if (data.has("contentRepoName"))
                 {
                   policyArray.getObject(i).put("contentRepoName", data.get("contentRepoName").asText());
                 }
                 matchFound = true;
                 
                 break;
               } 
             }
           } 
           if (matchFound) {
             
             userPolicyNode.put("policyDetails", policyArray);
             String docStatus = syncService.replaceTransDocumentBySync(docId, userPolicyNode);
             log.info("User Policy Record document: " + docId + " status: " + docStatus);
             return true;
           } 
 
           
           log.error("policy details cannot be updated as record does not exist with pkey :" + pKey + " for document :" + docId);
           return false;
         } 
 
 
 
         
         log.error("policy details document does not exist in DB : " + docId + " pkey : " + pKey);
         return false;
       } 
 
 
       
       log.error("policy update failed : DownloadLink and contentRepoType is missing in data");
       return false;
 
     
     }
     catch (Exception e) {
       
       log.error("failed to update policy record : " + mobile, e);
       return false;
     } 
   }
 
 
 
   
   public JsonNode getPolicyRecordByPkey(String mobile, String pKey) {
     try {
       String docId = "PolicyDetails-" + mobile;
       JsonNode userPolicyDoc = syncService.getTransDocumentBySync(docId);
       
       if (userPolicyDoc != null) {
         
         boolean matchFound = false;
         JsonNode policyNode = null;
         for (JsonNode policy : userPolicyDoc.get("policyDetails")) {
           
           if (policy.has("secretKey"))
           {
             if (policy.get("secretKey").asText().equalsIgnoreCase(pKey)) {
               
               matchFound = true;
               policyNode = policy;
               
               break;
             } 
           }
         } 
         
         if (matchFound)
         {
           return policyNode;
         }
 
         
         log.error("policy match not found by pKey : " + pKey + " for user policy details :" + userPolicyDoc);
         return null;
       } 
 
 
 
 
 
       
       log.error("policy details document does not exist in DB : " + docId + " pkey : " + pKey);
       return null;
 
     
     }
     catch (Exception e) {
       
       log.error("failed to get policy record : " + mobile, e);
       return null;
     } 
   }
 }


