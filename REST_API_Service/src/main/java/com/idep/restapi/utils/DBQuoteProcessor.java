package com.idep.restapi.utils;

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
      JsonNode joltResponse = RestAPIConstants.objectMapper.readTree(joltRes);
      JsonNode inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      this.carrierId = inputRequest.findValue("carrierId").asInt();
      if (inputRequest.findValue("stage").asText().equalsIgnoreCase("Quote")) {
        ((ObjectNode)inputRequest).put("carrierData", joltResponse.get("data").get("quotes").get(0));
        ((ObjectNode)inputRequest).put("subStage", "Response");
        exchange.getIn().setHeader("sendToQ", "True");
      } else {
        ((ObjectNode)inputRequest).put("carrierData", joltResponse);
        ((ObjectNode)inputRequest).put("subStage", "Response");
      } 
      exchange.getIn().setBody(inputRequest.toString());
    } catch (Exception e) {
      this.log.error("Exception in DBStoreProcessor :" + e);
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
    } 
  }
}
