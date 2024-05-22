package com.idep.policyrenewprocessor;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class UserPolicyDetailsUpdateProcesssor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = null;
	CBService policyTransaction = null;
	String proposalDOC = null;
	JsonNode quoteNode = null;
	JsonNode proposalNode = null;
	String renewproposalId = null;
	//responseNode = null;
	//responseNode = null;
	Logger log = Logger.getLogger(UserPolicyDetailsUpdateProcesssor.class.getName());
	CBService quoteDataInstance = null;
	String mobileNumber;
	HashMap<Object,Object> userDetailsMap ;
	
	public void process(Exchange exchange) throws Exception 
	{
		Logger log = Logger.getLogger(UserPolicyDetailsUpdateProcesssor.class.getName());
		policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		ObjectNode responseNode = objectMapper.createObjectNode();
		
		try
		{
		String proposalResponse = exchange.getIn().getBody().toString();
		JsonNode reqNode = this.objectMapper.readTree(proposalResponse);
		log.info("Its UserPolicyDetailsUpdateProcesssor ");
		JsonNode inputReqNode =  objectMapper.readTree(exchange.getProperty(PolicyRenewConstatnt.INPUT_REQUEST).toString());
		  
		if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null && reqNode.get("businessLineId").asInt() == 4)
		{
			renewproposalId = reqNode.get("proposalId").asText();
			proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			proposalNode = objectMapper.readTree(proposalDOC);
			if(proposalNode.get("proposalRequest").has("proposerInfo"))
			{
				mobileNumber = proposalNode.get("proposalRequest").get("proposerInfo").get("contactInfo").get("mobile").asText();
				String docId = PolicyRenewConstatnt.USER_POLICY_DETAILS_CONFIG+proposalNode.get("proposalRequest").get("proposerInfo").get("contactInfo").get("mobile").asText();
				JsonNode userPolicyDetail = objectMapper.readTree(policyTransaction.getDocBYId(docId).content().toString());
				ArrayNode policyDetailsList = (ArrayNode) userPolicyDetail.get("policyDetails");
				ObjectNode renewDetailsNode = objectMapper.createObjectNode();
				for(JsonNode policyDetailNode: policyDetailsList)
				{
					if(policyDetailNode.get("proposalId").asText().equalsIgnoreCase(inputReqNode.findValue("proposalId").asText()))
					{
						if(policyDetailNode.has("renewPolicyDetails") && policyDetailNode.get("renewPolicyDetails").has("QUOTE_ID"))
						{
							((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.PROPOSAL_ID,reqNode.get("proposalId").asText());
							((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID,proposalNode.get(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID).asText());
							//Put Renew QUOTE_ID in Renew proposalId
							((ObjectNode)proposalNode).put("QUOTE_ID",policyDetailNode.get("renewPolicyDetails").get("QUOTE_ID").asText());
							JsonObject proposaldocObj = JsonObject.fromJson(proposalNode.toString());
							policyTransaction.replaceDocument(renewproposalId,proposaldocObj );
						}
						else
						{
							renewDetailsNode.put(PolicyRenewConstatnt.PROPOSAL_ID,reqNode.get("proposalId").asText());
							renewDetailsNode.put(PolicyRenewConstatnt.RENEW_RUN,"Y");
							renewDetailsNode.put(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID,proposalNode.get(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID).asText());
							((ObjectNode)policyDetailNode).put("renewPolicyDetails", renewDetailsNode);
						}
						((ObjectNode)responseNode).put("policyDetails",policyDetailNode);
						JsonObject docObj = JsonObject.fromJson(userPolicyDetail.toString());
						policyTransaction.replaceDocument(docId,docObj );
						log.info("Successfully updated ProposalId in userPolicyDetail:"+docId );
						exchange.getIn().setHeader("isPolicyDetailsUpdated", "Y");
						JsonNode inputRequestNode = null;
						inputRequestNode = objectMapper.readTree(exchange.getProperty(PolicyRenewConstatnt.POLICYREMINDERINPUTREQUEST).toString());
						if(inputRequestNode.has("policyExpiryDays"))
						{
							((ObjectNode)responseNode).put(PolicyRenewConstatnt.POLICYEXPIRYDAYS,inputReqNode.get("policyExpiryDays"));
						}
						break;
					}
					else
					{
						log.info("ProposalId not present in userPolicyDetail:"+docId );
					}
				}
			}
			 
		}
		
		else if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null)
		{
			renewproposalId = reqNode.get("proposalId").asText();
			proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			//log.info("Printing proposalDOC :"+proposalDOC);
			proposalNode = objectMapper.readTree(proposalDOC);
			if (proposalNode.get("proposalRequest").has("proposerDetails"))
			{
			mobileNumber = proposalNode.get("proposalRequest").get("proposerDetails").get("mobileNumber").asText();
			String docId = PolicyRenewConstatnt.USER_POLICY_DETAILS_CONFIG+proposalNode.get("proposalRequest").get("proposerDetails").get("mobileNumber").asText();
			JsonNode userPolicyDetail = objectMapper.readTree(policyTransaction.getDocBYId(docId).content().toString());
			ArrayNode policyDetailsList = (ArrayNode) userPolicyDetail.get("policyDetails");
			ObjectNode renewDetailsNode = objectMapper.createObjectNode();
			for(JsonNode policyDetailNode: policyDetailsList)
			{
				if(policyDetailNode.get("proposalId").asText().equalsIgnoreCase(inputReqNode.findValue("proposalId").asText()))
				{
					if(policyDetailNode.has("renewPolicyDetails")&& policyDetailNode.get("renewPolicyDetails").has("QUOTE_ID"))
					{
						((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.PROPOSAL_ID,reqNode.get("proposalId").asText());
						((ObjectNode)policyDetailNode.get("renewPolicyDetails")).put(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID,proposalNode.get(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID).asText());
						//Put Renew QUOTE_ID in Renew proposalId
						((ObjectNode)proposalNode).put("QUOTE_ID",policyDetailNode.get("renewPolicyDetails").get("QUOTE_ID").asText());
						JsonObject proposaldocObj = JsonObject.fromJson(proposalNode.toString());
						policyTransaction.replaceDocument(renewproposalId,proposaldocObj );
					}
					else
					{
						renewDetailsNode.put(PolicyRenewConstatnt.PROPOSAL_ID,reqNode.get("proposalId").asText());
						renewDetailsNode.put(PolicyRenewConstatnt.RENEW_RUN,"Y");
						renewDetailsNode.put(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID,proposalNode.get(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID).asText());
						((ObjectNode)policyDetailNode).put("renewPolicyDetails", renewDetailsNode);
					}
					((ObjectNode) responseNode).put("policyDetails",policyDetailNode);
					JsonObject docObj = JsonObject.fromJson(userPolicyDetail.toString());
					policyTransaction.replaceDocument(docId,docObj );
					log.info("Successfully updated ProposalId in userPolicyDetail:"+docId );
					exchange.getIn().setHeader("isPolicyDetailsUpdated", "Y");
					JsonNode inputRequestNode = null;
					inputRequestNode = objectMapper.readTree(exchange.getProperty(PolicyRenewConstatnt.POLICYREMINDERINPUTREQUEST).toString());
					if(inputRequestNode.has("policyExpiryDays"))
					{
						((ObjectNode)responseNode).put(PolicyRenewConstatnt.POLICYEXPIRYDAYS,inputReqNode.get("policyExpiryDays"));
					}
					break;
				}
				else
				{
					log.info("proposalId not present in userPolicyDetail"+docId);
				}
			}
			}
		}	
		
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(responseNode));
		}
		catch(Exception e)
		{
			log.error("|ERROR|UserPolicyDetailsUpdateProcessor processor failed:",e);
	        throw new ExecutionTerminator();
		}
	}
	
	
	}