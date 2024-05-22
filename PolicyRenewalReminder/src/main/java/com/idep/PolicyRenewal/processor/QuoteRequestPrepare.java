package com.idep.PolicyRenewal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.PolicyRenewalReminder.service.PolicyRenewalReminderServiceImpl;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class QuoteRequestPrepare  implements Processor{

	PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	PolicyRenewalReminderServiceImpl serviceImpl = new PolicyRenewalReminderServiceImpl();
	Logger log = Logger.getLogger(QuoteRequestPrepare.class.getName());
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	static String renewalConfigDOC = ((JsonObject)serverConfig.getDocBYId("PolicyRenewalConfiguration").content()).toString();

	public void process(Exchange exchange) throws Exception  {

		CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
		JsonNode requestNode = null;
		JsonNode responseNode = null;
		JsonNode proposalNode = null;
		requestNode = objectMapper.createObjectNode();
		String request = exchange.getIn().getBody().toString();
		log.info(" policy renewal reqNode :" + request);
		JsonNode reqNode = objectMapper.readTree(request);
		JsonNode policyRenewalConfigNode = objectMapper.readTree(renewalConfigDOC);
		proposalNode = objectMapper.readTree(((JsonObject)policyTransaction.getDocBYId(reqNode.get("proposalId").asText()).content()).toString());

		log.info("Proposal Node of Policy Renewal : " + proposalNode);

		if ((proposalNode.has("QUOTE_ID")) && (proposalNode.get("QUOTE_ID") != null) && 
				(proposalNode.get("QUOTE_ID").asText() != ""))
		{
			((ObjectNode)requestNode).put("header", dataProvider.prepareQuoteRequestHeader(proposalNode.get("businessLineId").asText()));
			((ObjectNode)requestNode).put("body", dataProvider.prepareQuoteRequestBody(proposalNode.get("QUOTE_ID").asText()));
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO)).put("PreviousPolicyExpiryDate", proposalNode.findValue("insuranceDetails").get("policyEndDate"));
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO)).put("PreviousPolicyStartDate", proposalNode.findValue("insuranceDetails").get("policyStartDate"));
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO)).put("previousPolicyExpired","N");
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO)).put("IDV", 0);
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO)).put("idvOption", 1);
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM)).put("userIdv", 0);
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM)).put("owneredBy", "Individual");
			((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM)).put("ncb", dataProvider.getNewNCB(proposalNode.findValue("ncb").asText()));
			if (requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM).findValue("quoteType").asInt() == 2)
			{
				if (policyRenewalConfigNode.get("requestPrepare").has("bikeRequestPrepare")) {
					requestNode = filterRequest(requestNode, policyRenewalConfigNode.get("requestPrepare").get("bikeRequestPrepare"));
				}
			}
			else if ((requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM).findValue("quoteType").asInt() == 3) && 
					(policyRenewalConfigNode.get("requestPrepare").has("carRequestPrepare"))) {
				requestNode = filterRequest(requestNode, policyRenewalConfigNode.get("requestPrepare").get("carRequestPrepare"));
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
			log.info("Quote Node Of policy Reminder : " + requestNode);

			responseNode = serviceImpl.getQuote(requestNode);
			if( responseNode != null){
				log.info("policy renewal Quote Response : " + responseNode);
				((ObjectNode)responseNode).putAll((ObjectNode)reqNode);
				((ObjectNode) responseNode).put("isResponseNull",false);

				// Stopped updating QUOTE_ID in iCRM because in iQuote UI fetching by msgid

				/*
				String leadId = null;
				try {
					if(reqNode.has("leadMsgId") && reqNode.get("leadMsgId") != null){
						log.info("leadMsgId  :"+reqNode.get("leadMsgId"));
						leadId = crmService.findLead("leads_cstm.messageid_c = '"+reqNode.get("leadMsgId").asText()+"'", " ");
						log.info("Lead found :"+leadId);
					}
					// update lead 
					if(leadId != null || leadId != "" ){
						ObjectNode updateNode = objectMapper.createObjectNode();
						if(responseNode.has("businessLineId") && responseNode.has("QUOTE_ID") && responseNode.get("businessLineId").asInt() == 2){
							updateNode.put("quote_id_bike_c", responseNode.get("QUOTE_ID").asText());
						}else{
							updateNode.put("quoteid_c", responseNode.get("QUOTE_ID").asText());
						}
						updateNode.put("lastvisitedquote_c", responseNode.get("QUOTE_ID").asText());
						leadId = crmService.updateLead(updateNode, leadId);
						log.info("Quote updated in crm :"+leadId);
					}
				} catch (Exception e) {
					log.info("Error while updating quote id in lead",e);
				}
				 */
			}else{
				// In policy expired case you may get null response
				log.info("Got quote response null");
				responseNode = objectMapper.createObjectNode();
				((ObjectNode) responseNode).put("isResponseNull",true);
				((ObjectNode)responseNode).putAll((ObjectNode)reqNode);
			}
			log.info("Quote Node :"+responseNode);
			exchange.getIn().setBody(responseNode);
		}
	}

	private JsonNode filterRequest(JsonNode requestNode, JsonNode requestPrepareConfig)
	{
		if (requestPrepareConfig.has(PolicyRenewalConstatnt.VEHICLE_INFO)) {
			for (JsonNode field : requestPrepareConfig.get(PolicyRenewalConstatnt.VEHICLE_INFO)) {
				if (requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO).has(field.asText())) {
					((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO)).remove(field.asText());
				}
			}
		}
		if (requestPrepareConfig.has(PolicyRenewalConstatnt.QUOTE_PARAM)) {
			for (JsonNode field : requestPrepareConfig.get(PolicyRenewalConstatnt.QUOTE_PARAM)) {
				if (requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM).has(field.asText())) {
					((ObjectNode)requestNode.findValue(PolicyRenewalConstatnt.QUOTE_PARAM)).remove(field.asText());
				}
			}
		}
		log.info("Filter Request Output :" + requestNode);
		return requestNode;
	}
}
