 package com.idep.proposal.req.processor;
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
 
 public class TransformServiceRequest implements Processor {
   Logger log = Logger.getLogger(TransformServiceRequest.class);
   
   ExtendedJsonNode configDoc = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       this.configDoc = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("CarCarrierRequestConfiguration").content()).toString()));
       ExtendedJsonNode requestTransNode = new ExtendedJsonNode((JsonNode)Utils.mapper.createObjectNode());
       ExtendedJsonNode requestNode = new ExtendedJsonNode(Utils.mapper.readTree(exchange.getIn().getBody().toString()));
       requestTransNode.put("lob", requestNode.getKey("businessLineId"));
       requestTransNode.put("request", requestNode);
       exchange.getIn().getHeaders().clear();
       exchange.getIn().setHeader("CamelHttpMethod", "POST");
       exchange.getIn().setHeader("content-type", "application/json");
       if (!this.configDoc.has(requestNode.findValueAsText("carrierId"))) {
         exchange.getIn().setBody((new ExceptionResponse()).configDocMissing(String.valueOf(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ConfigDocMissingMsg").asText()))) + " for carrierId :" + requestNode.findValueAsText("carrierId")));
         throw new ExecutionTerminator();
       } 
       exchange.getIn().setHeader("webserviceType", this.configDoc.get(requestTransNode.findValueAsText("carrierId")).getKey("webserviceType"));
       exchange.getIn().setHeader("requestURL", this.configDoc.get(requestTransNode.findValueAsText("carrierId")).getKey(exchange.getIn().getHeader("webserviceType").toString()));
       if (this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("proposal") > 0) {
         exchange.getIn().setHeader("invokeservice", "True");
         exchange.setProperty("configDoc", this.configDoc);
         if (this.configDoc.get(requestTransNode.findValueAsText("carrierId")).has("carrierReqMapPaymentRequired") && this.configDoc.get(requestTransNode.findValueAsText("carrierId")).asText("carrierReqMapPaymentRequired").equalsIgnoreCase("Yes")) {
           exchange.setProperty("carrierReqMapConf", "CarProposalRESCONF-" + requestNode.findValueAsText("carrierId") + "-" + 
               requestNode.findValueAsText("productId"));
           requestNode.get("proposerDetails").put("proposalId", requestNode.getKey("proposalId"));
           exchange.getIn().setHeader("carrierReqMapConf", "Yes");
         } 
         exchange.getIn().setBody(requestTransNode);
       } else {
         exchange.getIn().setHeader("invokeservice", "False");
         exchange.setProperty("carrierReqMapConf", "CarProposalRESCONF-" + requestNode.findValueAsText("carrierId") + "-" + 
             requestNode.findValueAsText("productId"));
         requestNode.get("proposerDetails").put("proposalId", requestNode.getKey("proposalId"));
         exchange.getIn().setHeader("carrierReqMapConf", "Yes");
         exchange.getIn().setBody(requestNode);
       } 
       if (requestNode.findValue("previousPolicyExpired") != null && requestNode.findValue("previousPolicyExpired").asText().equalsIgnoreCase("Y")) {
         if (requestNode.findValue("inspectionReferenceNo") == null) {
           if (this.configDoc.get(requestTransNode.findValueAsText("carrierId")).has("expiredPolicyAllow") && this.configDoc.get(requestTransNode.findValueAsText("carrierId")).getKey("expiredPolicyAllow").equals("Y")) {
             exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).asInt("invokeWithExpireService")));
           } else {
             exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("proposal")));
           } 
         } else if (this.configDoc.get(requestTransNode.findValueAsText("carrierId")).has("checkInspectionStatus") && this.configDoc.get(requestTransNode.findValueAsText("carrierId")).getKey("checkInspectionStatus").equals("Y")) {
           this.log.info("Need to check Inspection status Before payment");
           exchange.getIn().setHeader("checkInspectionStatus", "Y");
           exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("proposal")));
           exchange.setProperty("PostInspectionRequest", requestTransNode);
         } else {
           exchange.getIn().setHeader("checkInspectionStatus", "N");
           exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).asInt("invokeWithExpireService")));
         } 
       } else if (requestNode.has("insuranceDetails") && requestNode.get("insuranceDetails").has("insurerId") && requestNode.get("insuranceDetails").asText("insurerId").equalsIgnoreCase("25") && 
         requestNode.findValueAsText("carrierId") == "25") {
         exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("renewalProposal")));
       } else {
         exchange.getIn().setHeader("noOfServiceInvoke", Integer.valueOf(this.configDoc.get(requestTransNode.findValueAsText("carrierId")).get("noOfServiceInvoke").asInt("proposal")));
       } 
       this.log.info("No of Service to be invoke : " + exchange.getIn().getHeader("noOfServiceInvoke"));
       exchange.setProperty("request", requestTransNode);
     } catch (Exception e) {
       this.log.error(e);
       e.printStackTrace();
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in transform proposal request processor :" + e.getMessage());
       exchange.getIn().setBody(failure);
     } 
   }
 }


