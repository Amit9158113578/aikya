package com.idep.proposal.req.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author shweta.joshi
 * Generate proposal sequence
 */
public class TravelProposalReqProcessor implements Processor
{
	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	static ObjectMapper objectMapper = new ObjectMapper();
	CBService quoteData =  CBInstanceProvider.getBucketInstance(ProposalConstants.QUOTE_BUCKET);
	static Logger log = Logger.getLogger(TravelProposalReqProcessor.class.getName());
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	static JsonNode keyConfigDoc;
	static String ExceptionHandlerQ = "ExceptionHandlerQ";


	static
	{
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		try {
			docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("LogConfiguration").content().toString());
			keyConfigDoc = objectMapper.readTree(((JsonObject)serverConfigService.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
		}
		catch(Exception e)
		{
			log.info("Failed to load Log Config Document"+e);
		}
	}

	CBService service =  CBInstanceProvider.getServerConfigInstance();

	public void process(Exchange exchange) {
		CamelContext camelContext = exchange.getContext();
		ProducerTemplate template = camelContext.createProducerTemplate();
		JsonNode reqNode = null;
		try

		{
			String encryptedProposalId =null;
			String encryptedQuoteId =null;
			String QUOTE_ID =null;
			String proposalRequest = (String)exchange.getIn().getBody(String.class);
			reqNode = objectMapper.readTree(proposalRequest);
			ObjectNode proposalIdNode = objectMapper.createObjectNode();
			/**
			 *  add request type (xpathmapper use this to load configuration)
			 */
			((ObjectNode)reqNode).put("requestType","TravelProposalRequest");

			try
			{
				synchronized(this)
				{
					// get proposal sequence from database
					long proposal_seq = this.service.updateDBSequence(ProposalConstants.PROPOSAL_SEQ);
					String proposalId = DocumentDataConfig.getConfigDocList().get(ProposalConstants.DOCID_CONFIG).get(ProposalConstants.TRAVELPROPOSAL_DOCELE).asText()+proposal_seq;
					encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, keyConfigDoc.get("encryptionKey").asText());
					log.info("Encrypted Car ProposalId : " + encryptedProposalId);
					((ObjectNode)proposalIdNode).put(ProposalConstants.ENCRYPT_PROPOSAL_ID,encryptedProposalId);
					((ObjectNode)reqNode).put(ProposalConstants.PROPOSAL_ID,proposalId);
					if(reqNode.has(ProposalConstants.QUOTE_ID)){
						QUOTE_ID = reqNode.findValue(ProposalConstants.QUOTE_ID).asText();
						JsonNode quoteIdDoc = objectMapper.readTree(((JsonObject)quoteData.getDocBYId(QUOTE_ID).content()).toString());
						if(quoteIdDoc.findValue(ProposalConstants.ENCRYPT_QUOTE_ID) != null && !quoteIdDoc.get(ProposalConstants.ENCRYPT_QUOTE_ID).asText().isEmpty()){
							encryptedQuoteId = quoteIdDoc.findValue(ProposalConstants.ENCRYPT_QUOTE_ID).asText();
							log.info("encryptedQuoteId to store in Travel ProposalId: "+encryptedQuoteId);
							proposalIdNode.put(ProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
						}
						else{
							encryptedQuoteId = GenrateEncryptionKey.GetEncryptedKey(QUOTE_ID, keyConfigDoc.get("encryptionKey").asText());
							log.info("Travel encryptedQuoteId generated : " + encryptedQuoteId);
							proposalIdNode.put(ProposalConstants.ENCRYPT_QUOTE_ID,encryptedQuoteId);
						}
					}
				}
			}
			catch(Exception e)
			{
				log.error("ProposalReqProcessor : Exception while updating proposal sequence in DB");
			}

			// set request configuration document id for sutrrMapper
			((ObjectNode)reqNode).put(ProposalConstants.EXTRA_FIELDS,proposalIdNode);
			exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, objectMapper.writeValueAsString(reqNode));
			JsonDocument configDocument = service.getDocBYId(ProposalConstants.TRAVEL_PROPOSAL_REQUEST+"-" + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
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
			exchange.getIn().setBody(reqNode);
			/**
			 * set default log data in property
			 */
			log.debug("reqNode in TravelProposalReqProcessor :"+ reqNode);
			exchange.setProperty(ProposalConstants.DEFAULT_LOG ,init(reqNode));
			exchange.setProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID, encryptedProposalId);
			exchange.setProperty(ProposalConstants.LOG_REQ, "Travel|"+reqNode.findValue("carrierId")+"|POLICY"+"|"+reqNode.findValue(ProposalConstants.PROPOSAL_ID).asText()+ "|");
		}
		catch (Exception e)
		{
			log.error("Exception at TravelProposalReqProcessor : ", e);

			String trace = "Error in Class :"+TravelProposalReqProcessor.class+"   Line Number :"+Thread.currentThread().getStackTrace()[0].getLineNumber();
			log.info("Erroror messgaes TravelProposalReqProcessor"+TravelProposalReqProcessor.class+"    "+Thread.currentThread().getStackTrace()[0].getLineNumber());
			String uri = "activemq:queue:" + ExceptionHandlerQ;
			((ObjectNode) reqNode).put("transactionName","TravelProposalReqProcessor");
			((ObjectNode) reqNode).put("Exception",e.toString());
			((ObjectNode) reqNode).put("ExceptionMessage",trace);
			exchange.getIn().setBody(reqNode.toString());
			log.info("sending to exception handler queue"+reqNode);
			exchange.setPattern(ExchangePattern.InOnly);
			template.send(uri, exchange);

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