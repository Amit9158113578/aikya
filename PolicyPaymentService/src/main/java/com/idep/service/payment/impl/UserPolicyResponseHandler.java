 package com.idep.service.payment.impl;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.CommonLib;
 import com.idep.service.payment.util.PaymentConstant;
 import java.util.Iterator;
 import java.util.Map;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class UserPolicyResponseHandler implements Processor {
   Logger log = Logger.getLogger(UserPolicyResponseHandler.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String redirectURLParams = "";
       String redirectURL = "";
       ObjectNode redirectionLinkResp = this.objectMapper.createObjectNode();
       String createPolicyResp = (String)exchange.getIn().getBody(String.class);
       JsonNode createPolicyResponse = this.objectMapper.readTree(createPolicyResp);
       this.log.info("Create user policy response : " + createPolicyResponse);
       PaymentConstant.PROPERTIES.getClass();
       String successURL = exchange.getProperty("successURL").toString();
       PaymentConstant.PROPERTIES.getClass();
       String failureURL = exchange.getProperty("failureURL").toString();
       PaymentConstant.PROPERTIES.getClass();
       if (createPolicyResponse.get("responseCode").asInt() == 1000 || "P365RES100".equalsIgnoreCase(createPolicyResponse.get("responseCode").textValue())) {
         PaymentConstant.PROPERTIES.getClass();
         ObjectNode objNode = (ObjectNode)createPolicyResponse.get("data");
         Iterator<Map.Entry<String, JsonNode>> fields = objNode.fields();
         while (fields.hasNext()) {
           Map.Entry<String, JsonNode> entry = fields.next();
           redirectURLParams = String.valueOf(redirectURLParams) + ((String)entry.getKey()).toString() + "=" + ((JsonNode)entry.getValue()).toString();
           if (fields.hasNext())
             redirectURLParams = String.valueOf(redirectURLParams) + "&"; 
         } 
         redirectURLParams = redirectURLParams.replaceAll("\"", "");
         String[] successURLArray = successURL.split("\\?");
         String oldSuccessURLParam = successURLArray[1];
         String[] successURLParamArray = oldSuccessURLParam.split("&");
         String sourceTag = "";
         for (int i = 0; i < successURLParamArray.length; i++) {
           sourceTag = successURLParamArray[i];
           if (sourceTag.startsWith("source"))
             break; 
         } 
         String successURLVal = successURLArray[0];
         String successURLParam = "?";
         if (successURLArray.length > 1) {
           Map<String, String> successURLParamMap = CommonLib.getQueryMap(successURLArray[1]);
           PaymentConstant.PROPERTIES.getClass();
           successURLParam = String.valueOf(successURLParam) + "apPreferId=" + (String)successURLParamMap.get("apPreferId");
         } 
         redirectURL = String.valueOf(successURLVal) + successURLParam + "&" + redirectURLParams + "&" + sourceTag;
         
         PaymentConstant.PROPERTIES.getClass();
         redirectionLinkResp.put("policyStatus", "success");
       } else {
         redirectURL = failureURL;
         PaymentConstant.PROPERTIES.getClass();
         redirectionLinkResp.put("policyStatus", "failure");
       } 
       PaymentConstant.PROPERTIES.getClass();
       redirectionLinkResp.put("redirectURL", redirectURL);
       this.log.info("Final redirectUrl : " + redirectURL);
       exchange.getIn().setBody(redirectURL);
     } catch (Exception ex) {
       this.log.info("Exception : User policy response handler : ", ex);
       throw new ExecutionTerminator();
     } 
   }
 }


