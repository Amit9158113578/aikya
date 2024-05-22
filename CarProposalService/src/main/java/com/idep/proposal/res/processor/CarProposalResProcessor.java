 package com.idep.proposal.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarProposalResProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarProposalResProcessor.class.getName());
   
   JsonNode errorNode = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       String proposalResponse = (String)exchange.getIn().getBody(String.class);
       JsonNode proposalResNode = this.objectMapper.readTree(proposalResponse);
       this.log.info("encPropoasal Car :" + exchange.getProperty("encryptedProposalId"));
       if (exchange.getProperty("encryptedProposalId") != null) {
         ((ObjectNode)proposalResNode).put("encryptedProposalId", exchange.getProperty("encryptedProposalId").toString());
       } else {
         this.log.info("NOTE - Encrypted ProposalId for Car not sent in response ");
       } 
       ObjectNode obj = this.objectMapper.createObjectNode();
       obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").asInt());
       obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
       obj.put("data", proposalResNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
       exchange.getUnitOfWork().done(exchange);
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CARPRORESPROCESSOR" + "|ERROR|" + "car proposal response processor failed:", e);
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
       exchange.getUnitOfWork().done(exchange);
       throw new ExecutionTerminator();
     } 
   }
 }


