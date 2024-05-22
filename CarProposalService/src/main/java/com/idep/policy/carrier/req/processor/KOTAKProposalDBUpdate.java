 package com.idep.policy.carrier.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class KOTAKProposalDBUpdate implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(KOTAKProposalDBUpdate.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     try {
       String inputReq = exchange.getIn().getBody().toString();
       JsonNode carrierPropReq = this.objectMapper.readTree(inputReq);
       String proposalId = carrierPropReq.findValue("TransactionID").asText();
       ObjectNode objNode = this.objectMapper.createObjectNode();
       ((ObjectNode)carrierPropReq).put("proposalId", proposalId);
       ((ObjectNode)carrierPropReq).put("documentType", "carrierPolicyUpdateReq");
       objNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText());
       objNode.put("message", "success");
       objNode.put("data", carrierPropReq);
       objNode.put("status", "success");
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objNode));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "KOTAKPRODBUPDATE" + "|ERROR|" + "Exception at EmailTemplateLoader:", e);
     } 
   }
 }


