package com.idep.policyrenewprocessor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policyrenewprocessor.PolicyRenewDataProcessor;
import com.idep.policyrenewprocessor.PrepareQuoteRequest;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.impl.service.PolicyRenewCalcServiceImpl;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class PrepareQuoteRequest implements Processor{

	PolicyRenewDataProcessor dataProvider = new PolicyRenewDataProcessor();
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	PolicyRenewCalcServiceImpl serviceImpl = new PolicyRenewCalcServiceImpl();
	Logger log = Logger.getLogger(PrepareQuoteRequest.class.getName());
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	static String renewalConfigDOC = ((JsonObject)serverConfig.getDocBYId("PolicyRenewConfigDetails").content()).toString();
	CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	
	public void process(Exchange exchange) throws Exception  
	{
		JsonNode requestNode = null;
		JsonNode responseNode = null;
		JsonNode proposalNode = null;
		requestNode = objectMapper.createObjectNode();
		try
		{
		String request = exchange.getIn().getBody().toString();
		//log.info(" policy renewal reqNode :" + request);
		JsonNode reqNode = objectMapper.readTree(request);
		exchange.setProperty(PolicyRenewConstatnt.RENEW_QUOTE_INPUT_REQUEST,reqNode);
		JsonNode policyRenewalConfigNode = objectMapper.readTree(renewalConfigDOC);
		proposalNode = objectMapper.readTree(((JsonObject)policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content()).toString());

		//log.info("Proposal Node of Policy Renewal : " + proposalNode);

		if ((proposalNode.has("QUOTE_ID")) && (proposalNode.get("QUOTE_ID") != null) && 
				(proposalNode.get("QUOTE_ID").asText() != ""))
		{
			
			if(proposalNode.findValue("businessLineId").asInt()== 2)
				//if (requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).findValue("quoteType").asInt() == 2)
			{
					((ObjectNode)requestNode).put("header", dataProvider.prepareQuoteRequestHeader(proposalNode.get("businessLineId").asText()));
					((ObjectNode)requestNode).put("body", dataProvider.prepareQuoteRequestBody(proposalNode.get("QUOTE_ID").asText()));
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("PreviousPolicyExpiryDate", proposalNode.findValue("insuranceDetails").get("policyEndDate"));
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("PreviousPolicyStartDate", proposalNode.findValue("insuranceDetails").get("policyStartDate"));
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("previousPolicyExpired","N");
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("IDV", 0);
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("idvOption", 1);
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).put("userIdv", 0);
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).put("owneredBy", "Individual");
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).put("ncb", dataProvider.getNewNCB(proposalNode.findValue("ncb").asText()));
				
				if (policyRenewalConfigNode.get("requestPrepare").has("bikeRequestPrepare")) {
					requestNode = filterRequest(requestNode, policyRenewalConfigNode.get("requestPrepare").get("bikeRequestPrepare"));
				}
			}
			//else if ((requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).findValue("quoteType").asInt() == 3) && 
				//	(policyRenewalConfigNode.get("requestPrepare").has("carRequestPrepare"))) {
			else if(proposalNode.findValue("businessLineId").asInt()== 3)
			{
				((ObjectNode)requestNode).put("header", dataProvider.prepareQuoteRequestHeader(proposalNode.get("businessLineId").asText()));
				((ObjectNode)requestNode).put("body", dataProvider.prepareQuoteRequestBody(proposalNode.get("QUOTE_ID").asText()));
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("PreviousPolicyExpiryDate", proposalNode.findValue("insuranceDetails").get("policyEndDate"));
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("PreviousPolicyStartDate", proposalNode.findValue("insuranceDetails").get("policyStartDate"));
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("previousPolicyExpired","N");
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("IDV", 0);
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).put("idvOption", 1);
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).put("userIdv", 0);
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).put("owneredBy", "Individual");
				((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).put("ncb", dataProvider.getNewNCB(proposalNode.findValue("ncb").asText()));
			
				if (policyRenewalConfigNode.get("requestPrepare").has("carRequestPrepare")) {
				requestNode = filterRequest(requestNode, policyRenewalConfigNode.get("requestPrepare").get("carRequestPrepare"));
				}
			}
				else if(proposalNode.findValue("businessLineId").asInt()== 4)
			{
				((ObjectNode)requestNode).put("header", dataProvider.prepareQuoteRequestHeader(proposalNode.get("businessLineId").asText()));
				((ObjectNode)requestNode).put("body", dataProvider.prepareHealthQuoteRequestBody(proposalNode.get("QUOTE_ID").asText()));
			}

			// messageId in quote request
			if(reqNode.has("messageId") && reqNode.get("messageId") != null){
				((ObjectNode) requestNode.get("body")).put("messageId",reqNode.get("messageId").asText());
			}
			// PACoverDetails added by default 
			if( !requestNode.get("body").has("PACoverDetails")){
				JsonNode PACoverDetails = objectMapper.createObjectNode();
				((ObjectNode) PACoverDetails).put("isPACoverApplicable",true);
				((ObjectNode) requestNode.get("body")).put("PACoverDetails",PACoverDetails);
			}
			//log.info("Quote Node Of policy Reminder : " + requestNode);

			responseNode = serviceImpl.getQuote(requestNode);
			if( responseNode != null){
				log.info("policy renewal Quote Response : " + responseNode);
				((ObjectNode)responseNode).putAll((ObjectNode)reqNode);
				((ObjectNode) responseNode).put("isResponseNull",false);
			}else{
				// In policy expired case you may get null response
				log.info("Got quote response null");
				responseNode = objectMapper.createObjectNode();
				((ObjectNode) responseNode).put("isResponseNull",true);
				((ObjectNode)responseNode).putAll((ObjectNode)reqNode);
			}
			//log.info("Quote Node :"+responseNode);
			exchange.getIn().setBody(responseNode);
		}
		
	}
		
		catch(Exception e)
		{
			log.error("|ERROR| PrepareQuoteRequest processor failed:",e);
	        throw new ExecutionTerminator();
		}
	}

		
	private JsonNode filterRequest(JsonNode requestNode, JsonNode requestPrepareConfig)
	{
		if (requestPrepareConfig.has(PolicyRenewConstatnt.VEHICLE_INFO)) {
			for (JsonNode field : requestPrepareConfig.get(PolicyRenewConstatnt.VEHICLE_INFO)) {
				if (requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO).has(field.asText())) {
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.VEHICLE_INFO)).remove(field.asText());
				}
			}
		}
		if (requestPrepareConfig.has(PolicyRenewConstatnt.QUOTE_PARAM)) {
			for (JsonNode field : requestPrepareConfig.get(PolicyRenewConstatnt.QUOTE_PARAM)) {
				if (requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM).has(field.asText())) {
					((ObjectNode)requestNode.findValue(PolicyRenewConstatnt.QUOTE_PARAM)).remove(field.asText());
				}
			}
		}
		log.info("Filter Request Output :" + requestNode);
		return requestNode;
	}
}

