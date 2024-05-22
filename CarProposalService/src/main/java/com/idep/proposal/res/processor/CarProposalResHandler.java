 package com.idep.proposal.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarProposalResHandler implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarProposalResHandler.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     try {
       String carrierResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
       JsonNode requestDocNode = this.objectMapper.readTree(exchange.getProperty("carrierInputRequest").toString());
       ((ObjectNode)requestDocNode).put("carrierResponse", carrierResNode);
       exchange.setProperty("carrierResponse", requestDocNode);
       exchange.setProperty("carrierReqMapConf", "CarProposalRESCONF-" + 
           requestDocNode.get("carrierId").intValue() + "-" + 
           requestDocNode.get("productId").intValue());
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CARPRORESHANDL" + "|ERROR|" + "car proposal response handler failed:", e);
       throw new ExecutionTerminator();
     } 
   }
 }


