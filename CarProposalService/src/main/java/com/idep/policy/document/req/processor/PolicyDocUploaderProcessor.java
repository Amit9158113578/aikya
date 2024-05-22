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
   
   public String uploadPolicyDocument(String fileName, String documentId, JsonNode proposalDoc) {
     try {
       JsonNode contentMgmtConfigNode = this.objectMapper.readTree(((JsonObject)this.serverConfigService.getDocBYId(documentId).content()).toString());
       String policyNo = proposalDoc.findValue("policyNo").asText();
       PolicyDocumentMetaData policyMetaData = new PolicyDocumentMetaData();
       this.log.info("flow before calling meatadata: ");
       JsonObject metaData = policyMetaData.createMetaData(proposalDoc, proposalDoc.get("proposalId").asText(), policyNo);
       this.log.info("flow after calling meatadata: " + metaData);
       String filePath = this.managerAPI.uploadPolicyDocument(contentMgmtConfigNode, fileName, metaData);
       this.log.info("filepath in policy doc uploader: " + filePath);
       return filePath;
     } catch (Exception e) {
       this.log.error("Exception while uploading document to Alfresco ", e);
       return "";
     } 
   }
 }


