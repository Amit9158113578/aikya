package com.idep.restapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class RESTXMLResponseFormatter implements Processor {
  Logger log = Logger.getLogger(RESTXMLResponseFormatter.class.getName());
  
  JsonNode inputRequest;
  
  public void process(Exchange exchange) {
    try {
      this.inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      String XMLResponse = (String)exchange.getIn().getBody(String.class);
      JsonNode resNode = RestAPIConstants.objectMapper.readTree(XMLResponse);
      if (exchange.getProperty("RESTXMLConfiguration") != null) {
        JsonNode configurations = RestAPIConstants.objectMapper.readTree(exchange.getProperty("RESTXMLConfiguration").toString());
        if (configurations.has("validateResponseNode")) {
          JsonNode validations = configurations.get("validateResponseNode");
          this.log.info("validation node : " + validations);
          if (validations.has("successResponseKey") && validations.has("successResponseKey") && validations.has("datatype")) {
            this.log.info("inside validate response node");
            if (resNode.findValue(validations.get("successResponseKey").asText()) != null) {
              if (!resNode.findValue(validations.get("successResponseKey").asText()).isArray()) {
                exchange.getIn().setHeader("carrierResponse", "Success");
                this.log.info("carrier response  success");
                exchange.getIn().setBody(resNode);
              } else if (resNode.findValue(validations.get("successResponseKey").asText()).get(0) != null) {
                exchange.getIn().setHeader("carrierResponse", "Success");
                this.log.info("carrier response  success");
                exchange.getIn().setBody(resNode);
              } else {
                exchange.getIn().setHeader("carrierResponse", "Failure");
                this.log.info("carrier response  failure");
                exchange.getIn().setBody(ResponseMessageProcessor.returnNotValidcarrierResponse(this.inputRequest.findValue("carrierId").asInt(), resNode));
              } 
            } else {
              exchange.getIn().setHeader("carrierResponse", "Failure");
              this.log.info("carrier response  failure");
              exchange.getIn().setBody(ResponseMessageProcessor.returnNotValidcarrierResponse(this.inputRequest.findValue("carrierId").asInt(), resNode));
            } 
          } 
        } else {
          this.log.info("carrier doesn't contains validate response node");
          exchange.getIn().setBody(resNode);
        } 
      } else {
        this.log.error("InputRequest property is not set");
        exchange.getIn().setHeader("configDocumentFound", "False");
        exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.inputRequest.findValue("carrierId").asInt()));
      } 
    } catch (Exception e) {
      this.log.error("Exception in RESTXMLRequestFormatter", e);
      exchange.getIn().setHeader("configDocumentFound", "False");
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.inputRequest.findValue("carrierId").asInt()));
    } 
  }
}
