 package com.idep.policy.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class BikePolicyReqHandler implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(BikePolicyReqHandler.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     try {
       String carrierRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode carrierReqNode = this.objectMapper.readTree(carrierRequest);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(carrierReqNode.get("carrierRequestForm")));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYREQHAND|ERROR|policy req handler processing failed :", e);
       throw new ExecutionTerminator();
     } 
   }
 }


