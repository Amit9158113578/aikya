package com.idep.policyrenewprocessor;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class UserPolicyDetailsUpdateQuoteProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = null;
	CBService policyTransaction = null;
	String proposalDOC = null;
	String quoteIdDetails = null;
	JsonNode quoteNode = null;
	JsonNode proposalNode = null;
	ObjectNode responseNode = null;
	CBService quoteDataInstance = null;
	String mobileNumber;
	HashMap<Object,Object> userDetailsMap ;
	ObjectNode renewProposalIdNode = null;
	JsonNode quoteIdNode=null;

	public void process(Exchange exchange) throws Exception
	{
		Logger log = Logger.getLogger(UserPolicyDetailsUpdateQuoteProcessor.class.getName());
		policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		quoteDataInstance = CBInstanceProvider.getBucketInstance(PolicyRenewConstatnt.QUOTE_BUCKET);
		try
		{
		String proposalResponse = exchange.getIn().getBody().toString();
		JsonNode reqNode = this.objectMapper.readTree(proposalResponse);
		log.info("Its UserPolicyDetailsUpdateQuoteProcessor ");
		//Update QUOTE_ID in UserPolicyDetails
		JsonNode inputReqNode =  objectMapper.readTree(exchange.getProperty(PolicyRenewConstatnt.RENEW_QUOTE_INPUT_REQUEST).toString());
				
		if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null)
		{
			responseNode = objectMapper.createObjectNode();
			proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			//log.info("Printing proposalDOC :"+proposalDOC);
			proposalNode = objectMapper.readTree(proposalDOC);
			
		}
		if(reqNode.has("isResponseNull") && reqNode.get("isResponseNull").asText().equalsIgnoreCase("false")
				&& reqNode.get("QUOTE_ID").asText() != null && reqNode.get("businessLineId").asInt() == 4)
		{
			String docName = reqNode.get("QUOTE_ID").asText();
			JsonDocument QuoteDocId = quoteDataInstance.getDocBYId(docName);
			//quoteIdDetails = quoteDataInstance.getDocBYId(reqNode.get("QUOTE_ID").asText()).content().toString();
			//log.info("Printing QuoteDocId :"+QuoteDocId);
			quoteIdNode = objectMapper.readTree(QuoteDocId.content().toString());
			if(proposalNode.get("proposalRequest").get("proposerInfo").has("contactInfo") && quoteIdNode.has(PolicyRenewConstatnt.ENCRYPTED_QUOTEID))
			{
			renewProposalIdNode = objectMapper.createObjectNode();
			mobileNumber = proposalNode.get("proposalRequest").get("proposerInfo").get("contactInfo").get("mobile").asText();
			String docId = PolicyRenewConstatnt.USER_POLICY_DETAILS_CONFIG+proposalNode.get("proposalRequest").get("proposerInfo").get("contactInfo").get("mobile").asText();
			JsonNode userPolicyDetail = objectMapper.readTree(policyTransaction.getDocBYId(docId).content().toString());
			ArrayNode policyDetailsList = (ArrayNode) userPolicyDetail.get("policyDetails");
			ObjectNode renewDetailsNode = objectMapper.createObjectNode();
			for(JsonNode policyDetailNode: policyDetailsList)
			{
				if(policyDetailNode.get("proposalId").asText().equalsIgnoreCase(inputReqNode.findValue("proposalId").asText()))
				{
					if(policyDetailNode.has("renewPolicyDetails"))
					{
						((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.QUOTE_ID,reqNode.get("QUOTE_ID").asText());
						((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.ENCRYPTED_QUOTEID,quoteIdNode.findValue("encryptedQuoteId").asText());
						
					}
					else
					{
						renewDetailsNode.put(PolicyRenewConstatnt.RENEW_RUN,"Y");
						renewDetailsNode.put(PolicyRenewConstatnt.QUOTE_ID,reqNode.get("QUOTE_ID").asText());
						renewDetailsNode.put(PolicyRenewConstatnt.ENCRYPTED_QUOTEID,quoteIdNode.findValue("encryptedQuoteId").asText());
						((ObjectNode)policyDetailNode).put("renewPolicyDetails", renewDetailsNode);
					}
				}
			}
			JsonObject docObj = JsonObject.fromJson(userPolicyDetail.toString());
			policyTransaction.replaceDocument(docId,docObj );
			log.info("Successfully updated QUOTEID in userPolicyDetail:"+docId );
		}
			exchange.getIn().setBody(proposalResponse);
		}
		else if(reqNode.has("isResponseNull") && reqNode.get("isResponseNull").asText().equalsIgnoreCase("false")
				&& reqNode.get("QUOTE_ID").asText() != null)
		{
			quoteIdNode = objectMapper.readTree(((JsonObject)quoteDataInstance.getDocBYId(reqNode.get("QUOTE_ID").asText()).content()).toString());
			if(proposalNode.get("proposalRequest").has("proposerDetails") && quoteIdNode.has(PolicyRenewConstatnt.ENCRYPTED_QUOTEID))
			{
			renewProposalIdNode = objectMapper.createObjectNode();
			mobileNumber = proposalNode.get("proposalRequest").get("proposerDetails").get("mobileNumber").asText();
			String docId = PolicyRenewConstatnt.USER_POLICY_DETAILS_CONFIG+proposalNode.get("proposalRequest").get("proposerDetails").get("mobileNumber").asText();
			JsonNode userPolicyDetail = objectMapper.readTree(policyTransaction.getDocBYId(docId).content().toString());
			ArrayNode policyDetailsList = (ArrayNode) userPolicyDetail.get("policyDetails");
			ObjectNode renewDetailsNode = objectMapper.createObjectNode();
			for(JsonNode policyDetailNode: policyDetailsList)
			{
				if(policyDetailNode.get("proposalId").asText().equalsIgnoreCase(inputReqNode.findValue("proposalId").asText()))
				{
					if(policyDetailNode.has("renewPolicyDetails"))
					{
						((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.QUOTE_ID,reqNode.get("QUOTE_ID").asText());
						((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.ENCRYPTED_QUOTEID,quoteIdNode.findValue("encryptedQuoteId").asText());
						
					}
					else
					{
						renewDetailsNode.put(PolicyRenewConstatnt.RENEW_RUN,"Y");
						renewDetailsNode.put(PolicyRenewConstatnt.QUOTE_ID,reqNode.get("QUOTE_ID").asText());
						renewDetailsNode.put(PolicyRenewConstatnt.ENCRYPTED_QUOTEID,quoteIdNode.findValue("encryptedQuoteId").asText());
						((ObjectNode)policyDetailNode).put("renewPolicyDetails", renewDetailsNode);
					}
				}
			}
			JsonObject docObj = JsonObject.fromJson(userPolicyDetail.toString());
			policyTransaction.replaceDocument(docId,docObj );
			log.info("Successfully updated QUOTEID in userPolicyDetail:"+docId );
		}
			exchange.getIn().setBody(proposalResponse);
		}
	}
		catch(Exception e)
		{
			log.error("|ERROR| UserPolicyDetailsUpdateQuoteProcessor processor failed:",e);
	        throw new ExecutionTerminator();
		}
	}
}
