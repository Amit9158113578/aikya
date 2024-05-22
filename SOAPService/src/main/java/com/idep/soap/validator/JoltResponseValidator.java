 package com.idep.soap.validator;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.TreeNode;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.request.validate.impl.ValidateJsonImpl;
 import com.idep.soap.util.ResponseMessageProcessor;
 import com.idep.soap.util.SoapUtils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class JoltResponseValidator implements Processor {
   Logger log = Logger.getLogger(JoltResponseValidator.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   ValidateJsonImpl validate = new ValidateJsonImpl();
   
   int carrierId;
   
   public void process(Exchange exchange) {
     try {
       synchronized (this) {
         Object joltResponse = exchange.getIn().getBody();
         JsonNode joltResNode = SoapUtils.objectMapper.readTree(joltResponse.toString());
         JsonNode reqProperty = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
         this.carrierId = reqProperty.findValue("carrierId").asInt();
         if (joltResNode.get("responseCode").asText().equalsIgnoreCase("P365RES100")) {
           JsonNode responseNode = SoapUtils.objectMapper.readTree(joltResNode.get("data").toString());
           JsonNode response = validateRequest(responseNode, reqProperty);
           this.log.info("response :" + response);
           if (!response.has("responseCode")) {
             exchange.getIn().setHeader("successRes", "True");
             if (response.has("sortRequest") && response.get("sortRequest").asBoolean()) {
               exchange.getIn().setBody(sortRequest(response).toString());
             } else {
               exchange.getIn().setBody(response.toString());
             } 
           } else if (response.has("responseCode") && response.get("responseCode").asText().equals("P365RES100")) {
             exchange.getIn().setHeader("successRes", "True");
             if (response.has("sortRequest") && response.get("sortRequest").asBoolean()) {
               exchange.getIn().setBody(sortRequest(responseNode).toString());
             } else {
               exchange.getIn().setBody(responseNode.toString());
             } 
           } else {
             this.log.info("failure response received ");
             exchange.getIn().setHeader("successRes", "False");
             this.log.info("errorMessage message:" + response.get("message").toString());
             String errorMessage = response.get("message").asText();
             this.log.info("errorMessage :" + errorMessage);
             exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(errorMessage, this.carrierId));
           } 
         } else {
           this.log.error("Error received from JoltResponseValidator");
           exchange.getIn().setHeader("successRes", "False");
           exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.carrierId));
         } 
       } 
     } catch (Exception e) {
       this.log.error("Exception in JoltResponseValidator :" + e);
       e.printStackTrace();
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
     } 
   }
   
   public JsonNode validateRequest(JsonNode responseNode, JsonNode reqProperty) {
     JsonNode validationResponse = null;
     try {
       String docId;
       JsonDocument schemaDoc;
       int productId = reqProperty.findValue("productId").asInt();
       int lob = reqProperty.findValue("lob").asInt();
       String stage = reqProperty.findValue("stage").asText();
       JsonNode policyType = reqProperty.findValue("policyType");
       if (policyType != null) {
         docId = "SchemaValidation-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType.asText();
         this.log.info("fetching Schema Validation Doc :" + docId);
         schemaDoc = SoapUtils.serverConfig.getDocBYId(docId);
       } else {
         docId = "SchemaValidation-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId;
         schemaDoc = this.serverConfig.getDocBYId(docId);
       } 
       if (schemaDoc != null) {
         this.log.info("schemaDoc :" + schemaDoc);
         validationResponse = this.validate.parseCarrierJson(responseNode, ((JsonObject)schemaDoc.content()).toString());
         this.log.info("validationResponse :" + validationResponse);
       } else {
         this.log.error("Schema Validation document not found" + docId);
         return responseNode;
       } 
     } catch (Exception e) {
       e.printStackTrace();
     } 
     return validationResponse;
   }
   
   public JsonNode sortRequest(JsonNode data) {
     JsonNode requestNode = null;
     try {
       ObjectMapper objectMapper = new ObjectMapper();
       objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
       Object obj1 = objectMapper.treeToValue((TreeNode)data, Object.class);
       String request = objectMapper.writeValueAsString(obj1);
       requestNode = SoapUtils.objectMapper.readTree(request);
       ((ObjectNode)requestNode).remove("sortRequest");
       this.log.info("request after sorting :" + requestNode);
     } catch (Exception e) {
       this.log.info("Error at prepareRequest method : ", e);
     } 
     return requestNode;
   }
 }


