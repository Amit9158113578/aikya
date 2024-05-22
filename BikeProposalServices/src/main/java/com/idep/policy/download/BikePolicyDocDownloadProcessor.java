 package com.idep.policy.download;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.downloader.impl.DownloaderAPI;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import com.idep.services.impl.ECMSManagerAPI;
 import com.idep.user.profile.impl.UserProfileServices;
 import java.io.File;
 import java.io.FileOutputStream;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
 public class BikePolicyDocDownloadProcessor
   implements Processor
 {
   Logger log = Logger.getLogger(BikePolicyDocDownloadProcessor.class.getName());
   
   CBService policyTransService = CBInstanceProvider.getPolicyTransInstance();
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   UserProfileServices profileServices = new UserProfileServices();
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode configDocNode = null;
       String downloadURL = null;
       String documentId = null;
       String fileName = null;
       String reqBody = (String)exchange.getIn().getBody(String.class);
       JsonNode reqNode = Utils.mapper.readTree(reqBody);
       ExtendedJsonNode requestNode = new ExtendedJsonNode(reqNode);
       if (requestNode.has("responseCode") && requestNode.getKey("responseCode").equals(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText())) {
         requestNode = requestNode.get("data");
         if (requestNode.has("downloadPolicy") && requestNode.getKey("downloadPolicy").equalsIgnoreCase("Y")) {
           JsonDocument policyDocDownConfig = this.serverConfig.getDocBYId("BikePolicyDocDownloadConfig-" + requestNode.findValueAsText("carrierId") + "-" + requestNode.findValueAsText("productId"));
           if (policyDocDownConfig != null) {
             configDocNode = Utils.mapper.readTree(((JsonObject)policyDocDownConfig.content()).toString());
             ExtendedJsonNode extendedconfigDocNode = new ExtendedJsonNode(configDocNode);
             if (requestNode.has("base64data"))
               fileName = createPDFFile(requestNode, extendedconfigDocNode); 
             if (requestNode.has("fetchDocumentNameResponseElement")) {
               this.log.info("PolicyDoc Request for New India :" + reqNode);
               JsonNode indexArrayNode = reqNode.get("data").get("fetchDocumentNameResponseElement").get("indexType");
               JsonNode docsArrayNode = reqNode.get("data").get("fetchDocumentNameResponseElement").get("docs");
               String policyIndexValue = null;
               this.log.info("fetchDocServiceResNode : " + requestNode.get("fetchDocumentNameResponseElement"));
               for (int i = 0; i <= docsArrayNode.size(); i++) {
                 if (docsArrayNode.get(i).get("name").asText().substring(0, 15).equals("POLICY_DOCUMENT")) {
                   policyIndexValue = indexArrayNode.get(i).get("index").asText();
                   this.log.info("policyIndexNumber :" + policyIndexValue);
                   break;
                 } 
                 if (docsArrayNode.get(i).get("name").asText().substring(0, 20).equals("TW_NIAPOLICYSCHEDULE")) {
                   policyIndexValue = indexArrayNode.get(i).get("index").asText();
                   this.log.info("policyIndexNumber :" + policyIndexValue);
                   break;
                 } 
               } 
               requestNode.put("contentRepoType", "ContentRepository");
               requestNode.put("indexId", policyIndexValue);
               requestNode.remove("fetchDocumentNameResponseElement");
             } 
             if (requestNode.findValue("downloadUrl") != null)
               downloadURL = requestNode.findValueAsText("downloadUrl"); 
             if (extendedconfigDocNode.has("downloadUrl"))
               downloadURL = extendedconfigDocNode.getKey("downloadUrl"); 
             this.log.info("download url :" + downloadURL);
             if (extendedconfigDocNode.has("fieldNameReplacement")) {
               ArrayNode fieldNameRepArray = extendedconfigDocNode.get("fieldNameReplacement").iterator();
               for (JsonNode fieldNameRep : fieldNameRepArray)
                 downloadURL = downloadURL.replace(fieldNameRep.get("destFieldName").asText(), requestNode.findValueAsText(fieldNameRep.get("sourceFieldName").asText())); 
               this.log.info("replace download url :" + downloadURL);
             } 
             if (extendedconfigDocNode.has("concatenate") && extendedconfigDocNode
               .get("concatenate").has("lastIndexOf"))
               if (requestNode.has(extendedconfigDocNode.get("concatenate").get("lastIndexOf").asText("downloadLink"))) {
                 String url = requestNode.findValueAsText(extendedconfigDocNode.get("concatenate").get("lastIndexOf").asText("downloadLink"));
                 String pdfName = url.substring(url.lastIndexOf("/"), url.length());
                 downloadURL = String.valueOf(String.valueOf(downloadURL)) + pdfName;
                 this.log.info("concatenate download url :" + downloadURL);
               } else {
                 this.log.info("carrier document download link not found in request :" + requestNode);
               }  
             if (extendedconfigDocNode.has("policyDocSaveLocation") && downloadURL != null) {
               DownloaderAPI downloaderAPI = new DownloaderAPI();
               fileName = downloaderAPI.downloadFile(extendedconfigDocNode.getKey("policyDocSaveLocation"), downloadURL);
               fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
             } 
           } else {
             ExtendedJsonNode failure = (new ExceptionResponse()).configDocMissing("config document not found for carrier :BikePolicyDocDownloadConfig-" + requestNode.findValueAsText("carrierId") + "-" + requestNode.findValueAsText("productId"));
             exchange.getIn().setBody(failure);
             throw new ExecutionTerminator();
           } 
           this.log.info("fileName  :" + fileName);
           if (!fileName.equals(null)) {
             JsonNode contentMgmtConfigNode = null;
             JsonDocument contentMgtConfig = this.serverConfig.getDocBYId("ContentManagementConfig-" + requestNode.findValueAsText("carrierId") + "-" + requestNode.findValueAsInt("businessLineId"));
             if (contentMgtConfig != null) {
               contentMgmtConfigNode = Utils.mapper.readTree(((JsonObject)contentMgtConfig.content()).toString());
             } else {
               ExtendedJsonNode failure = (new ExceptionResponse()).configDocMissing("config document not found for carrier :ContentManagementConfig-" + requestNode.findValueAsText("carrierId") + "-" + requestNode.findValueAsInt("businessLineId"));
               exchange.getIn().setBody(failure);
               throw new ExecutionTerminator();
             } 
             documentId = uploadDocument(requestNode, fileName, contentMgmtConfigNode);
             this.log.info("policy document download documentId :" + documentId);
             if (documentId != null)
               updatePolicyDocument(requestNode, documentId); 
           } 
         } else {
           this.log.info("No need to download policy document because flage is missing or its value is not be Y");
           exchange.getIn().setBody(reqNode);
         } 
       } else {
         exchange.getIn().setBody(reqNode);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception At : BikePolicyDocDownloadProcessor :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 
   
   public void updatePolicyDocument(ExtendedJsonNode extendedJsonNode, String downloadURL) throws Exception {
     try {
       JsonNode userProfileDetailsNode = this.profileServices.getUserProfileByUkey(extendedJsonNode.findValueAsText("uKey"));
       if (userProfileDetailsNode == null)
       {
         userProfileDetailsNode = Utils.mapper.readTree(((JsonObject)this.policyTransService.getDocBYId("UserProfile-" + extendedJsonNode.findValueAsText("mobile")).content()).toString());
       }
       if (userProfileDetailsNode != null) {
         ObjectNode policyDetailsNode = Utils.mapper.createObjectNode();
         policyDetailsNode.put("DownloadLink", downloadURL);
         policyDetailsNode.put("contentRepoType", "ContentRepository");
         policyDetailsNode.put("contentRepoName", "Alfresco");
         String mobile = userProfileDetailsNode.findValue("mobile").asText();
         boolean status = this.profileServices.updatePolicyRecordByPkey(mobile, extendedJsonNode.findValueAsText("pKey"), policyDetailsNode);
         this.log.info("policy details updated status : " + status);
       } 
     } catch (Exception e) {
       throw new ExecutionTerminator();
     } 
   }
   
   public String uploadDocument(ExtendedJsonNode requestNode, String fileName, JsonNode contentMgmtConfigNode) throws Exception {
     String documentId = null;
     try {
       ECMSManagerAPI managerAPI = new ECMSManagerAPI();
       JsonObject metaDataObj = JsonObject.create();
       metaDataObj.put("policyNumber", requestNode.findValueAsText("policyNo"));
       metaDataObj.put("customerId", requestNode.findValueAsText("proposalId"));
       metaDataObj.put("emailId", requestNode.findValueAsText("emailId"));
       metaDataObj.put("mobileNumber", requestNode.findValueAsText("mobile"));
       metaDataObj.put("policyBond", "BikePolicyBond");
       documentId = managerAPI.uploadPolicyDocument(contentMgmtConfigNode, fileName, metaDataObj);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Failed to upload policy document details" + e.getMessage());
       throw new ExecutionTerminator();
     } 
     return documentId;
   }
   
   public String createPDFFile(ExtendedJsonNode requestNode, ExtendedJsonNode extendedconfigDocNode) throws Exception {
     try {
       this.log.info("Converting to PDF started.....");
       String filePath = extendedconfigDocNode.getKey("policyDocSaveLocation");
       if (extendedconfigDocNode.has("concat")) {
         ExtendedJsonNode extendedJsonNode = extendedconfigDocNode.get("concat");
         filePath = filePath.concat(requestNode.findValueAsText(extendedJsonNode.getKey("policyNo")).concat(extendedJsonNode.getKey("pdf")));
       } 
       byte[] decodedBytes = Base64.decodeBase64(requestNode.getKey("base64data"));
       File file = new File(filePath);
       FileOutputStream fop = new FileOutputStream(file);
       this.log.info("PDF Generated");
       fop.write(decodedBytes);
       fop.flush();
       fop.close();
       return requestNode.findValueAsText("policyNo").concat(".pdf");
     } catch (Exception e) {
       this.log.error("unable to create policy pdf file at " + extendedconfigDocNode.getKey("policyDocSaveLocation") + " : not found key :" + e.getMessage());
       e.printStackTrace();
       return null;
     } 
   }
 }


