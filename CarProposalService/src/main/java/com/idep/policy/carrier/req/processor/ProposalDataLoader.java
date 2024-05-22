 package com.idep.policy.carrier.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ProposalDataLoader implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(ProposalDataLoader.class.getName());
   
   CBService service = CBInstanceProvider.getServerConfigInstance();
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String inputReq = exchange.getIn().getBody().toString();
       exchange.setProperty("userPolicyProfileData", inputReq);
       JsonNode inputReqNode = this.objectMapper.readTree(inputReq);
       JsonNode proposalDataNode = null;
       try {
         proposalDataNode = this.objectMapper.readTree(((JsonObject)this.transService.getDocBYId(inputReqNode.findValue("proposalId").asText()).content()).toString());
         exchange.setProperty("logReq", "Car|" + proposalDataNode.findValue("carrierId") + "|" + "POLICY SIGN" + "|" + "|" + proposalDataNode.findValue("proposalId").asText() + "|");
         this.log.info(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "POLICYREQ" + "|" + "policy request received :" + inputReqNode.findValue("policyNo"));
       } catch (NullPointerException e) {
         this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CARPROPDATALOADER" + "|ERROR|" + "unable to fetch proposal document at the moment, retrying after 3 seconds:", e);
         Thread.sleep(3000L);
         proposalDataNode = this.objectMapper.readTree(((JsonObject)this.transService.getDocBYId(inputReqNode.get("proposalId").asText()).content()).toString());
       } 
       exchange.getIn().setBody(proposalDataNode);
     } catch (Exception e) {
       this.log.error(ProposalDataLoader.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + "BIKEPROPDATALOADER|ERROR|" + "Exception as carProposalDataLoader:", e);
       throw new ExecutionTerminator();
     } 
   }
 }


