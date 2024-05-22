 package com.idep.processor.customerprocessor;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CustomerProcessor
   implements Processor
 {
   Logger log = Logger.getLogger(CustomerProcessor.class.getName());
   ObjectMapper objectMapper = new ObjectMapper();
   public void process(Exchange exchange) throws Exception {
     try {
       String request = exchange.getIn().getBody().toString();
       this.log.info("Customer Request In exchane :" + request);
       request = request.replace("[\"", "\"");
       request = request.replace("\"]", "\"");
       request = request.replace("]", "");
       request = request.replace("[", "");
       this.log.info("Customer Request After removing brackets :" + request);
       exchange.getIn().setBody(request);
       this.log.info("Customer Request After setting body :" + exchange.toString());
     } catch (Exception e) {
       this.log.info("Exception While Setting body");
     } 
   }
 }


