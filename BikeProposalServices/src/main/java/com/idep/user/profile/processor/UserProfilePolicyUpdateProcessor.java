 package com.idep.user.profile.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.services.impl.ECMSManagerAPI;
 import com.idep.user.profile.impl.UserProfileServices;
 import java.io.FileOutputStream;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
 public class UserProfilePolicyUpdateProcessor
   implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(UserProfilePolicyUpdateProcessor.class.getName());
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   CBService servercongi = CBInstanceProvider.getServerConfigInstance();
   
   UserProfileServices profileServices = new UserProfileServices();
   
   ECMSManagerAPI managerAPI = new ECMSManagerAPI();
   
   public void process(Exchange exchange) throws JsonProcessingException {
     try {
       String pdfFileLoc = exchange.getProperty("pdfFileLocation").toString();
       pdfFileLoc = String.valueOf(String.valueOf(pdfFileLoc)) + "signed.pdf";
       FileOutputStream fos = new FileOutputStream(pdfFileLoc);
       String resJSON = (String)exchange.getIn().getBody(String.class);
       this.log.debug("PolicyDocResponseProcessor JSON: " + resJSON);
       JsonNode policyResNode = this.objectMapper.readTree(resJSON);
       String pdfString = "data:application/pdf;base64,";
       String bas64String = policyResNode.get("data").get("signedPDF").textValue();
       String kotakPDF = bas64String.replaceAll(pdfString, "");
       fos.write(Base64.decodeBase64(kotakPDF));
       fos.close();
       JsonNode policyDocReqNode = this.objectMapper.readTree(exchange.getProperty("bikePolicyDocumentRequest").toString());
       ObjectNode policyPDFFileNode = this.objectMapper.createObjectNode();
       JsonObject metaDataObj = JsonObject.create();
       metaDataObj.put("policyNumber", policyDocReqNode.get("bikePolicyResponse").get("policyNo").asText());
       metaDataObj.put("customerId", policyDocReqNode.get("proposalId").asText());
       metaDataObj.put("emailId", policyDocReqNode.get("emailId").asText());
       metaDataObj.put("mobileNumber", policyDocReqNode.get("mobile").asText());
       metaDataObj.put("policyBond", "BikePolicyBond");
       String policyNo = policyDocReqNode.get("bikePolicyResponse").get("policyNo").asText();
       policyNo = policyNo.replaceAll("/", "");
       String fileName = String.valueOf(String.valueOf(policyNo)) + "signed.pdf";
       try {
         String documentId = "ContentManagementConfig-" + policyDocReqNode.get("carrierId") + "-" + policyDocReqNode.get("businessLineId");
         JsonNode contentMapConfig = this.objectMapper.readTree(((JsonObject)this.servercongi.getDocBYId(documentId).content()).toString());
         Thread.sleep(10000L);
         String filePath = this.managerAPI.uploadPolicyDocument(contentMapConfig, fileName, metaDataObj);
         if (filePath.length() > 0) {
           policyPDFFileNode.put("filePath", filePath);
           policyPDFFileNode.put("contentRepoType", "ContentRepository");
           policyPDFFileNode.put("contentRepoName", "Alfresco");
         } else {
           policyPDFFileNode.put("filePath", pdfFileLoc);
           policyPDFFileNode.put("contentRepoType", "FileSystem");
         } 
       } catch (Exception e) {
         this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "USERPROFILEUPDATE|ERROR|Exception while uploading policy document at Alfresco :");
         policyPDFFileNode.put("filePath", pdfFileLoc);
         policyPDFFileNode.put("contentRepoType", "FileSystem");
       } 
       JsonNode userProfileNode = (JsonNode)exchange.getProperty("userProfileData", JsonNode.class);
       JsonNode userPolicyKeyNode = (JsonNode)exchange.getProperty("userPolicyKeys", JsonNode.class);
       String policyKey = userPolicyKeyNode.findValue("pKey").asText();
       String mobile = userProfileNode.get("mobile").asText();
       boolean status = this.profileServices.updatePolicyRecordByPkey(mobile, policyKey, policyPDFFileNode);
       this.log.info("policy details updated status : " + status);
       this.log.info(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICY SIGN|SUCCESS|Policy Sign successfully");
       ObjectNode obj = this.objectMapper.createObjectNode();
       obj.put("responseCode", 1000);
       obj.put("message", "success");
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "USERPROFILEUPDATE|ERROR|unable to update policy document reference to users profile :", e);
       ObjectNode obj = this.objectMapper.createObjectNode();
       obj.put("responseCode", 1002);
       obj.put("message", "error");
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
     } 
   }
 }


