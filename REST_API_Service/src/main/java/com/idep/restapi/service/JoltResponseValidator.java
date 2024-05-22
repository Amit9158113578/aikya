package com.idep.restapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.idep.restapi.utils.RestAPIConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class JoltResponseValidator implements Processor {
  Logger log = Logger.getLogger(JoltResponseValidator.class.getName());
  
  public void process(Exchange exchange) {
    try {
      Object joltResponse = exchange.getIn().getBody();
      JsonNode joltResNode = RestAPIConstants.objectMapper.readTree(joltResponse.toString());
      if (joltResNode.get("responseCode").asText().equalsIgnoreCase("P365RES100")) {
        JsonNode response = joltResNode.get("data");
        this.log.info("response " + response);
        exchange.getIn().setBody(response);
        exchange.getIn().setHeader("successRes", "True");
      } else {
        this.log.info("Error received from JoltResponseValidator");
        exchange.getIn().setHeader("successRes", "False");
      } 
    } catch (Exception e) {
      this.log.error("Exception in JoltResponseValidator :" + e);
      exchange.getIn().setHeader("successRes", "False");
    } 
  }
}
