 package com.idep.service.payment.exception;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.impl.PaymentDataAccessor;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PaymentRequestExceptionProcessor implements Processor {
   Logger log = Logger.getLogger(PaymentRequestExceptionProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   JsonNode responseConfigNode;
   
   JsonNode errorNode;
   
   public void process(Exchange exchange) throws JsonProcessingException {
     try {
       if (this.responseConfigNode == null)
         this.responseConfigNode = this.objectMapper.readTree(this.paymentDataAccessor.fetchDBDocument("ResponseMessages", "serverConfig").toString()); 
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", this.responseConfigNode.get("errorCode").intValue());
       objectNode.put("message", this.responseConfigNode.get("errorMessage").textValue());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
     } catch (Exception e) {
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", this.responseConfigNode.get("errorCode").intValue());
       objectNode.put("message", this.responseConfigNode.get("errorMessage").textValue());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
     } 
   }
 }


