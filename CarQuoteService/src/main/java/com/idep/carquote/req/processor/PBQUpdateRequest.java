 package com.idep.carquote.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import org.apache.camel.CamelContext;
 import org.apache.camel.Exchange;
 import org.apache.camel.ExchangePattern;
 import org.apache.camel.ProducerTemplate;
 import org.apache.log4j.Logger;
 
 public class PBQUpdateRequest {
   static Logger log = Logger.getLogger(PBQUpdateRequest.class.getName());
   
   public static void sendPBQUpdateRequest(JsonNode request, Exchange exchange) {
     try {
       CamelContext camelContext = exchange.getContext();
       ProducerTemplate template = camelContext.createProducerTemplate();
       String uri = "activemq:queue:pbqupdatereqQ";
       exchange.getIn().setBody(request.toString());
       exchange.setPattern(ExchangePattern.InOnly);
       template.send(uri, exchange);
       log.info("Updated for Car Quote request added in activemq:queue:pbqupdatereqQ : ");
     } catch (Exception e) {
       log.error("unable to send request to activemq:queue:pbqupdatereqQ ", e);
     } 
   }
 }


