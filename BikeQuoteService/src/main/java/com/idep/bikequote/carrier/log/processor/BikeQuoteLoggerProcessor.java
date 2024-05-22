package com.idep.bikequote.carrier.log.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class BikeQuoteLoggerProcessor implements Processor {
  Logger log = Logger.getLogger(BikeQuoteLoggerProcessor.class.getName());
  
  ObjectMapper objectMapper = new ObjectMapper();
  
  JsonNode errorNode;
  
  public void process(Exchange exchange) throws IOException {
    String data = new String();
    if (exchange.getProperty("defaultLogdata") != null)
      data = String.valueOf(data) + exchange.getProperty("defaultLogdata"); 
    if (exchange.getProperty("webserviceType") != null)
      data = String.valueOf(data) + exchange.getProperty("webserviceType").toString() + "|"; 
    if (exchange.getProperty("stage") != null)
      data = String.valueOf(data) + exchange.getProperty("stage").toString() + "|"; 
    data = String.valueOf(data) + "request body :" + exchange.getIn().getBody().toString();
    this.log.info(data);
   }
}
