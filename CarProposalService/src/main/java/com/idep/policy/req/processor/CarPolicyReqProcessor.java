 package com.idep.policy.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarPolicyReqProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarPolicyReqProcessor.class.getName());
   
   CBService service = CBInstanceProvider.getPolicyTransInstance();
   
   public void process(Exchange exchange) throws ExecutionTerminator {
     JsonObject stage = JsonObject.create();
     try {
       String policyRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode reqNode = this.objectMapper.readTree(policyRequest);
       if (!reqNode.has("carProposalResponse")) {
         JsonObject carrierPropRes = (JsonObject)this.service.getDocBYId(reqNode.get("transactionStausInfo").get("proposalId").asText()).content();
         JsonNode carrierProposalResponse = this.objectMapper.readTree(carrierPropRes.get("carProposalResponse").toString());
         ((ObjectNode)reqNode).put("carrierProposalResponse", carrierProposalResponse);
       } 
       exchange.setProperty("carrierInputRequest", this.objectMapper.writeValueAsString(reqNode));
       exchange.setProperty("carrierReqMapConf", "CarPolicyREQCONF-" + reqNode.get("carrierId").intValue() + 
           "-" + reqNode.get("productId").intValue());
       stage.put("stage", "policy");
       stage.put("status", "pending");
       exchange.getProperty("policyStatus", stage);
       this.log.info("policy req stage info is: " + stage);
       exchange.getIn().setBody(reqNode);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CARPOLICYREQPROCE" + "|ERROR|" + "car policy request processor failed:", e);
       throw new ExecutionTerminator();
     } 
   }
 }


