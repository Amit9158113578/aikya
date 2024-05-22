 package com.idep.soap.util;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class DBQuoteProcessor implements Processor {
   Logger log = Logger.getLogger(DBQuoteProcessor.class.getName());
   
   int carrierId;
   
   public void process(Exchange exchange) {
     try {
       String joltRes = (String)exchange.getIn().getBody(String.class);
       JsonNode joltResponse = SoapUtils.objectMapper.readTree(joltRes);
       JsonNode inputRequest = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
       if (inputRequest.findValue("stage").asText().equalsIgnoreCase("Quote")) {
         ((ObjectNode)inputRequest).put("carrierData", joltResponse.get("data").get("quotes").get(0));
         ((ObjectNode)inputRequest).put("subStage", "Response");
         exchange.getIn().setBody(inputRequest.toString());
         exchange.getIn().setHeader("sendToQ", "True");
       } else {
         ((ObjectNode)inputRequest).put("carrierData", joltResponse);
         exchange.getIn().setBody(inputRequest.toString());
       } 
       this.carrierId = inputRequest.findValue("carrierId").asInt();
     } catch (Exception e) {
       this.log.error("Exception in DBQuoteProcessor :" + e);
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
     } 
   }
 }


