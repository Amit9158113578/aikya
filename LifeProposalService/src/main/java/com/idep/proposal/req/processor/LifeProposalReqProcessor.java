package com.idep.proposal.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;
import com.idep.encryption.session.GenrateEncryptionKey;
import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class LifeProposalReqProcessor implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(LifeProposalReqProcessor.class.getName());
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  CBService transService = CBInstanceProvider.getBucketInstance("QuoteData");
  
  public void process(Exchange exchange) throws Exception
  {
    try
    {
      String proposalRequest = (String)exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
      //this.log.info("reqNode Contains Following Data:" + reqNode);
      ObjectNode quoteIdDetails = getQuoteIdDetails(reqNode);
      String encryptedQuoteId = null;
      String encryptedProposalId = null ;
      String QUOTE_ID = null;
      synchronized (this)
      {
        long proposal_seq = this.serverConfig.updateDBSequence("SEQLIFEPROPOSAL");
        ObjectNode proposalIdNode = this.objectMapper.createObjectNode();
        String proposalId = DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("lifeproposal").asText() + proposal_seq;
       
        if(reqNode.has(ProposalConstants.QUOTE_ID)){
			QUOTE_ID = reqNode.findValue(ProposalConstants.QUOTE_ID).asText();
			JsonNode quoteIdDoc = objectMapper.readTree(((JsonObject)transService.getDocBYId(QUOTE_ID).content()).toString());
			encryptedQuoteId = quoteIdDoc.findValue(ProposalConstants.ENCRYPT_QUOTE_ID).asText();
			log.info("encryptedQuoteId to store in Health ProposalId: "+encryptedQuoteId);
		}
        JsonNode keyConfigDoc = objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
        encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, keyConfigDoc.get("encryptionKey").asText());
        log.info("encrypted Health ProposalId : " + encryptedProposalId);
        proposalIdNode.put("proposalId", proposalId);
        ((ObjectNode)reqNode).put(ProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
        ((ObjectNode)reqNode).put(ProposalConstants.ENCRYPT_PROPOSAL_ID,encryptedProposalId);
        ((ObjectNode)reqNode).put("proposalId", proposalId);
        
        long carrierId = reqNode.findValue("carrierId").asLong();
        long productId = reqNode.findValue("productId").asLong();
        
        exchange.setProperty("proposalId", proposalId);
        exchange.setProperty("carrierId", Long.valueOf(carrierId));
        exchange.setProperty("productId", Long.valueOf(productId));
        exchange.setProperty("logReq", "Life|" + reqNode.findValue("carrierId") + "|" + "PROPOSAL" + "|" + reqNode.findValue("proposalId").asText() + "|");
      }
      ((ObjectNode)reqNode).put("quoteParam", quoteIdDetails.get("quoteParam"));
      
      exchange.setProperty("carrierInputRequest", this.objectMapper.writeValueAsString(reqNode));
      exchange.setProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID, encryptedProposalId);
      exchange.setProperty("carrierReqMapConf", "LifeProposalREQCONF-" + reqNode.get("carrierId").intValue() + 
        "-" + reqNode.get("productId").intValue());
      exchange.setProperty("LifeProposalRequestConfigDoc", "LifeProposalRequest-" + reqNode.get("carrierId").intValue() + 
        "-" + reqNode.get("productId").intValue());
      
      String baseEnvStatus = "false";
      if ((reqNode.has("baseEnvStatus")) && 
        (reqNode.get("baseEnvStatus").asBoolean())) {
        baseEnvStatus = reqNode.get("baseEnvStatus").asText();
      }
      
      
      exchange.setProperty("LifeProposalRequest", reqNode);
      exchange.getIn().setHeader("baseEnvStatus", baseEnvStatus);
      LeadProfileRequest.sendLeadProfileRequest(reqNode, exchange);
      exchange.getIn().setBody(reqNode);
    }
    
    catch (Exception e)
    {
      this.log.error("failed to retrive quote infomation :", e);
      throw new ExecutionTerminator();
    }
  }
  
  public ObjectNode getQuoteIdDetails(JsonNode reqNode)
    throws ExecutionTerminator, JsonProcessingException, IOException
  {
    ObjectNode quoteIdNode = this.objectMapper.createObjectNode();
    JsonNode quoteReqInfoNode = this.objectMapper.readTree(((JsonObject)this.transService.getDocBYId(reqNode.get("QUOTE_ID").textValue()).content()).toString());
    this.log.info("quoteReqInfoNode Contains following data: " + quoteReqInfoNode);
    quoteIdNode.put("quoteParam", quoteReqInfoNode.get("lifeQuoteRequest").get("quoteParam"));
    return quoteIdNode;
  }
}
