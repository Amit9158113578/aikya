 package com.idep.policy.document.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.services.impl.ECMSManagerAPI;
 import org.apache.log4j.Logger;
 
 public class PolicyDocUploaderProcessor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(PolicyDocUploaderProcessor.class.getName());
   
   CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
   
   ECMSManagerAPI managerAPI = new ECMSManagerAPI();
   
   public String uploadPolicyDocument(String fileName, String documentId) {
     try {
       JsonNode contentMgmtConfigNode = this.objectMapper.readTree(((JsonObject)this.serverConfigService.getDocBYId(documentId).content()).toString());
       return null;
     } catch (Exception e) {
       this.log.error("Exception while uploading document to Alfresco ", e);
       return "";
     } 
   }
 }


