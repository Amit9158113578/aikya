 package com.idep.proposal.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.encryption.session.GenrateEncryptionKey;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class BikeProposalReqProcessor implements Processor {
   static Logger log = Logger.getLogger(BikeProposalReqProcessor.class.getName());
   
   static JsonNode docConfigNode = (JsonNode)Utils.mapper.createObjectNode();
   
   String encryptedProposalId = null;
   
   static {
     try {
       docConfigNode = Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("LogConfiguration").content()).toString());
     } catch (Exception e) {
       log.info("Failed to load Log Config Document" + e);
     } 
   }
   
   public void process(Exchange exchange) throws Exception {
     String proposalRequest = (String)exchange.getIn().getBody(String.class);
     JsonNode reqNode = Utils.mapper.readTree(proposalRequest);
     try {
       synchronized (this) {
         long proposal_seq = Utils.serverConfig.updateDBSequence("SEQBIKEPROPOSAL");
         if (proposal_seq == -1L) {
           proposal_seq = Utils.serverConfig.updateDBSequence("SEQBIKEPROPOSAL");
           if (proposal_seq == -1L)
             proposal_seq = Utils.serverConfig.updateDBSequence("SEQBIKEPROPOSAL"); 
         } 
         String proposalId = String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("bikeproposal").asText())) + proposal_seq;
         ExtendedJsonNode keyConfigDoc = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString()));
         this.encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, keyConfigDoc.getKey("encryptionKey"));
         if (reqNode.has("encryptedProposalId") && reqNode.get("encryptedProposalId") != null)
           this.encryptedProposalId = reqNode.get("encryptedProposalId").asText(); 
         ((ObjectNode)reqNode).put("proposalId", proposalId);
         ((ObjectNode)reqNode).put("encryptedProposalId", this.encryptedProposalId);
         exchange.setProperty("defaultLog", init(reqNode));
         exchange.setProperty("proposalId", proposalId);
       } 
       exchange.setProperty("carrierInputRequest", Utils.mapper.writeValueAsString(reqNode));
       exchange.setProperty("encryptedProposalId", this.encryptedProposalId);
       LeadProfileRequest.sendLeadProfileRequest(reqNode, exchange);
       exchange.getIn().setBody(reqNode);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Bike Proposal Req Processor class  :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
   
   public String init(JsonNode quoteReqNode) {
     String logData = new String();
     log.info("docConfigNode" + docConfigNode);
     ArrayNode logNode = (ArrayNode)docConfigNode.get("logFields");
     String seperator = docConfigNode.get("seperateBy").asText();
     log.info("Seperator :" + seperator);
     try {
       if (docConfigNode.has("defaultValue")) {
         logData = logData.concat(docConfigNode.get("defaultValue").asText());
         logData = logData.concat(seperator);
       } 
       for (JsonNode node : logNode) {
         if (quoteReqNode.findPath(node.asText()) == null) {
           logData = logData.concat(seperator);
           log.info("finding in log :" + logData);
           continue;
         } 
         if (node.asText().equalsIgnoreCase("quoteType")) {
           if (docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText()) != null) {
             logData = logData.concat(docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText()).asText());
             logData = logData.concat(seperator);
           } 
           continue;
         } 
         logData = logData.concat(quoteReqNode.findPath(node.asText()).asText());
         logData = logData.concat(seperator);
       } 
       log.info("outside BikeProposalReqProcessor" + seperator);
     } catch (Exception e) {
       log.error("Error occurred while processing logging details ", e);
     } 
     return logData;
   }
 }

