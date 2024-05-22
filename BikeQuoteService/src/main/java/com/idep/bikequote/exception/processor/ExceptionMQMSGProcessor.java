package com.idep.bikequote.exception.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.util.BikeQuoteConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ExceptionMQMSGProcessor implements Processor {
  Logger log = Logger.getLogger(ExceptionMQMSGProcessor.class.getName());
  
  ObjectMapper objectMapper = new ObjectMapper();
  
  JsonNode errorNode;
  
  public void process(Exchange exchange) throws JsonProcessingException {
    exchange.getIn().setHeader("JMSCorrelationID", exchange.getProperty("messageId"));
    ObjectNode node = this.objectMapper.createObjectNode();
    try {
      node.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES101");
      node.put(BikeQuoteConstants.QUOTE_RES_MSG, "calculate quote failure for insurer");
      node.put(BikeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(node));
    } catch (Exception e) {
      ObjectNode objectNode = this.objectMapper.createObjectNode();
      node.put(BikeQuoteConstants.QUOTE_RES_MSG, "calculate quote failure for insurer");
      objectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, String.valueOf(e.getMessage()) + " at :" + Thread.currentThread().getStackTrace()[1].getClassName());
      objectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
    } 
  }
}
