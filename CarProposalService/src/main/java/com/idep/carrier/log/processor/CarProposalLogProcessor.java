 package com.idep.carrier.log.processor;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarProposalLogProcessor implements Processor {
   Logger log = Logger.getLogger(CarProposalLogProcessor.class.getName());
   
   static ObjectMapper objectMapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     String data = new String();
     if (exchange.getProperty("defaultLog") != null)
       data = String.valueOf(String.valueOf(String.valueOf(data))) + exchange.getProperty("defaultLog"); 
     if (exchange.getProperty("webserviceType") != null)
       data = String.valueOf(String.valueOf(String.valueOf(data))) + exchange.getProperty("webserviceType").toString() + "|"; 
     if (exchange.getProperty("stage") != null)
       data = String.valueOf(String.valueOf(String.valueOf(data))) + exchange.getProperty("stage").toString() + "|"; 
     data = String.valueOf(String.valueOf(String.valueOf(data))) + "request body :" + exchange.getIn().getBody().toString();
     this.log.info(data);
   }
 }


