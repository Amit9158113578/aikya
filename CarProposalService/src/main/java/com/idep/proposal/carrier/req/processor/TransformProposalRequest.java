 package com.idep.proposal.carrier.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.policy.req.processor.AddRequestInformation;
 import com.idep.policy.req.processor.CarrierInformation;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class TransformProposalRequest extends AddRequestInformation implements Processor {
   public void process(Exchange exchange) throws Exception {
     Logger logger = Logger.getLogger(TransformProposalRequest.class);
     try {
       JsonNode serviceConfigNode = Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("CarProposalServiceURLConfig").content()).toString());
       JsonDocument docNode = Utils.serverConfig.getDocBYId("CarProposalValidationDocConfig");
       if (docNode == null) {
         exchange.getIn().setBody((new ExceptionResponse()).configDocMissing());
         throw new ExecutionTerminator();
       } 
       JsonNode jsonNode = Utils.mapper.readTree(((JsonObject)docNode.content()).toString()).get("proposalRequest");
       ExtendedJsonNode ProposalValidationDocConfig = new ExtendedJsonNode(jsonNode);
       String requestString = (String)exchange.getIn().getBody(String.class);
       ExtendedJsonNode requestNode = new ExtendedJsonNode(Utils.mapper.readTree(requestString));
       try {
         String deviceId = exchange.getIn().getHeader("deviceId").toString();
         requestNode.put("deviceId", deviceId);
       } catch (Exception e) {
         logger.error("deviceId not found in header :");
       } 
       requestNode = addPremiumDetails(requestNode, ProposalValidationDocConfig);
       if (!requestNode.has("responseCode")) {
         requestNode = addCarQuoteRequest(requestNode, ProposalValidationDocConfig);
         if (!requestNode.has("responseCode")) {
           requestNode = carrierTransformRequest(requestNode, ProposalValidationDocConfig);
           if (!requestNode.has("responseCode")) {
             requestNode = validatePolicyDates(requestNode);
             if (!requestNode.has("responseCode")) {
               requestNode = (new CarrierInformation()).preProcessing(requestNode, requestNode.getKey("carrierId"), ProposalValidationDocConfig);
               if (!requestNode.has("responseCode")) {
                 exchange.getIn().setHeader("requoteCalculationFlag", "N");
                 exchange.getIn().setHeader("carproposalService", serviceConfigNode.get("proposalServiceURL").textValue());
                 exchange.getIn().setBody(requestNode.toString());
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
         exchange.getIn().setBody(requestNode);
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Transform Proposal Request class  :" + e.getMessage());
       exchange.getIn().setBody(failure.toString());
       throw new ExecutionTerminator();
     } 
   }
 }


