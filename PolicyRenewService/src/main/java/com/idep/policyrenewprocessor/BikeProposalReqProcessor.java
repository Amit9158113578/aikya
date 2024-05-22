package com.idep.policyrenewprocessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;

public class BikeProposalReqProcessor implements Processor 
{
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = null;
	CBService policyTransaction = null;
	String proposalDOC = null;
	JsonNode quoteNode = null;
	JsonNode proposalNode = null;
	ObjectNode responseNode = null;
	CBService quoteDataInstance = null;
	ObjectNode premiumDetailsNode = null;
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(BikeProposalReqProcessor.class.getName());
		PolicyRenewDataProcessor dataProvider = new PolicyRenewDataProcessor();
		serverConfig = CBInstanceProvider.getServerConfigInstance();
		policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		quoteDataInstance = CBInstanceProvider.getBucketInstance(PolicyRenewConstatnt.QUOTE_BUCKET);
		String userKey = null;
		String policyEndDate = null;
		String policyStartDate = null;
		try
		{
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = this.objectMapper.readTree(request);
		exchange.setProperty(PolicyRenewConstatnt.INPUT_REQUEST,reqNode);
		log.info("Its BikeProposalReqProcessor ");
		JsonNode encryptionKeyConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewConstatnt.ENCRYPTION_PRIVATE_KEY_CONFIG).content().toString());
		if(encryptionKeyConfig !=null)
		{
			userKey = encryptionKeyConfig.get("encryptionKey").asText();
		}
		
		if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null)
		{
			responseNode = objectMapper.createObjectNode();
			JsonNode policyRenewalConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
			//log.info("Printing policyRenewalConfig :"+policyRenewalConfig);
			proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			proposalNode = objectMapper.readTree(proposalDOC);
			responseNode = dataProvider.prepareResponseNode(proposalNode,policyRenewalConfig.get("bikeResponseConfig"));

				// Update with new information
				((ObjectNode) responseNode).put("requestType","BikeProposalRequest");
				//((ObjectNode) responseNode).put("QUOTE_ID",proposalNode.get("QUOTE_ID").asText());
				if(responseNode.has("insuranceDetails"))
				{
					//update dates
					((ObjectNode) responseNode.get("insuranceDetails")).put("prevPolicyStartDate",responseNode.findValue("policyStartDate"));
					((ObjectNode) responseNode.get("insuranceDetails")).put("prevPolicyEndDate",responseNode.findValue("policyEndDate"));
					((ObjectNode) responseNode.get("insuranceDetails")).put("policyNumber",proposalNode.findValue("bikePolicyResponse").get("policyNo").asText());
					((ObjectNode) responseNode.get("insuranceDetails")).put("ncb",dataProvider.getNewNCB(proposalNode.findValue("ncb").asText()));
					((ObjectNode) responseNode.get("insuranceDetails")).put("insurerName",proposalNode.findValue("insuranceCompany").asText());
					((ObjectNode) responseNode.get("insuranceDetails")).put("insurerId",proposalNode.findValue("bikePolicyResponse").get("carrierId").asInt());
					
					policyStartDate = calculateNextDay(responseNode.findValue("policyEndDate").asText());
					policyEndDate = calculateNextYear(responseNode.findValue("policyEndDate").asText());
					((ObjectNode) responseNode.get("insuranceDetails")).put("policyStartDate",policyStartDate);
					((ObjectNode) responseNode.get("insuranceDetails")).put("policyEndDate",policyEndDate);

				}

				// update nominee details like age..
				if(responseNode.has("nominationDetails") && responseNode.get("nominationDetails").has("personAge"))
				{
					((ObjectNode) responseNode.get("nominationDetails")).put("personAge",responseNode.get("nominationDetails").get("personAge").asInt()+1);
				}

				// update proposer details like age..
				if(responseNode.has("appointeeDetails") && responseNode.findValue("appointeeDetails").has("personAge"))
				{
					((ObjectNode) responseNode.findValue("appointeeDetails")).put("personAge",responseNode.findValue("appointeeDetails").get("personAge").asDouble()+1);
				}

				// update proposer details like age..
				if(responseNode.has("proposerDetails") && responseNode.findValue("proposerDetails").has("personAge"))
				{
					((ObjectNode) responseNode.findValue("proposerDetails")).put("personAge",responseNode.findValue("proposerDetails").get("personAge").asInt()+1);
				}
				
				 long proposal_seq = this.serverConfig.updateDBSequence(PolicyRenewConstatnt.BIKE_PROPOSAL_SEQ);
				 ObjectNode proposalIdNode = objectMapper.createObjectNode();
	    		 String proposalId = DocumentDataConfig.getConfigDocList().get(PolicyRenewConstatnt.DOCID_CONFIG).get(PolicyRenewConstatnt.BIKEPROPOSAL_DOCELE).asText()+proposal_seq;
	    		 proposalIdNode.put(PolicyRenewConstatnt.PROPOSAL_ID,proposalId);
	    		 String encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, userKey);
	    		 proposalIdNode.put(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID,encryptedProposalId);
	    		 ((ObjectNode)responseNode).put(PolicyRenewConstatnt.EXTRA_FIELDS,proposalIdNode);		    		 
		
			
			if(proposalNode.has("carrierId") && proposalNode.has("productId"))
			{
				((ObjectNode)responseNode).put("carrierId", proposalNode.get("carrierId"));
				((ObjectNode)responseNode).put("productId", proposalNode.get("productId"));
			}
			
			if(proposalNode.get("proposalRequest").has("premiumDetails"))
			{
				premiumDetailsNode = objectMapper.createObjectNode();
				premiumDetailsNode.put("grossPremium", proposalNode.get("proposalRequest").get("premiumDetails").get("grossPremium"));
				((ObjectNode)responseNode).put("premiumDetails", premiumDetailsNode);	
			}
						
			log.info("Final Bike response :"+responseNode);
		}
		exchange.getIn().setBody(this.objectMapper.writeValueAsString(responseNode));
		}
		
		catch(Exception e)
		{
			log.error("|ERROR| BikeProposalReqProcessor request processor failed:",e);
	        throw new ExecutionTerminator();
		}
	}
	

	public String calculateNextDay(String startDate)
	{
	 Calendar cal = new GregorianCalendar();
	 	Date date; 
	 	Date nextYear;
	 	String sysPolicyStartDate = null ;
	     try
	     {
			date = dateFormat.parse(startDate);
			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			nextYear = cal.getTime();
			sysPolicyStartDate = dateFormat.format(nextYear);
	     } 
	     catch (ParseException e)
	     {
			e.printStackTrace();
	     }
	     return sysPolicyStartDate;
	}
	
	public String calculateNextYear(String startDate)
	{
	 Calendar cal = new GregorianCalendar();
	 	Date date; 
	 	Date nextYear;
	 	String sysPolicyStartDate = null ;
	     try
	     {
			date = dateFormat.parse(startDate);
			cal.setTime(date);
			cal.add(Calendar.YEAR, 1);
			nextYear = cal.getTime();
			sysPolicyStartDate = dateFormat.format(nextYear);	
	     } 
	     catch (ParseException e)
	     {
			e.printStackTrace();
	     }
	     return sysPolicyStartDate;
	}
}