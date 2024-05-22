package com.idep.OfflinePolicyRenewal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewalReminder.service.PolicyRenewalReminderServiceImpl;

public class OfflineRenewalLeadResProcessor implements Processor { 
	static Logger log = Logger.getLogger(OfflineRenewalLeadResProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	PolicyRenewalReminderServiceImpl renewalService = new PolicyRenewalReminderServiceImpl();
	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		//set infoNode , required to prepare email, sms content
		JsonNode emailReqNode = reqNode.get("infoNode");
		if(reqNode.has("body") && reqNode.get("body") != null){
			if(reqNode.has("infoNode")){
				((ObjectNode) reqNode).remove("infoNode");
			}
			
			else{
			JsonNode leadResponse =null; 
				//JsonNode leadResponse  = renewalService.createRenewalLead(reqNode);
				log.info("offline Renewal Lead Creation Response" +leadResponse);

				if (leadResponse.get("data").has("messageId")){
					((ObjectNode) emailReqNode).put("messageId", leadResponse.get("data").get("messageId"));
				}
			}
		}
		exchange.getIn().setBody(emailReqNode);
	}
}
