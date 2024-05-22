 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.CommonLib;
 import com.idep.service.payment.util.PaymentConstant;
 import java.util.Map;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CreateUserPolicy implements Processor {
   Logger log = Logger.getLogger(CreateUserPolicy.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String paymentGatewayResponse = (String)exchange.getIn().getBody(String.class);
       this.log.info("Payment gateway response formatted from mappper : " + paymentGatewayResponse);
       JsonNode paymentGatewayResp = this.objectMapper.readTree(paymentGatewayResponse);
       JsonNode createPolicyTransNames = this.objectMapper.readTree("{\"life\":\"createLifePolicy\", \"bike\":\"createBikePolicy\", \"car\":\"createCarPolicy\", \"health\":\"createHealthPolicy\", \"travel\":\"createTravelPolicy\", \"personalaccident\":\"createPersonalAccidentPolicy\",\"home\":\"createHomePolicy\"}");
       JsonNode businessLineNames = this.objectMapper.readTree("{\"1\":\"life\", \"2\":\"bike\", \"3\":\"car\", \"4\":\"health\", \"5\":\"travel\", \"7\":\"home\", \"8\":\"personalaccident\"}");
       PaymentConstant.PROPERTIES.getClass();
       String successURL = exchange.getProperty("successURL").toString();
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = exchange.getProperty("failureURL").toString();
       PaymentConstant.PROPERTIES.getClass();
       String policyStatus = exchange.getProperty("policyStatus").toString();
       PaymentConstant.PROPERTIES.getClass();
       PaymentConstant.PROPERTIES.getClass();
       String proposalId = paymentGatewayResp.get("transactionStausInfo").get("proposalId").asText();
       PaymentConstant.PROPERTIES.getClass();
       String lineOfBusiness = paymentGatewayResp.get("businessLineId").asText();
       JsonNode serviceConfigNode = this.objectMapper.readTree(this.paymentDataAccessor.fetchDBDocument("ServiceURLConfig", "serverConfig").toString());
       JsonNode proposalDocument = this.paymentDataAccessor.fetchDBDocument(proposalId, "policyTransaction");
       JsonNode hostConfig = this.objectMapper.readTree(this.paymentDataAccessor.fetchDBDocument("WEBHOSTConfig", "serverConfig").toString());
       String[] successURLArray = successURL.split("\\?");
       if (successURLArray.length > 1 && successURLArray[1] != null) {
         Map<String, String> createPolicyParam = CommonLib.getQueryMap(successURLArray[1]);
         for (Map.Entry<String, String> entry : createPolicyParam.entrySet()) {
           PaymentConstant.PROPERTIES.getClass();
           ((ObjectNode)paymentGatewayResp.get("transactionStausInfo")).put(entry.getKey(), entry.getValue());
         } 
       } 
       if (policyStatus.equalsIgnoreCase("success")) {
         PaymentConstant.PROPERTIES.getClass();
         PaymentConstant.PROPERTIES.getClass();
         ((ObjectNode)paymentGatewayResp.get("transactionStausInfo")).put("transactionStatusCode", 1);
         PaymentConstant.PROPERTIES.getClass();
         exchange.setProperty("successURL", successURL);
         PaymentConstant.PROPERTIES.getClass();
         exchange.setProperty("failureURL", failureURL);
         PaymentConstant.PROPERTIES.getClass();
         exchange.setProperty("policyStatus", policyStatus);
         exchange.getIn().setHeader("MServiceURL", serviceConfigNode.get("masterService").asText());
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().removeHeader("CamelHttpPath");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().removeHeader("CamelHttpUri");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("policyStatus", "success");
         exchange.getIn().setHeader("CamelHttpMethod", "POST");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("Content-Type", "application/json");
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("CamelAcceptContentType", "application/json");
         PaymentConstant.PROPERTIES.getClass();
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("Origin", hostConfig.get("serviceOrigin").asText());
         ObjectNode headerNode = this.objectMapper.createObjectNode();
         headerNode.put("transactionName", createPolicyTransNames.get(businessLineNames.get(lineOfBusiness).asText()));
         headerNode.put("messageId", proposalDocument.get("leadMessageId"));
         headerNode.put("deviceId", proposalDocument.get("deviceId"));
         ObjectNode requestNode = this.objectMapper.createObjectNode();
         PaymentConstant.PROPERTIES.getClass();
         requestNode.put("header", (JsonNode)headerNode);
         PaymentConstant.PROPERTIES.getClass();
         requestNode.put("body", paymentGatewayResp);
         this.log.info("Create policy service call master request : " + requestNode);
         exchange.getIn().setBody(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestNode));
       } else {
         ObjectNode createPolicyResponse = this.objectMapper.createObjectNode();
         PaymentConstant.PROPERTIES.getClass();
         createPolicyResponse.put("failureURL", failureURL);
         PaymentConstant.PROPERTIES.getClass();
         createPolicyResponse.put("proposalId", proposalId);
         PaymentConstant.PROPERTIES.getClass();
         exchange.getIn().setHeader("policyStatus", "failure");
         exchange.getIn().setBody(createPolicyResponse);
       } 
     } catch (Exception ex) {
       this.log.info("Exception : Create Policy Error : ", ex);
       throw new ExecutionTerminator();
     } 
   }
 }


