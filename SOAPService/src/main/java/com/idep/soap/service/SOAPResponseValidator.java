 package com.idep.soap.service;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.soap.util.ResponseMessageProcessor;
 import com.idep.soap.util.SoapUtils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class SOAPResponseValidator implements Processor {
   Logger log = Logger.getLogger(SOAPResponseValidator.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   int carrierId;
   
   public void process(Exchange exchange) {
     try {
       JsonNode res = SoapUtils.objectMapper.readTree((String)exchange.getIn().getBody(String.class));
       JsonNode configurations = SoapUtils.objectMapper.readTree(exchange.getProperty("ResponseConfigDoc").toString());
       String tagName = configurations.get("responseTagName").asText();
       JsonNode response = res.findValue(tagName);
       JsonNode reqProperty = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
       this.carrierId = reqProperty.findValue("carrierId").asInt();
       this.log.info("response :" + response);
       if (configurations.has("validateKeyName")) {
         String responeValue = res.findValue(configurations.get("validateKeyName").asText()).asText();
         String failureResTag = null;
         if (configurations.has("failureResponse"))
           failureResTag = configurations.get("failureResponse").get("tagName").asText(); 
         String ExpectedValue = configurations.get("validateValueName").asText();
         this.log.info("responeValue :" + responeValue);
         this.log.info("ExpectedValue :" + ExpectedValue);
         if (responeValue.equalsIgnoreCase(ExpectedValue)) {
           this.log.info("Success response received");
           exchange.getIn().setHeader("success", "True");
           exchange.getIn().setBody(response);
         } else if (!res.findValue(configurations.get("validateKeyName").asText()).isNull()) {
           this.log.info("Success response received");
           exchange.getIn().setHeader("success", "True");
           exchange.getIn().setBody(response);
         } else {
           this.log.info("Failure response received from carrier");
           exchange.getIn().setHeader("success", "False");
           exchange.getIn().setBody(ResponseMessageProcessor.returnCarrierFailedResponse(res.findValue(failureResTag), this.carrierId));
         } 
       } else {
         this.log.info("Success response received");
         exchange.getIn().setHeader("success", "True");
         exchange.getIn().setBody(response);
       } 
     } catch (Exception e) {
       this.log.error("Exception in SOAPResponseValidator " + e);
       exchange.getIn().setHeader("success", "False");
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.carrierId));
     } 
   }
 }


