 package com.idep.policy.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarPolicyReqHandler implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarPolicyReqHandler.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     String carrierRequest = (String)exchange.getIn().getBody(String.class);
     JsonNode carrierReqNode = this.objectMapper.readTree(carrierRequest);
     exchange.getIn().setBody(this.objectMapper.writeValueAsString(carrierReqNode.get("carrierRequestForm")));
   }
 }


