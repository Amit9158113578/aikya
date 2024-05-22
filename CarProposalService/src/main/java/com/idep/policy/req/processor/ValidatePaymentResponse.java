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
 
 public class ValidatePaymentResponse implements Processor {
   Logger log = Logger.getLogger(ValidatePaymentResponse.class);
   
   public void process(Exchange exchange) throws Exception {
     try {
       String requestString = (String)exchange.getIn().getBody(String.class);
       JsonNode createPolicyReqNode = Utils.mapper.readTree(requestString);
       if (createPolicyReqNode.has("transactionStausInfo")) {
         if (createPolicyReqNode.get("transactionStausInfo").has("apPreferId")) {
           ((ObjectNode)createPolicyReqNode).put("proposalId", createPolicyReqNode.get("transactionStausInfo").get("proposalId").asText());
           ((ObjectNode)createPolicyReqNode.get("transactionStausInfo")).put("documentType", "paymentResponse");
           ((ObjectNode)createPolicyReqNode).put("status", "success");
           exchange.getIn().setBody(createPolicyReqNode);
         } else {
           ExtendedJsonNode failure = (new ExceptionResponse()).failure("apPreferId not found in create policy request under transcation status info :" + createPolicyReqNode);
           exchange.getIn().setBody(failure);
           throw new ExecutionTerminator();
         } 
       } else {
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("Transcation status info node found in create policy request :" + createPolicyReqNode);
         exchange.getIn().setBody(failure);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in validate payment response details :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }

