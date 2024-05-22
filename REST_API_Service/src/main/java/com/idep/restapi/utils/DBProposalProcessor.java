package com.idep.restapi.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class DBProposalProcessor implements Processor {
  Logger log = Logger.getLogger(DBProposalProcessor.class.getName());
  
  int carrierId;
  
  public void process(Exchange exchange) {
    try {
      String serviceResponse = (String)exchange.getIn().getBody(String.class);
      JsonNode inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      if (!inputRequest.findValue("stage").asText().equalsIgnoreCase("Quote")) {
        ((ObjectNode)inputRequest).put("carrierData", serviceResponse);
        ((ObjectNode)inputRequest).put("subStage", "Response");
        exchange.getIn().setBody(inputRequest.toString());
        exchange.getIn().setHeader("sendToQ", "True");
      } else {
        exchange.getIn().setBody(serviceResponse);
      } 
      this.carrierId = inputRequest.findValue("carrierId").asInt();
    } catch (Exception e) {
      this.log.error("Exception in DBProposalProcessor :" + e);
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
    } 
  }
}
