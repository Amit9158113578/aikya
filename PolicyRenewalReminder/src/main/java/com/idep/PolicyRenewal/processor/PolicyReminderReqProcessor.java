package com.idep.PolicyRenewal.processor;

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
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class PolicyReminderReqProcessor
implements Processor
{
	static Logger log = Logger.getLogger(PolicyReminderReqProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
	static SimpleDateFormat simpleDate = new SimpleDateFormat(PolicyRenewalConstatnt.SIMPLE_DATE);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void process(Exchange exchange)
			throws Exception
	{
		CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
		Date date = new Date();
		JsonNode proposalNode = null;
		String renewalConfigDOC = null;
		JsonNode policyRenewalConfigNode = null;
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("Processing Recieved Proposal Id " + reqNode);
		if (reqNode.has("proposalId")){
			renewalConfigDOC = ((JsonObject)serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_CONFIG).content()).toString();
			policyRenewalConfigNode = objectMapper.readTree(renewalConfigDOC);
			log.info("ProposalId : "+reqNode.get("proposalId").asText());
			log.info("policyTransaction :"+policyTransaction);
			String proposalDoc = policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content().toString();
			log.info("proposalDoc :"+proposalDoc);
			proposalNode = objectMapper.readTree(proposalDoc);
			log.info("Proposal Doc :"+proposalNode);
		}
		log.info("PPPPPP :"+proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE));
		if (proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE) != null) {
			if (proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 1)
			{
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("SMSEMAILDetailConfig").get("Life").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
				}
				((ObjectNode)reqNode).put(PolicyRenewalConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE));
				if(reqNode.has("PreviousPolicyExpiryDate")){
					if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
						log.info("Policy already Expired");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
					}
				}
				exchange.getIn().setHeader(PolicyRenewalConstatnt.QUOTE_FLAG, "N");
			}
			else if (proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 2){
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("SMSEMAILDetailConfig").get("Bike").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					try {
						((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
					} catch (NullPointerException e) {
						log.info("Field Not Found :"+(String)fields.getKey());
					}
				}
				((ObjectNode)reqNode).put(PolicyRenewalConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE));
				if(reqNode.has("PreviousPolicyExpiryDate")){
					if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
						log.info("Policy already Expired");
						exchange.getIn().setHeader(PolicyRenewalConstatnt.QUOTE_FLAG, "N");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
						exchange.getIn().setHeader(PolicyRenewalConstatnt.QUOTE_FLAG, "Y");
					}
				}
			}
			else if (proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 3){
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("SMSEMAILDetailConfig").get("Car").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					try {
						((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
					} catch (NullPointerException e) {
						log.info("Field Not Found :"+(String)fields.getKey());
					}
				}
				((ObjectNode)reqNode).put(PolicyRenewalConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE));

				if(reqNode.has("PreviousPolicyExpiryDate")){
					if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
						log.info("Policy already Expired");
						exchange.getIn().setHeader(PolicyRenewalConstatnt.QUOTE_FLAG, "N");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
						exchange.getIn().setHeader(PolicyRenewalConstatnt.QUOTE_FLAG, "Y");
					}
				}
			}
			else if (proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 4)
			{
				Map<String, String> renewalLeadConfigNodeMap = (Map)objectMapper.readValue(policyRenewalConfigNode.get("SMSEMAILDetailConfig").get("Health").toString(), Map.class);
				for (Map.Entry<String, String> fields : renewalLeadConfigNodeMap.entrySet()) {
					try {
						((ObjectNode)reqNode).put((String)fields.getKey(), dataProvider.getValue(proposalNode, (String)fields.getValue()).get("value"));
					} catch (NullPointerException e) 
					{
						log.info("Field Not Found :"+(String)fields.getKey());
					}
				}
				((ObjectNode)reqNode).put(PolicyRenewalConstatnt.BUSINESS_LINE, proposalNode.findValue(PolicyRenewalConstatnt.BUSINESS_LINE));
				if(reqNode.has("PreviousPolicyExpiryDate")){
					if(date.after(simpleDate.parse(reqNode.get("PreviousPolicyExpiryDate").asText()))){
						log.info("Policy already Expired");
						((ObjectNode) reqNode).put("isExpiredCase",true);
					}else{
						log.info("Policy Not yet Expired");
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
				exchange.getIn().setHeader(PolicyRenewalConstatnt.QUOTE_FLAG, "N");
			}
		}


		if( !reqNode.has(PolicyRenewalConstatnt.POLICY_NUMBER)){
			if(reqNode.has(PolicyRenewalConstatnt.MOBILE_NUMBER)){
				String profileDoc = policyTransaction.getDocBYId("PolicyDetails-"+reqNode.get(PolicyRenewalConstatnt.MOBILE_NUMBER).asText()).content().toString();
				JsonNode profileNode = objectMapper.readTree(profileDoc);
				JsonNode policyNode = dataProvider.getPolicyDetails(profileNode, reqNode.get("proposalId").asText(),PolicyRenewalConstatnt.POLICY_NUMBER,PolicyRenewalConstatnt.POLICY_NUMBER);
				log.info("policyNode :"+policyNode);
				if(policyNode != null && policyNode.has(PolicyRenewalConstatnt.POLICY_NUMBER) && policyNode.get(PolicyRenewalConstatnt.POLICY_NUMBER) != null){
					((ObjectNode) reqNode).put(PolicyRenewalConstatnt.POLICY_NUMBER,policyNode.get(PolicyRenewalConstatnt.POLICY_NUMBER));
					((ObjectNode) reqNode).put("PreviousPolicyNumber",policyNode.get(PolicyRenewalConstatnt.POLICY_NUMBER));
					exchange.setProperty(PolicyRenewalConstatnt.POLICY_NUMBER,policyNode.get(PolicyRenewalConstatnt.POLICY_NUMBER).asText());
				}else{
					exchange.setProperty(PolicyRenewalConstatnt.POLICY_NUMBER," ");
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
}
