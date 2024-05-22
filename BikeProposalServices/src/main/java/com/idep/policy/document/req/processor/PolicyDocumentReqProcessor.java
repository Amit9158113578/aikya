 package com.idep.policy.document.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PolicyDocumentReqProcessor implements Processor {
   Logger log = Logger.getLogger(PolicyDocumentReqProcessor.class.getName());
   
   CBService proposalService = CBInstanceProvider.getPolicyTransInstance();
   
   CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
   
   public void process(Exchange exchange) {
     try {
       String policyDocRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode reqNode = Utils.mapper.readTree(policyDocRequest).get("data");
       JsonNode proposalDocNode = Utils.mapper.readTree(((JsonObject)this.proposalService.getDocBYId(reqNode.get("proposalId").asText()).content()).toString());
       ((ObjectNode)reqNode).put("requestType", "BikePolicyDocumentRequest");
       ((ObjectNode)reqNode).putAll((ObjectNode)proposalDocNode);
       exchange.setProperty("logReq", "Bike|" + proposalDocNode.findValue("carrierId").asText() + "|POLICY SIGN|" + proposalDocNode.findValue("policyNo").asText() + "|");
       exchange.setProperty("bikePolicyDocumentRequest", reqNode);
       exchange.setProperty("userPolicyProfileData", reqNode);
       exchange.getIn().setBody(reqNode);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYDOCREQPRO|ERROR|policy doc req processing failed :", e);
     } 
   }
 }


