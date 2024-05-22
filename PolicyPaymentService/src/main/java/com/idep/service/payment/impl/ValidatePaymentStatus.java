 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.PaymentConstant;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ValidatePaymentStatus implements Processor {
   Logger log = Logger.getLogger(ValidatePaymentStatus.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String paymentGatewayResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode paymentGatewayResp = this.objectMapper.readTree(paymentGatewayResponse);
       this.log.info("Payment gateway response : " + paymentGatewayResp);
       PaymentConstant.PROPERTIES.getClass();
       String lineOfBusiness = paymentGatewayResp.get("lineOfBusiness").asText();
       PaymentConstant.PROPERTIES.getClass();
       String carrierName = paymentGatewayResp.get("carrier").asText();
       PaymentConstant.PROPERTIES.getClass();
       String successURL = paymentGatewayResp.get("successURL").asText();
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = paymentGatewayResp.get("failureURL").asText();
       PaymentConstant.PROPERTIES.getClass();
       String policyStatus = paymentGatewayResp.get("policyStatus").asText();
       JsonNode businessLineIdList = this.objectMapper.readTree("{\"life\":1, \"bike\":2, \"car\":3, \"health\":4, \"travel\":5,\"home\":7,\"personalaccident\":8}");
       JsonNode serviceConfigNode = this.objectMapper.readTree(this.paymentDataAccessor.fetchDBDocument("ServiceURLConfig", "serverConfig").toString());
       JsonNode hostConfig = this.objectMapper.readTree(this.paymentDataAccessor.fetchDBDocument("WEBHOSTConfig", "serverConfig").toString());
       JsonNode carrierConfigDocument = this.paymentDataAccessor.fetchDBDocument("CarrierConfig", "serverConfig");
       int businessLineId = businessLineIdList.get(lineOfBusiness).asInt();
       int carrierId = carrierConfigDocument.get(carrierName).asInt();
       PaymentConstant.PROPERTIES.getClass();
       PaymentConstant.PROPERTIES.getClass();
       JsonNode purchaseTokenArray = paymentGatewayResp.get("paymentGatewayResponse").get("purchaseToken");
       ArrayNode purchaseStatusArray = (ArrayNode)this.objectMapper.convertValue(purchaseTokenArray, ArrayNode.class);
       String purchaseStatus = purchaseStatusArray.get(0).asText();
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("successURL", successURL);
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("failureURL", failureURL);
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("policyStatus", policyStatus);
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("lineOfBusiness", lineOfBusiness);
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("carrier", carrierName);
       PaymentConstant.PROPERTIES.getClass();
       PaymentConstant.PROPERTIES.getClass();
       exchange.setProperty("paymentGatewayResponse", paymentGatewayResp.get("paymentGatewayResponse"));
       exchange.getIn().setHeader("MServiceURL", serviceConfigNode.get("masterService").asText());
       PaymentConstant.PROPERTIES.getClass();
       exchange.getIn().removeHeader("CamelHttpPath");
       PaymentConstant.PROPERTIES.getClass();
       exchange.getIn().removeHeader("CamelHttpUri");
       exchange.getIn().setHeader("CamelHttpMethod", "POST");
       PaymentConstant.PROPERTIES.getClass();
       exchange.getIn().setHeader("Content-Type", "application/json");
       PaymentConstant.PROPERTIES.getClass();
       exchange.getIn().setHeader("CamelAcceptContentType", "application/json");
       PaymentConstant.PROPERTIES.getClass();
       PaymentConstant.PROPERTIES.getClass();
       exchange.getIn().setHeader("Origin", hostConfig.get("serviceOrigin").asText());
       ObjectNode headerNode = this.objectMapper.createObjectNode();
       headerNode.put("transactionName", "validatePaymentStatus");
       headerNode.put("deviceId", "ABCD12345");
       ObjectNode bodyNode = this.objectMapper.createObjectNode();
       PaymentConstant.PROPERTIES.getClass();
       bodyNode.put("carrierId", carrierId);
       PaymentConstant.PROPERTIES.getClass();
       bodyNode.put("businessLineId", businessLineId);
       PaymentConstant.PROPERTIES.getClass();
       bodyNode.put("purchaseToken", purchaseStatus);
       ObjectNode requestNode = this.objectMapper.createObjectNode();
       PaymentConstant.PROPERTIES.getClass();
       requestNode.put("header", (JsonNode)headerNode);
       PaymentConstant.PROPERTIES.getClass();
       requestNode.put("body", (JsonNode)bodyNode);
       this.log.info("Validate payment status request : " + requestNode);
       exchange.getIn().setBody(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestNode));
     } catch (Exception ex) {
       this.log.info("Exception : Validate payment status : ", ex);
       throw new ExecutionTerminator();
     } 
   }
 }


