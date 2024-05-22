 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.PaymentConstant;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PaymentResponseRendererProcessor implements Processor {
   Logger log = Logger.getLogger(PaymentResponseRendererProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String paymentGatewayResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode paymentGatewayResp = this.objectMapper.readTree(paymentGatewayResponse);
       this.log.info("Inside payment response renderer processor.");
       this.log.info("Payment gateway response : " + paymentGatewayResp);
       PaymentConstant.PROPERTIES.getClass();
       String carrierName = paymentGatewayResp.get("carrier").asText();
       PaymentConstant.PROPERTIES.getClass();
       String lineOfBusiness = paymentGatewayResp.get("lineOfBusiness").asText();
       PaymentConstant.PROPERTIES.getClass();
       String redirectURL = paymentGatewayResp.get("successURL").asText();
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = paymentGatewayResp.get("failureURL").asText();
       PaymentConstant.PROPERTIES.getClass();
       String proposalId = paymentGatewayResp.get("proposalId").asText();
       String carrier = String.valueOf(carrierName) + lineOfBusiness;
       this.log.info("carrier name with lob : " + carrier);
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("failureURL", failureURL);
       PaymentConstant.PROPERTIES.getClass();
       if (redirectURL.contains("payfailure")) {
         ObjectNode paymentGatewayFailureResponse = this.objectMapper.createObjectNode();
         PaymentConstant.PROPERTIES.getClass();
         paymentGatewayFailureResponse.put("failureURL", redirectURL);
         PaymentConstant.PROPERTIES.getClass();
         paymentGatewayFailureResponse.put("proposalId", proposalId);
         PaymentConstant.PROPERTIES.getClass();
         exchange.setProperty("policyStatus", "failure");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("policyStatus", "failure");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("carrierName", carrier);
         exchange.getIn().setBody(paymentGatewayFailureResponse);
       } else {
         PaymentConstant.PROPERTIES.getClass();
         exchange.setProperty("policyStatus", "success");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("policyStatus", "success");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("carrierName", carrier);
         exchange.getIn().setBody(paymentGatewayResp);
       } 
     } catch (Exception ex) {
       this.log.info("Exception : Payment response renderer processor : ", ex);
       throw new ExecutionTerminator();
     } 
   }
 }


