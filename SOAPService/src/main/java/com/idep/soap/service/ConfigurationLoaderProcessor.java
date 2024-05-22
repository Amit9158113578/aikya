 package com.idep.soap.service;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.jolt.soap.rest.DataCacher;
 import com.idep.soap.util.ResponseMessageProcessor;
 import com.idep.soap.util.SoapUtils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ConfigurationLoaderProcessor implements Processor {
   Logger log = Logger.getLogger(ConfigurationLoaderProcessor.class.getName());
   
   int carrierId;
   
   DataCacher dataCacher = new DataCacher();
   
   public void process(Exchange exchange) {
     try {
       String request = (String)exchange.getIn().getBody(String.class);
       JsonNode reqNode = SoapUtils.objectMapper.readTree(request);
       JsonNode loadedDoc = null;
       String docId = null;
       if (exchange.getProperty("inputRequest") == null) {
         int lob = reqNode.findValue("lob").asInt();
         this.carrierId = reqNode.findValue("carrierId").asInt();
         int productId = reqNode.findValue("productId").asInt();
         String stage = reqNode.findValue("stage").asText();
         JsonNode policyType = reqNode.findValue("policyType");
         if (policyType != null) {
           docId = "JOLTRequest-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType.asText();
           loadedDoc = SoapUtils.objectMapper.readTree(((JsonObject)SoapUtils.serverConfig.getDocBYId(docId).content()).toString());
         } else {
           docId = "JOLTRequest-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId;
           loadedDoc = this.dataCacher.docCache(docId);
         } 
         if (loadedDoc != null) {
           ObjectNode object = SoapUtils.objectMapper.createObjectNode();
           object.put("inputRequest", reqNode.get("request"));
           object.put("configuration", loadedDoc.get("configuration"));
           exchange.setProperty("inputRequest", reqNode);
           exchange.getIn().setHeader("documentFound", "True");
           exchange.getIn().setBody(object);
         } else {
           this.log.error("Configuration Document Not Found for docId :" + docId);
           exchange.getIn().setHeader("documentFound", "False");
           exchange.getIn().setBody(ResponseMessageProcessor.returnConfigDocResponse("Configuration Document Not Found, DocId : JOLTRequest-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType, this.carrierId));
         } 
       } else {
         JsonNode reqProperty = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
         int lob = reqProperty.findValue("lob").asInt();
         int carrierId = reqProperty.findValue("carrierId").asInt();
         int productId = reqProperty.findValue("productId").asInt();
         String stage = reqProperty.findValue("stage").asText();
         JsonNode policyType = reqProperty.findValue("policyType");
         if (policyType != null) {
           docId = "JOLTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-" + policyType.asText();
           loadedDoc = SoapUtils.objectMapper.readTree(((JsonObject)SoapUtils.serverConfig.getDocBYId(docId).content()).toString());
         } else {
           docId = "JOLTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
           loadedDoc = this.dataCacher.docCache(docId);
         } 
         if (loadedDoc != null) {
           ObjectNode object = SoapUtils.objectMapper.createObjectNode();
           ((ObjectNode)reqProperty).put("carrierResponse", reqNode);
           object.put("inputRequest", reqProperty);
           object.put("configuration", loadedDoc.get("configuration"));
           exchange.getIn().setHeader("documentFound", "True");
           exchange.getIn().setBody(object);
         } else {
           this.log.error("Configuration Document Not Found for docId :" + docId);
           exchange.getIn().setHeader("documentFound", "False");
           exchange.getIn().setBody(ResponseMessageProcessor.returnConfigDocResponse("Configuration Document Not Found, DocId : JOLTResponse-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-" + policyType, carrierId));
         } 
       } 
     } catch (Exception e) {
       this.log.error("Exception at ConfigurationLoaderProcessor : " + e.getMessage());
       exchange.getIn().setHeader("documentFound", "False");
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.carrierId));
     } 
   }
 }


