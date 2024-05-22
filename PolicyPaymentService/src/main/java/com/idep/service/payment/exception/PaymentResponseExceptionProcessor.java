 package com.idep.service.payment.exception;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.idep.service.payment.util.PaymentConstant;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PaymentResponseExceptionProcessor implements Processor {
   Logger log = Logger.getLogger(PaymentResponseExceptionProcessor.class.getName());
   
   public void process(Exchange exchange) throws JsonProcessingException {
     try {
       this.log.info("Payment response exception processor");
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = exchange.getProperty("failureURL").toString();
       this.log.info("failureURL : " + failureURL);
       exchange.getIn().setBody(failureURL);
     } catch (Exception e) {
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = exchange.getProperty("failureURL").toString();
       this.log.info("failureURL : " + failureURL);
       exchange.getIn().setBody(failureURL);
     } 
   }
 }


