 package com.idep.policy.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class TransformPolicyRequest
   implements Processor {
   Logger log = Logger.getLogger(TransformPolicyRequest.class);
   
   ExtendedJsonNode configDoc = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       this.configDoc = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("CarrierRequestConfiguration").content()).toString()));
       ExtendedJsonNode requestTransNode = new ExtendedJsonNode((JsonNode)Utils.mapper.createObjectNode());
       ExtendedJsonNode requestNode = new ExtendedJsonNode(Utils.mapper.readTree(exchange.getIn().getBody().toString()));
       exchange.getIn().getHeaders().clear();
       exchange.getIn().setHeader("CamelHttpMethod", "POST");
       exchange.getIn().setHeader("content-type", "application/json");
       if (!this.configDoc.has(requestNode.findValueAsText("carrierId"))) {
         exchange.getIn().setBody((new ExceptionResponse()).configDocMissing(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ConfigDocMissingMsg").asText())) + " for carrierId :" + requestNode.findValueAsText("carrierId")));
         throw new ExecutionTerminator();
       } 
       exchange.getIn().setHeader("webserviceType", this.configDoc.get(requestNode.findValueAsText("carrierId")).getKey("webserviceType"));
       exchange.getIn().setHeader("requestURL", this.configDoc.get(requestNode.findValueAsText("carrierId")).getKey(exchange.getIn().getHeader("webserviceType").toString()));
       requestNode = uptPolicyNoSeqAndWebSerType(requestNode, exchange);
       requestTransNode.put("lob", requestNode.getKey("businessLineId"));
       requestTransNode.put("request", requestNode);
       if (this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("policy") > 0) {
         exchange.getIn().setHeader("invokeservice", "True");
         exchange.setProperty("configDoc", this.configDoc);
         exchange.getIn().setBody(requestTransNode);
       } else {
         exchange.getIn().setHeader("invokeservice", "False");
         exchange.setProperty("carrierReqMapConf", "BikePolicyRESCONF-" + requestNode.findValueAsText("carrierId") + "-" + requestNode
             .findValueAsText("productId"));
         exchange.getIn().setHeader("carrierReqMapConf", "Yes");
         exchange.getIn().setBody(requestNode);
       } 
       exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("policy")));
       if (requestNode.get("proposalInfo").get("bikeProposalResponse").has("renewalPlan") && requestNode.get("proposalInfo").get("bikeProposalResponse").asBoolean("renewalPlan")) {
         this.log.info("Web Service Type is set as REST");
         exchange.getIn().setHeader("webserviceType", "REST");
         exchange.getIn().setHeader("requestURL", this.configDoc.get(requestNode.findValueAsText("carrierId")).getKey(exchange.getIn().getHeader("webserviceType").toString()));
         exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(2));
       } 
       exchange.setProperty("request", requestTransNode);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in transform policy request processor :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
   
   public ExtendedJsonNode uptPolicyNoSeqAndWebSerType(ExtendedJsonNode requestNode, Exchange exchange) throws Exception {
     try {
       if (requestNode.findValue("carrierId") != null) {
         String asText = requestNode.findValue("carrierId").asText();
         ExtendedJsonNode extendedJsonNode = this.configDoc.get(asText);
         if (extendedJsonNode.has("addPolicyNoSequence")) {
           if (extendedJsonNode.get("addPolicyNoSequence").has("sequenceUpdRequired") && extendedJsonNode.get("addPolicyNoSequence").getKey("sequenceUpdRequired").equalsIgnoreCase("Y")) {
             String sequenceDocId = extendedJsonNode.get("addPolicyNoSequence").getKey("sequenceDocId");
             long updateDBSequence = Utils.serverConfig.updateDBSequence(sequenceDocId);
             requestNode.get("proposalInfo").putLong("policySequenceNo", Long.valueOf(updateDBSequence));
           } 
           if (extendedJsonNode.get("addPolicyNoSequence").has("policyWebserviceType")) {
             exchange.getIn().setHeader("webserviceType", extendedJsonNode.get("addPolicyNoSequence").getKey("policyWebserviceType"));
             exchange.getIn().setHeader("requestURL", extendedJsonNode.get("addPolicyNoSequence").getKey(exchange.getIn().getHeader("webserviceType").toString()));
           } 
           return requestNode;
         } 
         return requestNode;
       } 
       return requestNode;
     } catch (Exception e) {
       this.log.error("error in update policy no squence method for carrier request :");
       return requestNode;
     } 
   }
 }


