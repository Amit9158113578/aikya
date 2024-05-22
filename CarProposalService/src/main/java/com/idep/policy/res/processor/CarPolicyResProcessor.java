 package com.idep.policy.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarPolicyResProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarPolicyResProcessor.class.getName());
   
   JsonNode errorNode = null;
   
   public void process(Exchange exchange) throws ExecutionTerminator, Exception {
     try {
       String policyResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode policyResNode = this.objectMapper.readTree(policyResponse);
       JsonNode responseNode = policyResNode.get("carrierRequestForm");
       ObjectNode obj = this.objectMapper.createObjectNode();
       obj.put("responseCode", 1000);
       obj.put("message", "success");
       obj.put("data", responseNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CARPOLICYRESPRO" + "|ERROR|" + "CarPolicyResProcessor Exception:", e);
       throw new ExecutionTerminator();
     } 
   }
 }


