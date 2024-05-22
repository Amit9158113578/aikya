package com.idep.PolicyRenewal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewalReminder.service.PolicyRenewalReminderServiceImpl;

public class RenewalLeadResProcessor
implements Processor
{
	static Logger log = Logger.getLogger(RenewalLeadResProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static PolicyRenewalReminderServiceImpl renewalService = new PolicyRenewalReminderServiceImpl();

	public void process(Exchange exchange) throws Exception	{

		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		JsonNode emailRequest = objectMapper.createObjectNode();
		log.info("RenewalLeadResProcessor Req :" + reqNode);
		if(!reqNode.has("body") && reqNode.has("messageId")){
			((ObjectNode)emailRequest).put("messageId", reqNode.get("messageId"));
		}else{
			JsonNode leadResponse = renewalService.createRenewalLead(reqNode);
			log.info("Renewal Lead Creation Response" + leadResponse);
			if (leadResponse.get("data").has("messageId")){
				((ObjectNode)emailRequest).put("messageId", leadResponse.get("data").get("messageId"));
			}
		}
		((ObjectNode)emailRequest).put("proposalId", reqNode.findValue("renewalProposalId").asText());
		log.info("emailRequest" + emailRequest);
		exchange.getIn().setBody(emailRequest);
	}
}
