 package com.idep.policy.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarPolicyResponseProcessor implements Processor {
   Logger log = Logger.getLogger(CarPolicyResponseProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode policyRes = this.objectMapper.readTree((String)exchange.getIn().getBody(String.class));
       String policyNumber = policyRes.findValue("policyno").asText();
       String policydocKey = policyRes.findValue("rnd_str").asText();
       JsonNode requestNode = (JsonNode)exchange.getProperty("reqNode");
       ObjectNode transactionStausInfo = (ObjectNode)requestNode.get("transactionStausInfo");
       transactionStausInfo.put("policyNo", policyNumber);
       transactionStausInfo.put("policydocKey", policydocKey);
       ((ObjectNode)requestNode).put("policyResponse", policyRes);
       exchange.getIn().setBody(requestNode);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "POLICYRES" + "|ERROR|" + "res transform processing failed :", e);
     } 
   }
 }


