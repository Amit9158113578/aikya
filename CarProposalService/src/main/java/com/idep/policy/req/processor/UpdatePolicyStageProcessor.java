 package com.idep.policy.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class UpdatePolicyStageProcessor implements Processor {
   Logger log = Logger.getLogger(UpdatePolicyStageProcessor.class);
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode requestNode = Utils.mapper.readTree(exchange.getIn().getBody().toString());
       JsonNode configDoc = Utils.mapper.readTree(exchange.getProperty("configDoc").toString());
       String loopIndex = exchange.getProperty("CamelLoopIndex").toString();
       String stage = configDoc.get(requestNode.findValue("carrierId").asText()).get("policyStages").get(loopIndex).asText();
       if (stage.isEmpty() || stage == null || stage.equalsIgnoreCase("NA")) {
         exchange.getIn().setBody((new ExceptionResponse()).configDocMissing("proposal stage field not found for carrierId :" + requestNode.findValue("carrierId").asText()));
         throw new ExecutionTerminator();
       } 
       ((ObjectNode)requestNode).put("stage", stage);
       exchange.setProperty("stage", stage);
       exchange.getIn().setBody(requestNode);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Update Stage Processor request processor :" + e.toString());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


