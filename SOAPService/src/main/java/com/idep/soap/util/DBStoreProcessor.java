 package com.idep.soap.util;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class DBStoreProcessor implements Processor {
   ObjectMapper object = new ObjectMapper();
   
   Logger log = Logger.getLogger(DBStoreProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   int carrierId;
   
   public void process(Exchange exchange) {
     try {
       String joltRes = (String)exchange.getIn().getBody(String.class);
       JsonNode joltResponse = this.objectMapper.readTree(joltRes);
       JsonNode inputRequest = this.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
       if (inputRequest.findValue("stage").asText().equalsIgnoreCase("Quote")) {
         ((ObjectNode)inputRequest).put("carrierData", joltResponse.get("data").get("quotes").get(0));
         exchange.getIn().setBody(inputRequest.toString());
       } else {
         exchange.getIn().setBody(joltResponse.toString());
       } 
       this.carrierId = inputRequest.findValue("carrierId").asInt();
     } catch (Exception e) {
       this.log.error("Exception in DBStoreProcessor :" + e);
       exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
     } 
   }
 }


