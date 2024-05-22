 package com.idep.soap.service;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.jolt.soap.rest.DataCacher;
 import com.idep.soap.util.ResponseMessageProcessor;
 import com.idep.soap.util.SoapUtils;
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import javax.xml.soap.MessageFactory;
 import javax.xml.soap.SOAPFault;
 import javax.xml.soap.SOAPMessage;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class SOAPResponseFormatter implements Processor {
   Logger log = Logger.getLogger(SOAPResponseFormatter.class.getName());
   
   DataCacher dataCacher = new DataCacher();
   
   int carrierId;
   
   public void process(Exchange exchange) {
     try {
       String resBody = (String)exchange.getIn().getBody();
       if (exchange.getProperty("inputRequest") != null) {
         JsonNode inputRequest = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
         JsonNode configurations = null;
         String docId = null;
         int lob = inputRequest.findValue("lob").asInt();
         this.carrierId = inputRequest.findValue("carrierId").asInt();
         int productId = inputRequest.findValue("productId").asInt();
         String stage = inputRequest.findValue("stage").asText();
         JsonNode policyType = inputRequest.findValue("policyType");
         if (policyType != null) {
           docId = "SOAPResponse-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType.asText();
           configurations = SoapUtils.objectMapper.readTree(((JsonObject)SoapUtils.serverConfig.getDocBYId(docId).content()).toString());
         } else {
           docId = "SOAPResponse-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId;
           configurations = this.dataCacher.docCache(docId);
         } 
         if (configurations != null) {
           exchange.getIn().setHeader("properResponseReceived", "True");
           exchange.setProperty("ResponseConfigDoc", configurations);
           InputStream is = new ByteArrayInputStream(resBody.getBytes());
           this.log.info("InputStream :" + is);
           SOAPMessage req = MessageFactory.newInstance().createMessage(null, is);
           this.log.info("SOAPMessage req:" + req);
           this.log.info("SOAPMessage has fault :" + req.getSOAPBody().hasFault());
           if (req.getSOAPBody().hasFault()) {
             SOAPFault sf = req.getSOAPBody().getFault();
             this.log.info("SOAP Fault Message :" + sf.getFaultString());
             exchange.getIn().setHeader("properResponseReceived", "False");
             exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(sf.getFaultString(), this.carrierId));
           } else {
             String tagName = configurations.get("responseTagName").asText();
             if (resBody.contains(tagName)) {
               this.log.info("response before replacing characters :" + resBody);
               if (configurations.has("replaceTags")) {
                 JsonNode replaceTags = SoapUtils.objectMapper.readTree(configurations.get("replaceTags").toString());
                 for (JsonNode replaceTag : replaceTags)
                   resBody = resBody.replaceAll(replaceTag.get("replaceTo").asText(), replaceTag.get("replaceWith").asText()); 
               } 
               this.log.info("response after replacing characters :" + resBody);
               String finalstring = resBody.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "").replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "").replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "").replace("<?xml version=\"1.0\" encoding=\"utf-16\"?>", "");
               exchange.getIn().setBody(finalstring);
             } else {
               this.log.error("Error response received from carrier");
               exchange.getIn().setHeader("properResponseReceived", "False");
               exchange.getIn().setBody(ResponseMessageProcessor.returnDynamicResponse("properResponseNotReceived", this.carrierId));
             } 
           } 
         } else {
           this.log.error("Configuration Document not found for docId :" + docId);
           exchange.getIn().setHeader("properResponseReceived", "False");
           exchange.getIn().setBody(ResponseMessageProcessor.returnConfigDocResponse("Configuration Document Not Found, DocId : SOAPResponse-" + stage + "-" + lob + "-" + this.carrierId + "-" + productId + "-" + policyType, this.carrierId));
         } 
       } 
     } catch (Exception e) {
       this.log.error("Exception in SOAPResponseFormatter processor " + e.getStackTrace());
       exchange.getIn().setHeader("properResponseReceived", "False");
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
     } 
   }
 }


