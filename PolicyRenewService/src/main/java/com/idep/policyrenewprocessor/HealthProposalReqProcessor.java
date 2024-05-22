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
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;

public class HealthProposalReqProcessor implements Processor 
{
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = null;
	CBService policyTransaction = null;
	String proposalDOC = null;
	JsonNode quoteNode = null;
	JsonNode proposalNode = null;
	ObjectNode responseNode = null;
	CBService quoteDataInstance = null;
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	public void process(Exchange exchange) throws Exception 
	{
		Logger log = Logger.getLogger(HealthProposalReqProcessor.class.getName());
		PolicyRenewDataProcessor dataProvider = new PolicyRenewDataProcessor();
		serverConfig = CBInstanceProvider.getServerConfigInstance();
		policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		quoteDataInstance = CBInstanceProvider.getBucketInstance(PolicyRenewConstatnt.QUOTE_BUCKET);
		String request = exchange.getIn().getBody().toString();
		String policyEndDate = null;
		String policyStartDate = null;
		JsonNode reqNode = this.objectMapper.readTree(request);
		exchange.setProperty(PolicyRenewConstatnt.INPUT_REQUEST,reqNode);
		log.info("Its HealthProposalReqProcessor");
		String userKey = null;
		try
		{
		JsonNode encryptionKeyConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewConstatnt.ENCRYPTION_PRIVATE_KEY_CONFIG).content().toString());
		if(encryptionKeyConfig !=null)
		{
			userKey = encryptionKeyConfig.get("encryptionKey").asText();
		}
		if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null)
		{
			responseNode = objectMapper.createObjectNode();
			JsonNode policyRenewalConfig = objectMapper.readTree(serverConfig.getDocBYId(PolicyRenewConstatnt.POLICY_RENEWAL_CONFIG).content().toString());
			proposalDOC = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			//log.info("Printing proposalDOC :"+proposalDOC);
			proposalNode = objectMapper.readTree(proposalDOC);
			if(proposalNode.has("QUOTE_ID"))
			{
				responseNode = dataProvider.prepareResponseNode(proposalNode,policyRenewalConfig.get("healthResponseConfig"));

				// Update with new information
				((ObjectNode) responseNode).put("requestType","HealthProposalRequest");
				((ObjectNode) responseNode).put("QUOTE_ID",proposalNode.get("QUOTE_ID").asText());
				if(responseNode.get("proposalRequest").has("coverageDetails")){
					//update dates
					log.info("update dates: ");
					((ObjectNode) responseNode.get("proposalRequest").get("coverageDetails")).put("prevPolicyStartDate",responseNode.findValue("policyStartDate"));
					((ObjectNode) responseNode.get("proposalRequest").get("coverageDetails")).put("prevPolicyEndDate",responseNode.findValue("policyEndDate"));
					((ObjectNode) responseNode.get("proposalRequest").get("coverageDetails")).put("policyNumber",proposalNode.findValue("healthPolicyResponse").get("policyNo").asText());
					((ObjectNode) responseNode.get("proposalRequest").get("coverageDetails")).put("insurerId",proposalNode.findValue("healthPolicyResponse").get("carrierId").asInt());
					
					policyStartDate = calculateNextDay(responseNode.findValue("policyEndDate").asText());
					policyEndDate = calculateNextYear(responseNode.findValue("policyEndDate").asText());
					((ObjectNode) responseNode.get("proposalRequest").get("coverageDetails")).put("policyStartDate",policyStartDate);
					((ObjectNode) responseNode.get("proposalRequest").get("coverageDetails")).put("policyEndDate",policyEndDate);
					
				}
				// update proposer details 
				if(responseNode.has("appointeeDetails") && responseNode.findValue("appointeeDetails").has("age")){
					((ObjectNode) responseNode.findValue("appointeeDetails")).put("age",responseNode.findValue("appointeeDetails").get("age").asDouble()+1);
				}			
				 long proposal_seq = this.serverConfig.updateDBSequence(PolicyRenewConstatnt.HEALTH_PROPOSAL_SEQ);
				 String proposalId = DocumentDataConfig.getConfigDocList().get(PolicyRenewConstatnt.DOCID_CONFIG).get(PolicyRenewConstatnt.HEALTHPROPOSAL_DOCELE).asText()+proposal_seq;
	    		 ((ObjectNode)responseNode).put(PolicyRenewConstatnt.PROPOSAL_ID,proposalId);
	    		 String encryptedProposalId = GenrateEncryptionKey.GetEncryptedKey(proposalId, userKey);
	    		 ((ObjectNode)responseNode).put(PolicyRenewConstatnt.ENCRYPTED_PROPOSALID,encryptedProposalId);
	    		 
			}
			
			if(proposalNode.has("carrierId") && proposalNode.has("productId")&& proposalNode.has("childPlanId"))
			{
				((ObjectNode)responseNode).put("carrierId", proposalNode.get("carrierId"));
				((ObjectNode)responseNode).put("productId", proposalNode.get("productId"));
				((ObjectNode)responseNode).put("planId", proposalNode.get("productId"));
				((ObjectNode)responseNode).put("childPlanId", proposalNode.get("childPlanId"));
			}
			
			if(proposalNode.has("insuranceCompany"))
			{
			((ObjectNode) responseNode).put("insuranceCompany",proposalNode.findValue("insuranceCompany").asText());
			}
		
			log.info("Final Health response :"+responseNode);
		}
		exchange.getIn().setBody(this.objectMapper.writeValueAsString(responseNode));
		}
		
		catch(Exception e)
		{
			log.error("|ERROR| HealthProposalReqProcessor request processor failed:",e);
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
	
	public String getDate(String dateOfBirth)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date,newdate = null;
		try {
			date = dateFormat.parse(dateOfBirth);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.YEAR, 1);
			newdate = cal.getTime();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateFormat.format(newdate);
	}
}