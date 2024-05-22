 package com.idep.proposal.res.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import java.util.List;
 import java.util.Map;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class BreakInInspectionStatusCheckProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(BreakInInspectionStatusCheckProcessor.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     CBService transService = CBInstanceProvider.getPolicyTransInstance();
     JsonNode requestDocNode = this.objectMapper.readTree(exchange.getProperty("carrierInputRequest").toString());
     String proposalRequest = (String)exchange.getIn().getBody(String.class);
     JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
     if (requestDocNode.get("premiumDetails").get("policyType").asText().equals("renew") && 
       requestDocNode.has("breakInInspectionStatus") && requestDocNode.get("breakInInspectionStatus").asText().equalsIgnoreCase("false")) {
       String proposalId = "select meta().id as proposalId from PolicyTransaction where documentType=\"carProposalRequest\"and breakInInspectionStatus = 'false' and proposalRequest.proposerDetails.mobileNumber='{1}' and proposalRequest.vehicleDetails.registrationNumber='{2}' and meta().id NOT LIKE \"undefined\" order by proposalId desc limit 1";
       String proposalIdQuery = proposalId.replace("{1}", requestDocNode.findValue("mobileNumber").asText()).replace("{2}", requestDocNode.get("vehicleDetails").get("registrationNumber").asText());
       this.log.info("Executing Query for findind latest proposalId: " + proposalIdQuery);
       List<Map<String, Object>> list = transService.executeQueryCouchDB(proposalIdQuery);
       this.log.info("latest proposalId's list : " + list);
       if (!list.isEmpty()) {
         proposalId = ((Map)list.get(0)).get("proposalId").toString();
         this.log.info("latest proposalId: " + proposalId);
         JsonNode docInJson = this.objectMapper.readTree(((JsonObject)transService.getDocBYId(proposalId).content()).toString());
         if (docInJson.has("breakInReferenceNo")) {
           this.log.info("breakInRefNo found : " + docInJson.findValue("breakInReferenceNo").asText());
           ((ObjectNode)reqNode).put("breakInReferenceNo", docInJson.findValue("breakInReferenceNo").asText());
           exchange.setProperty("carrierInputRequest", this.objectMapper.writeValueAsString(reqNode));
         } else {
           ObjectNode obj = this.objectMapper.createObjectNode();
           obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("failureCode").asInt());
           obj.put("message", "ProposalId does Not contains BreakInReferenceNo");
           obj.put("data", "");
           exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
           exchange.getUnitOfWork().done(exchange);
           throw new ExecutionTerminator();
         } 
       } else {
         ObjectNode obj = this.objectMapper.createObjectNode();
         obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("failureCode").asInt());
         obj.put("message", "unable to find proposalId for respective mobileNo");
         obj.put("data", "");
         exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
         exchange.getUnitOfWork().done(exchange);
         throw new ExecutionTerminator();
       } 
       exchange.getIn().setHeader("breakInInspectionStatus", "False");
     } 
     exchange.getIn().setBody(reqNode);
   }
 }


