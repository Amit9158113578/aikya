 package com.idep.proposal.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.policy.req.processor.AddRequestInformation;
 import com.idep.policy.req.processor.CarrierInformation;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class MasterPolicyReqProcessor extends AddRequestInformation implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(MasterPolicyReqProcessor.class.getName());
   
   CBService service = null;
   
   JsonNode serviceConfigNode = null;
   
   public void process(Exchange inExchange) throws Exception {
     try {
       JsonDocument docNode = Utils.serverConfig.getDocBYId("CarProposalValidationDocConfig");
       if (docNode == null) {
         inExchange.getIn().setBody((new ExceptionResponse()).configDocMissing());
         throw new ExecutionTerminator();
       } 
       if (this.service == null) {
         this.service = CBInstanceProvider.getServerConfigInstance();
         this.serviceConfigNode = Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("CarProposalServiceURLConfig").content()).toString());
       } 
       String inputmsg = (String)inExchange.getIn().getBody(String.class);
       JsonNode masterReqNode = this.objectMapper.readTree(inputmsg);
       this.log.info("masterReqNode :" + masterReqNode);
       ExtendedJsonNode policyValidationDocConfig = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)docNode.content()).toString()).get("policyRequest"));
       String requestString = (String)inExchange.getIn().getBody(String.class);
       ExtendedJsonNode requestNode = new ExtendedJsonNode(Utils.mapper.readTree(requestString));
       ExtendedJsonNode validatePayResponseForPolicy = validatePayResponseForPolicy(requestNode);
       this.log.info("requestNode :" + requestNode);
       if (!validatePayResponseForPolicy.has("responseCode")) {
         requestNode = addProposalInfo(requestNode);
         this.log.info("addProposalInfo :" + requestNode);
         if (!requestNode.has("responseCode")) {
           requestNode = addPaymentResponse(requestNode, policyValidationDocConfig);
           this.log.info("addPaymentResponse :" + requestNode);
           if (!requestNode.has("responseCode")) {
             requestNode = addCarQuoteRequest(requestNode, policyValidationDocConfig);
             this.log.info("addCarQuoteRequest :" + requestNode);
             if (!requestNode.has("responseCode")) {
               requestNode = carrierTransformRequest(requestNode, policyValidationDocConfig);
               this.log.info("carrierTransformRequest :" + requestNode);
               if (!requestNode.has("responseCode")) {
                 ExtendedJsonNode proposalRequest = requestNode.get("proposalInfo").get("proposalRequest");
                 proposalRequest = (new CarrierInformation()).preProcessing(proposalRequest, requestNode.getKey("carrierId"), policyValidationDocConfig);
                 this.log.info("CarrierInformation :" + requestNode);
                 if (!proposalRequest.has("responseCode")) {
                   requestNode.get("proposalInfo").put("proposalRequest", proposalRequest);
                   this.log.info("proposalInfo :" + requestNode);
                   if (!requestNode.has("responseCode")) {
                     inExchange.getIn().setHeader("invokePolicyService", "Y");
                     inExchange.getIn().setHeader("carPolicyService", this.serviceConfigNode.get("policyServiceURL").textValue());
                     inExchange.getIn().setBody(requestNode.toString());
                   } else {
                     inExchange.getIn().setBody(requestNode);
                   } 
                 } else {
                   inExchange.getIn().setBody(proposalRequest);
                 } 
               } else {
                 inExchange.getIn().setBody(requestNode);
               } 
             } else {
               inExchange.getIn().setBody(requestNode);
             } 
           } else {
             inExchange.getIn().setBody(requestNode);
           } 
         } else {
           inExchange.getIn().setBody(requestNode);
         } 
       } else {
         inExchange.getIn().setBody(validatePayResponseForPolicy);
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Master Policy Request Processor  :" + e.getMessage());
       inExchange.getIn().setBody(failure.toString());
       throw new ExecutionTerminator();
     } 
   }
 }


