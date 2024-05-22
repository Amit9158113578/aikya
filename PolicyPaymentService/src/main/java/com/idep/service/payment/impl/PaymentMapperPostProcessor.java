 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.PaymentResponseFormat;
 import com.idep.service.payment.util.CheckSumUtil;
 import com.idep.service.payment.util.CommonLib;
 import com.idep.service.payment.util.PaymentConstant;
 import java.io.IOException;
 import java.util.Iterator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 
 
 
 
 
 
 
 
 public class PaymentMapperPostProcessor
   implements Processor
 {
   Logger log = Logger.getLogger(PaymentMapperPostProcessor.class.getName());
   ObjectMapper objectMapper = new ObjectMapper();
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
 
 
 
 
 
 
 
   
   public String paymentRequestGenerate(String formdata, String proposalNumber) {
     this.log.info("Inside CarPayReqGeneration formdata : " + formdata);
     ObjectNode paymentRequest = this.objectMapper.createObjectNode();
     JsonNode paymentMapperResponse = null;
     JsonNode paymentConstantNode = null;
     try {
       paymentConstantNode = this.paymentDataAccessor.fetchDBDocument("PaymentConstantConfig", "serverConfig");
       if (formdata != null) {
         paymentMapperResponse = this.objectMapper.readTree(formdata);
         if (paymentMapperResponse.has("checksumStatus") && paymentMapperResponse.get("checksumStatus").asInt() == 1) {
           JsonNode paymentReqParams = CheckSumUtil.generatePaymentReqParam(paymentMapperResponse, paymentMapperResponse.get(paymentConstantNode.get("PAYMENT_CHECKSUM").textValue()).textValue(), paymentMapperResponse.get(paymentConstantNode.get("PAYMENT_GATEWAY").textValue()).textValue());
           if (paymentReqParams != null) {
             paymentRequest.putAll((ObjectNode)paymentReqParams);
           }
         } else if (paymentMapperResponse.has("checksumStatus") && paymentMapperResponse.get("checksumStatus").asInt() == 2) {
           JsonNode paymentReqParams = CheckSumUtil.generatePaymentPipeReqParam(paymentMapperResponse, paymentMapperResponse.get(paymentConstantNode.get("PAYMENT_CHECKSUM").textValue()).textValue(), paymentMapperResponse.get(paymentConstantNode.get("PAYMENT_GATEWAY").textValue()).textValue());
           if (paymentReqParams != null) {
             paymentRequest.putAll((ObjectNode)paymentReqParams);
           }
         }
         else if (paymentMapperResponse.has("checksumStatus") && paymentMapperResponse.get("checksumStatus").asInt() == 3) {
           JsonNode paymentReqParams = CheckSumUtil.generateCheckSum(paymentMapperResponse);
           if (paymentReqParams != null) {
             paymentRequest.putAll((ObjectNode)paymentReqParams);
           }
         }
         else if (paymentMapperResponse.has("checksumStatus") && paymentMapperResponse.get("checksumStatus").asInt() == 4) {
           JsonNode paymentReqParams = CheckSumUtil.generateTataAigCheckSum(paymentMapperResponse);
           if (paymentReqParams != null) {
             paymentRequest.putAll((ObjectNode)paymentReqParams);
           }
         }
         else if (paymentMapperResponse.has("checksumStatus") && paymentMapperResponse.get("checksumStatus").asInt() == 5) {
           JsonNode paymentReqParams = CheckSumUtil.generateSHA512CheckSum(paymentMapperResponse);
           if (paymentReqParams != null) {
             paymentRequest.putAll((ObjectNode)paymentReqParams);
           }
         }
         else if (paymentMapperResponse.has("checksumStatus") && paymentMapperResponse.get("checksumStatus").asInt() == 6) {
           JsonNode paymentReqParams = CheckSumUtil.generateHmacSHA256PaymentReqParam(paymentMapperResponse, paymentMapperResponse.get(paymentConstantNode.get("PAYMENT_CHECKSUM").textValue()).textValue(), paymentMapperResponse.get(paymentConstantNode.get("PAYMENT_GATEWAY").textValue()).textValue());
           if (paymentReqParams != null) {
             paymentRequest.putAll((ObjectNode)paymentReqParams);
           }
         }
         else if (paymentMapperResponse.has("submitParamApproach") && paymentMapperResponse.get("submitParamApproach").asText().equalsIgnoreCase("urlappend")) {
           
           String param = paymentMapperResponse.get("submitParams").asText();
           String value = null;
           Iterator<JsonNode> itr = paymentMapperResponse.get("paramterList").iterator();
           while (itr.hasNext()) {
             
             JsonNode node = itr.next();
             if (node.get("name").asText().equalsIgnoreCase(param)) {
               
               value = node.get("value").asText();
               break;
             } 
           } 
           String returnUrl = paymentMapperResponse.get("paymentURL").textValue();
           returnUrl = returnUrl + value;
           ((ObjectNode)paymentMapperResponse).put("paymentURL", returnUrl);
           paymentRequest.putAll((ObjectNode)paymentMapperResponse);
         }
         else {
           
           paymentRequest.putAll((ObjectNode)paymentMapperResponse);
         } 
         
         paymentRequest = CommonLib.updateReturnURL(paymentRequest, proposalNumber);
         this.log.info("Payment Request : " + paymentRequest);
         String payReqRefId = CommonLib.createPaymentRequestRecord(paymentRequest);
         String updateStatus = CommonLib.updateProposalDocument(payReqRefId, proposalNumber);
         this.log.info("Proposal document " + proposalNumber + " with update status " + updateStatus);
         
         return PaymentResponseFormat.createResponse(paymentConstantNode, paymentConstantNode.get("PAYMENT_RES_SUCCESS_CODE").asInt(), paymentConstantNode.get("PAYMENT_RES_SUCCESS_MESSAGE").textValue(), (JsonNode)paymentRequest);
       } 
       return PaymentResponseFormat.createResponse(paymentConstantNode, paymentConstantNode.get("PAYMENT_RES_FAILED_CODE").asInt(), paymentConstantNode.get("PAYMENT_RES_FAILED_MESSAGE").textValue(), (JsonNode)paymentRequest);
     }
     catch (JsonProcessingException e) {
       e.printStackTrace();
       return PaymentResponseFormat.createResponse(paymentConstantNode, paymentConstantNode.get("PAYMENT_RES_FAILED_CODE").asInt(), paymentConstantNode.get("PAYMENT_RES_FAILED_MESSAGE").textValue(), (JsonNode)paymentRequest);
     } catch (IOException e) {
       e.printStackTrace();
       return PaymentResponseFormat.createResponse(paymentConstantNode, paymentConstantNode.get("PAYMENT_RES_FAILED_CODE").asInt(), paymentConstantNode.get("PAYMENT_RES_FAILED_MESSAGE").textValue(), (JsonNode)paymentRequest);
     } 
   }
 
   
   public void process(Exchange exchange) throws Exception {
     try {
       this.log.info("Inside PaymentMapperPostProcessor Exchange.");
       String proposalResponse = (String)exchange.getIn().getBody(String.class);
       PaymentConstant.PROPERTIES.getClass(); String proposalNumber = exchange.getProperty("proposalNumber").toString();
       String paymentGatewayRequest = paymentRequestGenerate(proposalResponse, proposalNumber);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(paymentGatewayRequest));
     } catch (Exception ex) {
       this.log.error("Exception at PaymentMapperPostProcessor : ", ex);
     } 
   }
 }


