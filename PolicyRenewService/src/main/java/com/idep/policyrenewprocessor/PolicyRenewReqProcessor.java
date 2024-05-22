package com.idep.policyrenewprocessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.idep.PolicyRenewal.processor.PolicyReminderReqProcessor;
import com.idep.policyrenewprocessor.PolicyRenewDataProcessor;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyRenewReqProcessor implements Processor
{
	static Logger log = Logger.getLogger(PolicyRenewReqProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static PolicyRenewDataProcessor dataProvider = new PolicyRenewDataProcessor();
	static SimpleDateFormat simpleDate = new SimpleDateFormat(PolicyRenewConstatnt.SIMPLE_DATE);
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void process(Exchange exchange) throws Exception
	{
		CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
		Date date = new Date();
		
		Date strCurrentDate = sdf.parse(sdf.format(new Date()));
		Date policyExpiryDate = new Date();
		JsonNode proposalNode = null;
		String renewalConfigDOC = null;
		JsonNode policyRenewalConfigNode = null;
		
		try
		{
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);

		exchange.setProperty(PolicyRenewConstatnt.POLICYREMINDERINPUTREQUEST, reqNode);
		//log.info("Processing Recieved Proposal Id " + reqNode);
		if (reqNode.has("proposalId")){
			renewalConfigDOC = ((JsonObject)serverConfig.getDocBYId(PolicyRenewConstatnt.POLICY_RENEWAL_CONFIG).content()).toString();
			policyRenewalConfigNode = objectMapper.readTree(renewalConfigDOC);
			String proposalDoc = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			proposalNode = objectMapper.readTree(proposalDoc);
		}
		//log.info("PPPPPP :"+proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE));
		if (proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE) != null) {
			if (proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE).asInt() == 1)
			{
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("ProposalDetailConfig").get("Life").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
				}
				((ObjectNode)reqNode).put(PolicyRenewConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE));
				if(reqNode.has("PreviousPolicyExpiryDate")){
					
					policyExpiryDate = sdf.parse(reqNode.get("PreviousPolicyExpiryDate").asText());
					int dateResult = policyExpiryDate.compareTo(strCurrentDate);
					log.info("Date compare result = "+dateResult);
					
					//if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
					if(dateResult < 0)
					{	log.info("Policy already Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "N");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "Y");
							}
				}
			}
			else if (proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE).asInt() == 2){
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("ProposalDetailConfig").get("Bike").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					try {
						((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
					} catch (NullPointerException e) {
						log.info("Field Not Found :"+(String)fields.getKey());
					}
				}
				((ObjectNode)reqNode).put(PolicyRenewConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE));
				if(reqNode.has("PreviousPolicyExpiryDate"))
				{					
					policyExpiryDate = sdf.parse(reqNode.get("PreviousPolicyExpiryDate").asText());
					int dateResult = policyExpiryDate.compareTo(strCurrentDate);
					log.info("Date compare result = "+dateResult);
					
					//if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
					if(dateResult < 0)
					{
						log.info("Policy already Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "N");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "Y");
						}
				}
			}
			else if (proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE).asInt() == 3){
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("ProposalDetailConfig").get("Car").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					try {
						((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
					} catch (NullPointerException e) {
						log.info("Field Not Found :"+(String)fields.getKey());
					}
				}
				((ObjectNode)reqNode).put(PolicyRenewConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE));

				if(reqNode.has("PreviousPolicyExpiryDate")){
					policyExpiryDate = sdf.parse(reqNode.get("PreviousPolicyExpiryDate").asText());
					int dateResult = policyExpiryDate.compareTo(strCurrentDate);
					log.info("Date compare result = "+dateResult);
					
					//if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
					if(dateResult < 0)
					{	log.info("Policy already Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "N");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "Y");
					}
				}
			}
			else if (proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE).asInt() == 4)
			{
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("ProposalDetailConfig").get("Health").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					try {
						((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
					} catch (NullPointerException e) 
					{
						log.info("Field Not Found :"+(String)fields.getKey());
					}
				}
				((ObjectNode)reqNode).put(PolicyRenewConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewConstatnt.BUSINESS_LINE));
				if(reqNode.has("PreviousPolicyExpiryDate")){
					policyExpiryDate = sdf.parse(reqNode.get("PreviousPolicyExpiryDate").asText());
					int dateResult = policyExpiryDate.compareTo(strCurrentDate);
					log.info("Date compare result = "+dateResult);
					
					//if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
					if(dateResult < 0)
					{	log.info("Policy already Expired");
						((ObjectNode) reqNode).put("isExpiredCase",true);
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "N");
					}else{
						log.info("Policy Not yet Expired");
						exchange.getIn().setHeader(PolicyRenewConstatnt.QUOTE_FLAG, "Y");
					}
				}
				if(proposalNode.findValue("insuredMembers") != null){
					int members = 0;
					for(JsonNode member :proposalNode.findValue("insuredMembers")){
						members += 1;
					}
					log.info("Members :"+members);
					if(members > 1){
						((ObjectNode) reqNode).put("policyFor","Family");
					}
					if(members == 1){
						((ObjectNode) reqNode).put("policyFor","Individual");
					}
				}
			}
		}

		if( !reqNode.has(PolicyRenewConstatnt.POLICY_NUMBER)){
			if(reqNode.has(PolicyRenewConstatnt.MOBILE_NUMBER)){
				String profileDoc = policyTransaction.getDocBYId("PolicyDetails-"+reqNode.get(PolicyRenewConstatnt.MOBILE_NUMBER).asText()).content().toString();
				JsonNode profileNode = objectMapper.readTree(profileDoc);
				JsonNode policyNode = dataProvider.getPolicyDetails(profileNode, reqNode.get("proposalId").asText(),PolicyRenewConstatnt.POLICY_NUMBER,PolicyRenewConstatnt.POLICY_NUMBER);
				if(policyNode != null && policyNode.has(PolicyRenewConstatnt.POLICY_NUMBER) && policyNode.get(PolicyRenewConstatnt.POLICY_NUMBER) != null){
					((ObjectNode) reqNode).put(PolicyRenewConstatnt.POLICY_NUMBER,policyNode.get(PolicyRenewConstatnt.POLICY_NUMBER));
					((ObjectNode) reqNode).put("PreviousPolicyNumber",policyNode.get(PolicyRenewConstatnt.POLICY_NUMBER));
					exchange.setProperty(PolicyRenewConstatnt.POLICY_NUMBER,policyNode.get(PolicyRenewConstatnt.POLICY_NUMBER).asText());
				}else{
					exchange.setProperty(PolicyRenewConstatnt.POLICY_NUMBER," ");
				}
			}
		}

		if(reqNode.has("carrierId") && reqNode.get("carrierId") !=null){
			log.info("Carrier Id  :"+reqNode.get("carrierId"));
			String carrierDoc = serverConfig.getDocBYId("Carrier-"+reqNode.get("carrierId").asInt()).content().toString();
			JsonNode carrierNode = objectMapper.readTree(carrierDoc);
			if(carrierNode != null && carrierNode.has("carrierName")){
				((ObjectNode) reqNode).put("carrierName",carrierNode.get("carrierName").asText());
			}else{
				((ObjectNode) reqNode).put("carrierName"," ");
			}
		}
		log.info("Policy Renewal Reminder Required Field :" + reqNode);

		exchange.getIn().setBody(reqNode);
		}
		
		catch(Exception e)
		{
			log.error("|ERROR| PolicyRenewReqProcessor request processor failed:",e);
	        throw new ExecutionTerminator();
		}
	}
		
}

