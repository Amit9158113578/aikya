 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.CommonLib;
 import com.idep.service.payment.util.PaymentConstant;
 import javax.ws.rs.core.UriInfo;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ValidatePaymentStatusResponseHandler implements Processor {
   Logger log = Logger.getLogger(ValidatePaymentStatusResponseHandler.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String validatePaymentStatusResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode validatePaymentStatusResp = this.objectMapper.readTree(validatePaymentStatusResponse);
       ObjectNode createPolicyRequestParams = this.objectMapper.createObjectNode();
       this.log.info("Validate payment status response : " + validatePaymentStatusResp);
       UriInfo info = null;
       JsonNode paymentResponseParamConfig = this.paymentDataAccessor.fetchDBDocument("PaymentResponseParamConfig", "serverConfig");
       PaymentConstant.PROPERTIES.getClass();
       String successURL = exchange.getProperty("successURL").toString();
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = exchange.getProperty("failureURL").toString();
       PaymentConstant.PROPERTIES.getClass();
       String policyStatus = exchange.getProperty("policyStatus").toString();
       PaymentConstant.PROPERTIES.getClass();
       String lineOfBusiness = exchange.getProperty("lineOfBusiness").toString();
       PaymentConstant.PROPERTIES.getClass();
       String carrierName = exchange.getProperty("carrier").toString();
       PaymentConstant.PROPERTIES.getClass();
       String paymentGatewayResponse = exchange.getProperty("paymentGatewayResponse").toString();
       if (validatePaymentStatusResp.get("responseCode").asInt() == 1000) {
         PaymentConstant.PROPERTIES.getClass();
         JsonNode validatePaymentStatusData = validatePaymentStatusResp.get("data");
         PaymentConstant.PROPERTIES.getClass();
         if (validatePaymentStatusData.get("status").asText().equalsIgnoreCase("success")) {
           PaymentConstant.PROPERTIES.getClass();
           exchange.setProperty("policyStatus", "success");
           policyStatus = "success";
         } else {
           PaymentConstant.PROPERTIES.getClass();
           exchange.setProperty("policyStatus", "failure");
           policyStatus = "failure";
           failureURL = failureURL.replace("paysuccesshealth", "payfailurehealth");
           PaymentConstant.PROPERTIES.getClass();
           exchange.setProperty("failureURL", failureURL);
         } 
         createPolicyRequestParams = CommonLib.createPolicyParamConfig(validatePaymentStatusData, paymentResponseParamConfig, carrierName, lineOfBusiness, info);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyRequestParams.put("successURL", successURL);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyRequestParams.put("failureURL", failureURL);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyRequestParams.put("policyStatus", policyStatus);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyRequestParams.put("lineOfBusiness", lineOfBusiness);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyRequestParams.put("carrier", carrierName);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyRequestParams.put("paymentGatewayResponse", paymentGatewayResponse);
         this.log.info("Create policy request node : " + createPolicyRequestParams);
         exchange.getIn().setBody(createPolicyRequestParams);
       } else {
         this.log.info("Exception : Policy purchase status check service failure.");
         throw new ExecutionTerminator();
       } 
     } catch (Exception ex) {
       this.log.info("Exception : Validate payment status response handler : ", ex);
       throw new ExecutionTerminator();
     } 
   }
 }


