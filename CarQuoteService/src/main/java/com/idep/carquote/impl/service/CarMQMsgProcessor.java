 package com.idep.carquote.impl.service;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 
 public class CarMQMsgProcessor implements Processor {
   public void process(Exchange exchange) {
     String message = exchange.getIn().getBody().toString();
     exchange.getIn().setHeader("JMSCorrelationID", exchange.getProperty("correlationId"));
     exchange.getIn().setBody(message);
   }
 }


