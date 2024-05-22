 package com.idep.policy.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class MasterPolicyReqProcessor
   extends AddRequestInformation implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(MasterPolicyReqProcessor.class.getName());
   
   CBService service = null;
   
   JsonNode serviceConfigNode = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode serviceConfigNode = Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("BikeProposalServiceURLConfig").content()).toString());
       JsonDocument docNode = Utils.serverConfig.getDocBYId("ProposalValidationDocConfig");
       if (docNode == null) {
         exchange.getIn().setBody((new ExceptionResponse()).configDocMissing());
         throw new ExecutionTerminator();
       } 
       ExtendedJsonNode policyValidationDocConfig = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)docNode.content()).toString()).get("policyRequest"));
       String requestString = (String)exchange.getIn().getBody(String.class);
       ExtendedJsonNode requestNode = new ExtendedJsonNode(Utils.mapper.readTree(requestString));
       this.log.info("requestNode :" + requestNode);
       ExtendedJsonNode validatePayResponseForPolicy = validatePayResponseForPolicy(requestNode);
       if (!validatePayResponseForPolicy.has("responseCode")) {
         requestNode = addProposalInfo(requestNode);
         if (!requestNode.has("responseCode")) {
           requestNode = addPaymentResponse(requestNode, policyValidationDocConfig);
           if (!requestNode.has("responseCode")) {
             requestNode = addBikeQuoteRequest(requestNode, policyValidationDocConfig);
             if (!requestNode.has("responseCode")) {
               requestNode = carrierTransformRequest(requestNode, policyValidationDocConfig);
               if (!requestNode.has("responseCode")) {
                 ExtendedJsonNode proposalRequest = requestNode.get("proposalInfo").get("proposalRequest");
                 proposalRequest = (new CarrierInformation()).preProcessing(proposalRequest, requestNode.getKey("carrierId"), policyValidationDocConfig);
                 if (!proposalRequest.has("responseCode")) {
                   requestNode.get("proposalInfo").put("proposalRequest", proposalRequest);
                   if (!requestNode.has("responseCode")) {
                     exchange.getIn().setHeader("invokePolicyService", "Y");
                     exchange.getIn().setHeader("bikePolicyService", serviceConfigNode.get("policyServiceURL").textValue());
                     exchange.getIn().setBody(requestNode.toString());
                   } else {
                     exchange.getIn().setBody(requestNode);
                   } 
                 } else {
                   exchange.getIn().setBody(proposalRequest);
                 } 
               } else {
                 exchange.getIn().setBody(requestNode);
               } 
             } else {
               exchange.getIn().setBody(requestNode);
             } 
           } else {
             exchange.getIn().setBody(requestNode);
           } 
         } else {
           exchange.getIn().setBody(requestNode);
         } 
       } else {
         exchange.getIn().setBody(validatePayResponseForPolicy);
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Master Policy Request Processor  :" + e.getMessage());
       exchange.getIn().setBody(failure.toString());
       throw new ExecutionTerminator();
     } 
   }
 }


