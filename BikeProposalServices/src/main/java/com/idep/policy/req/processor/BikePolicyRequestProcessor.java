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
 
 public class BikePolicyRequestProcessor
   implements Processor
 {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(BikePolicyRequestProcessor.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getPolicyTransInstance();
   
   public void process(Exchange exchange) throws ExecutionTerminator {
     try {
       String policyRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode reqNode = this.objectMapper.readTree(policyRequest);
       JsonObject carrierPropRes = (JsonObject)this.serverConfig.getDocBYId(reqNode.get("transactionStausInfo").get("proposalId").asText()).content();
       JsonNode carrierProposalResponse = this.objectMapper.readTree(carrierPropRes.get("bikeProposalResponse").toString());
       JsonNode paymentResponse = this.objectMapper.readTree(carrierPropRes.get("paymentResponse").toString());
       ((ObjectNode)reqNode).put("carrierProposalResponse", carrierProposalResponse);
       ((ObjectNode)reqNode).put("paymentResponse", paymentResponse);
       ((ObjectNode)reqNode).put("requestType", "BikePolicyRequest");
       exchange.setProperty("carrierInputRequest", this.objectMapper.writeValueAsString(reqNode));
       exchange.setProperty("carrierReqMapConf", "BikePolicyRequest-" + reqNode.get("carrierId").intValue() + "-" + reqNode
           .get("productId").intValue());
       exchange.getIn().setBody(reqNode);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYREQPRO|ERROR|policy req processing failed :", e);
       throw new ExecutionTerminator();
     } 
   }
 }


