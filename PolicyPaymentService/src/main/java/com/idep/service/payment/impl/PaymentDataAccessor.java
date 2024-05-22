 package com.idep.service.payment.impl;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonArray;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.service.payment.util.PaymentConstant;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 
 public class PaymentDataAccessor {
   CBService policyTrans = CBInstanceProvider.getPolicyTransInstance();
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   static ObjectMapper objectMapper = new ObjectMapper();
   
   private static Logger log = Logger.getLogger(PaymentDataAccessor.class.getName());
   
   public String createDatabaseRecord(ObjectNode paymentRecord, String type) {
     String documentId = null;
     synchronized (paymentRecord) {
       JsonObject document = JsonObject.fromJson(paymentRecord.toString());
       if (type.equalsIgnoreCase("request")) {
         long sequence = this.serverConfig.updateDBSequence("SEQPAYMENTREQ");
         try {
           documentId = String.valueOf(DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("paymentRequestId").asText()) + "-" + sequence;
           String response = this.policyTrans.createDocument(documentId, document);
           if (response.equalsIgnoreCase("doc_created") || response.equalsIgnoreCase("doc_replaced") || response.equalsIgnoreCase("doc_updated")) {
             log.info("Payment Request document is created in the database with Id : " + documentId + " with response status : " + response);
           } else {
             log.info("Payment Request document document Id : " + documentId + " alredy exist and response status : " + response);
           } 
         } catch (Exception e) {
           e.printStackTrace();
         } 
       } else {
         long sequence = this.serverConfig.updateDBSequence("SEQPAYMENTRES");
         try {
           documentId = String.valueOf(DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("paymentResponseId").asText()) + "-" + sequence;
           String response = this.policyTrans.createDocument(documentId, document);
           if (response.equalsIgnoreCase("doc_created") || response.equalsIgnoreCase("doc_replaced") || response.equalsIgnoreCase("doc_updated")) {
             log.info("Payment Reponse document is created in the database with Id : " + documentId + " with response status :" + response);
           } else {
             log.info("Payment Reponse document document Id : " + documentId + " alredy exist and response status :" + response);
           } 
         } catch (Exception e) {
           e.printStackTrace();
         } 
       } 
     } 
     return documentId;
   }
   
   public JsonNode fetchDBDocument(String documentName, String bucketName) {
     JsonNode paymentConfigNode = null;
     log.info("PaymentConstant : document to be fetched : " + documentName);
     try {
       if (bucketName.equalsIgnoreCase("policyTransaction")) {
         JsonDocument document = this.policyTrans.getDocBYId(documentName);
         if (document != null) {
           paymentConfigNode = objectMapper.readTree(((JsonObject)document.content()).toString());
         } else {
           log.error("PaymentConstant : document seems to be missing : " + documentName);
         } 
       } else if (bucketName.equalsIgnoreCase("serverConfig")) {
         JsonDocument document = this.serverConfig.getDocBYId(documentName);
         if (document != null) {
           paymentConfigNode = objectMapper.readTree(((JsonObject)document.content()).toString());
         } else {
           log.error("PaymentConstant : document seems to be missing : " + documentName);
         } 
       } 
     } catch (JsonProcessingException e) {
       log.error("JsonProcessingException at PaymentConstant : ", (Throwable)e);
     } catch (IOException e) {
       log.error("IOException at PaymentConstant : ", e);
     } 
     return paymentConfigNode;
   }
   
   public JsonDocument fetchDocumentById(String bucketName, String documentId) throws JsonProcessingException, IOException {
     try {
       if (documentId != null && bucketName != null && bucketName.equalsIgnoreCase("policyTransaction"))
         return this.policyTrans.getDocBYId(documentId); 
       if (documentId != null && bucketName != null && bucketName.equalsIgnoreCase("serverConfig"))
         return this.serverConfig.getDocBYId(documentId); 
     } catch (Exception e) {
       log.info("Failed to fetch Document  :  " + documentId, e);
     } 
     return null;
   }
   
   public String replaceDocument(String bucketName, String documentId, JsonObject documentContent) {
     try {
       if (documentId != null && bucketName != null && bucketName.equalsIgnoreCase("policyTransaction"))
         return this.policyTrans.replaceAsyncDocument(documentId, documentContent); 
       if (documentId != null && bucketName != null && bucketName.equalsIgnoreCase("serverConfig"))
         return this.serverConfig.replaceAsyncDocument(documentId, documentContent); 
     } catch (Exception e) {
       log.info("Failed to Update Document  :  " + documentId, e);
     } 
     return null;
   }
   
   public String executeDbQueryWithParam(String query, JsonArray paramArray) {
     log.info("Executing DB Query.");
     log.info("Query : " + query);
     log.info("Query Params : " + paramArray);
     String documentId = "";
     try {
       List<JsonObject> queryResult = this.policyTrans.executeConsistentConfigParamArrQuery(query.trim(), paramArray);
       log.info("Query Response : " + queryResult);
       if (queryResult.size() != 0) {
         PaymentConstant.PROPERTIES.getClass();
         documentId = ((JsonObject)queryResult.get(0)).get("documentId").toString();
       } else {
         log.info("No response received for DB Query :  " + query + " : " + paramArray);
       } 
     } catch (Exception ex) {
       log.info("Failed to Execute DB Query :  " + query + " : " + paramArray);
       log.info("Exception : ", ex);
     } 
     return documentId;
   }
   
   public long updateSequenceCounter(String docId, String nodeName, String field) {
     try {
       JsonDocument document = this.serverConfig.getDocBYId(docId);
       JsonNode paymentConfigNode = objectMapper.readTree(((JsonObject)document.content()).toString());
       log.info("P365IntegrationList Document Fetched Value is:" + paymentConfigNode);
       JsonNode seqObject3 = paymentConfigNode.get(nodeName);
       long seq = seqObject3.get(field).asLong();
       ((ObjectNode)seqObject3).put(field, seq + 1L);
       ((ObjectNode)paymentConfigNode).set(nodeName, seqObject3);
       Map<String, String> mapData = (Map<String, String>)objectMapper.convertValue(paymentConfigNode, Map.class);
       JsonObject deviceIdNode = JsonObject.from(mapData);
       log.info("Replaced Document : " + deviceIdNode);
       String docStatus = replaceDocument("ServerConfig", docId, deviceIdNode);
       if (docStatus.equals("doc_replaced")) {
         log.info(String.valueOf(field) + " updated :" + (seq + 1L));
         return seq + 1L;
       } 
       seq = -1L;
       log.error("unable to update document");
       return seq;
     } catch (Exception e) {
       log.error("Exception at updateSequence: ", e);
       return -1L;
     } 
   }
 }


