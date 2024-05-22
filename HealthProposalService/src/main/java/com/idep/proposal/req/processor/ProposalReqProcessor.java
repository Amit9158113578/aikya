package com.idep.proposal.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * 
 * @author sandeep.jadhav
 * Generate proposal sequence
 */
public class ProposalReqProcessor implements Processor

{

	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(ProposalReqProcessor.class.getName());
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	static
	{
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		try {
			docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("LogConfiguration").content().toString());
		}
		catch(Exception e)
		{
			log.info("Failed to load Log Config Document"+e);
		}
	}
 
	CBService service =  CBInstanceProvider.getServerConfigInstance();
	

	public void process(Exchange exchange) {

		try
		{
			String proposalRequest = (String)exchange.getIn().getBody(String.class);
			JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
			String encryptedProposalId = null ;
			String encryptedQuoteId = null;
			JsonNode keyConfigDoc = objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
			/**
			 *  add request type (xpathmapper use this to load configuration)
			 */
			((ObjectNode)reqNode).put("requestType","HealthProposalRequest");

			try
			{
				synchronized(this)
				{
					// get proposal sequence from database
					String proposalId=null;
					String QUOTE_ID = null;
						 long proposal_seq = this.service.updateDBSequence(ProposalConstants.PROPOSAL_SEQ);
							proposalId = DocumentDataConfig.getConfigDocList().get(ProposalConstants.DOCID_CONFIG).get(ProposalConstants.HEALTHPROPOSAL_DOCELEMENT).asText()+proposal_seq;
							
							if(reqNode.has(ProposalConstants.QUOTE_ID)){
								QUOTE_ID = reqNode.findValue(ProposalConstants.QUOTE_ID).asText();
								JsonNode quoteIdDoc = objectMapper.readTree(((JsonObject)quoteData.getDocBYId(QUOTE_ID).content()).toString());
								if(quoteIdDoc.findValue(ProposalConstants.ENCRYPT_QUOTE_ID) != null && !quoteIdDoc.get(ProposalConstants.ENCRYPT_QUOTE_ID).asText().isEmpty()){
									encryptedQuoteId = quoteIdDoc.findValue(ProposalConstants.ENCRYPT_QUOTE_ID).asText();
									log.info("encryptedQuoteId to store in Health ProposalId: "+encryptedQuoteId);
									((ObjectNode)reqNode).put(ProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
								}
								else{
									encryptedQuoteId = GenrateEncryptionKey.GetEncryptedKey(QUOTE_ID, keyConfigDoc.get("encryptionKey").asText());
								    log.info("Health encryptedQuoteId generated : " + encryptedQuoteId);
								    ((ObjectNode)reqNode).put(ProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
								}
							}
							
					          encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, keyConfigDoc.get("encryptionKey").asText());
					          log.info("encrypted Health ProposalId : " + encryptedProposalId);
					          ((ObjectNode)reqNode).put(ProposalConstants.ENCRYPT_PROPOSAL_ID,encryptedProposalId);
					          ((ObjectNode)reqNode).put(ProposalConstants.PROPOSAL_ID,proposalId);
					          
				}

			}
			catch(Exception e)
			{
				this.log.error("ProposalReqProcessor : Exception while updating proposal sequence in DB");
			}

			// set request configuration document id for sutrrMapper
			exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, this.objectMapper.writeValueAsString(reqNode));
			exchange.setProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID, encryptedProposalId);

			/*  exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.PROPOSALCONF_REQ + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
      "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());*/

			JsonDocument configDocument = service.getDocBYId(reqNode.get(ProposalConstants.PROPOSALREQTYPE).asText()+"-" + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
					"-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
			log.debug("configDocument:" +configDocument);

			if(configDocument!=null)
			{
				JsonNode configDocNode = objectMapper.readTree(configDocument.content().toString());
				exchange.setProperty(ProposalConstants.PROPOSALREQ_CONFIG,configDocNode);
			}

			String baseEnvStatus = "false";
			if(reqNode.has(ProposalConstants.BASE_ENV_STATUS)){
				if(reqNode.get(ProposalConstants.BASE_ENV_STATUS).asBoolean()){
					baseEnvStatus = reqNode.get(ProposalConstants.BASE_ENV_STATUS).asText();
				}
			}

			exchange.getIn().setHeader(ProposalConstants.BASE_ENV_STATUS, baseEnvStatus);
			// set input request as property for sutrrMapper
			LeadProfileRequest.sendLeadProfileRequest(reqNode, exchange);
			exchange.getIn().setBody(reqNode);
			 /**
			  * set default log data in property
			  */
			exchange.setProperty(ProposalConstants.DEFAULT_LOG ,init(reqNode));
			exchange.setProperty(ProposalConstants.LOG_REQ, "Health|"+reqNode.findValue("carrierId")+"|POLICY"+"|"+reqNode.findValue(ProposalConstants.PROPOSAL_ID).asText()+ "|");
		}
		catch (Exception e)
		{
			this.log.error("Exception at ProposalReqProcessor : ", e);
		}
	}
	  public String init(JsonNode quoteReqNode)
			{
		  	String logData = new String();
			ArrayNode logNode = (ArrayNode)docConfigNode.get("logFields");
			String seperator = docConfigNode.get("seperateBy").asText();
			try {
			if(docConfigNode.has("defaultValue"))	
			{
				logData = logData.concat(docConfigNode.get("defaultValue").asText());
				logData = logData.concat(seperator);
			}	
			
			for(JsonNode node : logNode)
				{
					if(quoteReqNode.findPath(node.asText()) == null)
					{
						logData = logData.concat(seperator);
					}
					else
					{
						if(node.asText().equalsIgnoreCase("quoteType")) 
						{
							if(docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText())!= null)
							{
						
								logData = logData.concat(docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText()).asText());
								logData = logData.concat(seperator);
							}
							else
							{
								logData = logData.concat(seperator);
								
							}
						}
						
						else
						{
							logData = logData.concat(quoteReqNode.findPath(node.asText()).asText());	
							logData = logData.concat(seperator);
						}
					}
				}
			}
			catch(Exception e)
			{
				log.error("Error occurred while processing logging details ",e);
			}
			return logData;
	    }
}
