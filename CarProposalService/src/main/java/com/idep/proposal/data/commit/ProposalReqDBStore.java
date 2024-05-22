 package com.idep.proposal.data.commit;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ProposalReqDBStore implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
   
   public void process(Exchange exchange) throws Exception {
     String docId = null;
     try {
       String proposalRequest = (String)exchange.getIn().getBody(String.class);
       JsonObject docObj = JsonObject.fromJson(proposalRequest);
       this.log.info("request for DB :" + docObj);
       if (docObj.get("status").equals("success")) {
         if (docObj.get("data") != null) {
           docObj = (JsonObject)docObj.get("data");
         } else if (docObj.get("transactionStausInfo") != null) {
           docObj = (JsonObject)docObj.get("transactionStausInfo");
         } 
         if (docObj.get("proposalId").toString() != null) {
           docId = docObj.get("proposalId").toString();
         } else {
           docId = exchange.getProperty("proposalId").toString();
         } 
         docObj = removeNotRequiredDataForDB(docObj);
         exchange.getIn().setBody(docObj);
         JsonDocument proposalDoc = null;
         JsonNode docInJson = null;
         try {
           proposalDoc = this.transService.getDocBYId(docId);
           if (proposalDoc != null)
             docInJson = Utils.mapper.readTree(((JsonObject)this.transService.getDocBYId(docId).content()).toString()); 
         } catch (Exception e) {
           ExtendedJsonNode failure = (new ExceptionResponse()).failure("failed to read proposal document from DB :" + docId);
           exchange.getIn().setBody(failure);
           throw new ExecutionTerminator();
         } 
         if (proposalDoc == null) {
           try {
             Date currentDate = new Date();
             docObj.put("proposalCreatedDate", this.dateFormat.format(currentDate));
             this.transService.createDocument(docId, docObj);
             this.log.info("document created with docid=" + docId + " and Document Obj=" + docObj);
           } catch (Exception e) {
             ExtendedJsonNode failure = (new ExceptionResponse()).failure("Failed to create car proposal Document  :" + docId + " and exceptio  is :" + e);
             exchange.getIn().setBody(failure);
             throw new ExecutionTerminator();
           } 
         } else {
           Date currentDate = new Date();
           docObj.put("updatedDate", this.dateFormat.format(currentDate));
           if (exchange.getIn().getHeader("carPolicyResponse") != null) {
             this.log.info("document type not found in response :" + docObj);
             docObj.put("documentType", exchange.getIn().getHeader("carPolicyResponse"));
           } 
           if (!docObj.containsKey("documentType") || docObj.getString("documentType") == null || docObj.getString("documentType").toString().isEmpty()) {
             this.log.info("document type not found in response :" + docObj);
             docObj.put("documentType", "carProposalResponse");
           } 
           JsonObject documentContent = ((JsonObject)proposalDoc.content()).put(docObj.getString("documentType"), docObj);
           if (docObj.containsKey("proposalStatus"))
             documentContent.put("proposalStatus", docObj.getString("proposalStatus")); 
           try {
             if (docInJson != null) {
               if (docInJson.has("isCleared")) {
                 this.transService.replaceDocument(docId, docObj);
               } else {
                 this.transService.replaceDocument(docId, documentContent);
               } 
             } else {
               this.transService.replaceDocument(docId, documentContent);
             } 
             this.log.info("proposal document updated successfully for proposal id : " + docId);
           } catch (Exception e) {
             ExtendedJsonNode failure = (new ExceptionResponse()).failure("Failed to update bike proposal Document  :" + docId + " and exceptio  is :" + e);
             exchange.getIn().setBody(failure);
             throw new ExecutionTerminator();
           } 
         } 
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Proposal Request DB Store :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
   public JsonObject removeNotRequiredDataForDB(JsonObject dbRequest) {
     try {
       if (dbRequest.containsKey("base64data")) {
         dbRequest = dbRequest.removeKey("base64data");
         return dbRequest;
       } 
     } catch (Exception e) {
       this.log.info("error in removeNotRequiredDataForDB method :" + e.getStackTrace());
     } 
     return dbRequest;
   }
 }


