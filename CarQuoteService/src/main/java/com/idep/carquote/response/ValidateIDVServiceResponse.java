 package com.idep.carquote.response;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ValidateIDVServiceResponse
   implements Processor {
   Logger log = Logger.getLogger(ValidateIDVServiceResponse.class);
   
   ObjectMapper mapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String response = exchange.getIn().getBody().toString();
       if (response.contains("No service was found.")) {
         ObjectNode responseNode = this.mapper.createObjectNode();
         responseNode.put("responseCode", "P365RES103");
         responseNode.put("message", "invoke service down");
         responseNode.put("data", "error");
         exchange.getIn().setBody(responseNode);
       } else {
         JsonNode requestNode = this.mapper.readTree(response);
         if (requestNode.has("responseCode")) {
           if (requestNode.get("responseCode").asText().equals("P365RES100")) {
             JsonNode carQuoteRequest = this.mapper.readTree(exchange.getProperty("carQuoteInputRequest").toString());
             ((ObjectNode)carQuoteRequest).put("response_IDV", requestNode.get("data"));
             exchange.getIn().setHeader("Quote", "Yes");
             exchange.getIn().setBody(carQuoteRequest);
           } else {
             exchange.getIn().setHeader("Quote", "No");
             exchange.getIn().setBody(requestNode);
           } 
         } else {
           this.log.info("response not found from rest/soap service");
           exchange.getIn().setBody(requestNode);
         } 
       } 
     } catch (Exception e) {
       e.printStackTrace();
       this.log.error("Exception in transform idv request processor");
     } 
   }
 }


