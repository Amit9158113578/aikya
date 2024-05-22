 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.PaymentConstant;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CreateUserPolicyRequestConfig implements Processor {
   Logger log = Logger.getLogger(CreateUserPolicyRequestConfig.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String paymentGatewayResponse = (String)exchange.getIn().getBody(String.class);
       ObjectNode requestDocNode = this.objectMapper.createObjectNode();
       JsonNode paymentGatewayResp = this.objectMapper.readTree(paymentGatewayResponse);
       JsonNode businessLineIdList = this.objectMapper.readTree("{\"life\":1, \"bike\":2, \"car\":3, \"health\":4, \"travel\":5,\"home\":7,\"personalaccident\":8}");
       this.log.info("Payment gateway response : " + paymentGatewayResponse);
       PaymentConstant.PROPERTIES.getClass();
       String policyStatus = exchange.getProperty("policyStatus").toString();
       if (policyStatus.equalsIgnoreCase("success")) {
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("policyStatus", "success");
         PaymentConstant.PROPERTIES.getClass();
         String proposalId = paymentGatewayResp.get("proposalId").textValue();
         PaymentConstant.PROPERTIES.getClass();
         String successURL = paymentGatewayResp.get("successURL").textValue();
         PaymentConstant.PROPERTIES.getClass();
         String failureURL = paymentGatewayResp.get("failureURL").textValue();
         PaymentConstant.PROPERTIES.getClass();
         String lineOfBusiness = paymentGatewayResp.get("lineOfBusiness").textValue();
         JsonNode proposalDocument = this.paymentDataAccessor.fetchDBDocument(proposalId, "policyTransaction");
         JsonNode paymentConstantNode = this.paymentDataAccessor.fetchDBDocument("PaymentConstantConfig", "serverConfig");
         if (proposalDocument != null) {
           ((ObjectNode)proposalDocument).put(paymentConstantNode.get("BUSINESSLINE_ID").textValue(), businessLineIdList.get(lineOfBusiness).intValue());
           requestDocNode.put(paymentConstantNode.get("PAYMENT_MAPPER_REQUEST_TYPE").textValue(), paymentConstantNode.get("CREATE_POLICY_MAPPER_REQUEST_TYPE_VALUE").textValue());
           requestDocNode.put(paymentConstantNode.get("PAYMENT_MAPPER_CARRIER_RESP_KEY").textValue(), proposalDocument);
           PaymentConstant.PROPERTIES.getClass();
           exchange.setProperty("successURL", successURL);
           PaymentConstant.PROPERTIES.getClass();
           exchange.setProperty("failureURL", failureURL);
           PaymentConstant.PROPERTIES.getClass();
           exchange.setProperty("policyStatus", policyStatus);
           exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
         } else {
           this.log.info("Proposal Document not present : " + proposalId);
           PaymentConstant.PROPERTIES.getClass();
           exchange.getIn().setHeader("policyStatus", "failure");
           exchange.getIn().setBody(this.objectMapper.writeValueAsString(paymentGatewayResp));
         } 
       } else {
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("policyStatus", "failure");
         exchange.getIn().setBody(this.objectMapper.writeValueAsString(paymentGatewayResp));
       } 
     } catch (Exception ex) {
       this.log.info("Exception : Create User Policy Request Config Error : ", ex);
       throw new ExecutionTerminator();
     } 
   }
 }


