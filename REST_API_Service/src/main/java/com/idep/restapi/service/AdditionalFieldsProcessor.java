package com.idep.restapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.idep.restapi.quote.IDVRangeCalculatorProcessor;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class AdditionalFieldsProcessor implements Processor {
  Logger log = Logger.getLogger(AdditionalFieldsProcessor.class.getName());
  
  int carrierId;
  
  public void process(Exchange exchange) {
    try {
      String joltRes = (String)exchange.getIn().getBody(String.class);
      JsonNode joltResponse = RestAPIConstants.objectMapper.readTree(joltRes);
      JsonNode inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      this.carrierId = inputRequest.findValue("carrierId").asInt();
      if (inputRequest.findValue("stage").asText().equalsIgnoreCase("Quote")) {
        this.log.info("stage is Quote");
        JsonNode input_request = inputRequest.get("request");
        joltResponse = (new IDVRangeCalculatorProcessor()).process(input_request, joltResponse);
      } 
      exchange.getIn().setBody(joltResponse.toString());
    } catch (Exception e) {
      this.log.error("Exception in AdditionalFieldsProcessor :" + e);
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), this.carrierId));
    } 
  }
}
