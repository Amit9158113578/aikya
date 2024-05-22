 package com.idep.carquote.carrier.log.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import java.io.IOException;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarQuoteLoggerProcessor implements Processor {
   Logger log = Logger.getLogger(CarQuoteLoggerProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   JsonNode errorNode;
   
   public void process(Exchange exchange) throws IOException {
     String data = new String();
     if (exchange.getProperty("defaultLogData") != null)
       data = String.valueOf(data) + exchange.getProperty("defaultLogData"); 
     if (exchange.getProperty("stage") != null)
       data = String.valueOf(data) + exchange.getProperty("stage").toString() + "|"; 
     if (exchange.getProperty("status") != null)
       data = String.valueOf(data) + exchange.getProperty("status").toString() + "|"; 
     this.log.info(String.valueOf(data) + "input request for soap/rest :" + this.objectMapper.readTree(exchange.getIn().getBody().toString()));
   }
 }


