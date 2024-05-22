package com.idep.OfflinePolicyRenewal;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PolicyRenewal.processor.PolicyRenwalDataProvider;
import com.idep.PolicyRenewal.util.PolicyRenewalConstatnt;
import com.idep.PolicyRenewalReminder.service.PolicyRenewalReminderServiceImpl;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class OfflineSMSEmailRequest  implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null;
	static String emailTemplateDOC = null;
	static Logger log = Logger.getLogger(OfflineSMSEmailRequest.class.getName());
	PolicyRenewalReminderServiceImpl service = new PolicyRenewalReminderServiceImpl();
	JsonNode emailTemplateNode = null;

	static{
		if(serverConfig == null){
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			emailTemplateDOC = serverConfig.getDocBYId(PolicyRenewalConstatnt.POLICY_RENEWAL_TEMPLATE).content().toString();
		}
	}

	public void process(Exchange exchange) throws Exception {
		JsonNode smsEmailNode = objectMapper.createObjectNode();
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
		String bodyEmail = null;
		JsonNode urlNode = null;
		emailTemplateNode = objectMapper.readTree(emailTemplateDOC);
		String smsTemplate = null;
		log.info("Got SMS Email Request for offline lead : "+reqNode);
		
		urlNode = dataProvider.prepareURL(reqNode);
		log.info("Offline urlNode :"+urlNode);

		if(reqNode.has("emailId") && reqNode.get("emailId") !=null && 
				reqNode.get("emailId").asText() != ""){
			log.info("Offline renewal has email :"+reqNode.get("emailId").asText());
			bodyEmail = getEmailBody(reqNode,emailTemplateNode);
			if(urlNode.has("emailURL") && urlNode.get("emailURL") !=null){
				bodyEmail = bodyEmail.replace("$emailURL",service.getShortURL(urlNode.get("emailURL").asText()));
				((ObjectNode) smsEmailNode).put("mailId",urlNode.get("emailId").asText());
			}
			((ObjectNode) smsEmailNode).put("emailBody",bodyEmail);
			((ObjectNode) smsEmailNode).put("emailSubject",getEmailSubject(reqNode,emailTemplateNode));
			((ObjectNode) smsEmailNode).put("username",reqNode.get("emailId").asText());
			((ObjectNode) smsEmailNode).put("isBCCRequired","N");
			exchange.getIn().setHeader("isEmailPresent", "Y");
		}else{
			exchange.getIn().setHeader("isEmailPresent", "N");
		}
		
		if(reqNode.get("Source").asText().toLowerCase().equalsIgnoreCase("petrolpump") && reqNode.get("contract_type").asText().toLowerCase().contains("bike")){
			smsTemplate = emailTemplateNode.get("SMSTemplates").get("petrolPumpBikeSMSTemplate").asText();;
			exchange.getIn().setHeader("isEmailPresent", "N");
			
		}else{
			smsTemplate = emailTemplateNode.get("SMSTemplates").get("offlineSMSTemplate").asText();;
		}

		if(reqNode.get("mobileNumber") != null ){
			String smsBody = getSMSBody(reqNode,smsTemplate);
			if(urlNode.has("smsURL") && urlNode.get("smsURL") != null){
				smsBody = smsBody.replace("$smsURL", service.getShortURL(urlNode.get("smsURL").asText()));
				((ObjectNode)smsEmailNode).put("smsId", urlNode.get("smsId").asText());
			}
			((ObjectNode) smsEmailNode).put("mobileNumber",reqNode.get("mobileNumber"));
			((ObjectNode) smsEmailNode).put("sms",smsBody);
		}
		log.info("Policy Renewal Reminder SMS Email Content offline .... : "+smsEmailNode);
		if(reqNode.has("messageId") && reqNode.get("messageId") !=null){
			((ObjectNode) smsEmailNode).put("messageId",reqNode.get("messageId"));
		}
		exchange.getIn().setBody(objectMapper.writeValueAsString(smsEmailNode));
	}

	public String getEmailSubject(JsonNode reqNode, JsonNode emailTemplateNode){
		String emailTemplate = "";
		try{
			emailTemplate = emailTemplateNode.get("emailTemplate").get("OfflineLeadmailTemplate").get("emailSubject").asText();
			emailTemplate = emailTemplate.replace("$contract_type", reqNode.get("contract_type").asText());
			emailTemplate = emailTemplate.replace("$end_date1_c", reqNode.get("end_date1_c").asText());
		}catch(Exception e){
			log.error("error at preparing email subject",e);
		}
		return emailTemplate;
	}

	public String getEmailBody(JsonNode reqNode, JsonNode emailTemplateNode){
		String emailBodyTemplate = "";
		try{
			emailBodyTemplate = emailTemplateNode.get("emailTemplate").get("OfflineLeadmailTemplate").get("preEmailBody").asText();
			emailBodyTemplate = emailBodyTemplate.replace("$contract_type", reqNode.get("contract_type").asText());
			emailBodyTemplate = emailBodyTemplate.replace("$end_date1_c", reqNode.get("end_date1_c").asText());
			emailBodyTemplate = emailBodyTemplate.replace("$policynumber_c", reqNode.get("policynumber_c").asText());
		}catch(Exception e){
			log.error("error at preparing email body",e);
		}
		log.info("Email body for sending mail ...."+emailBodyTemplate);
		return emailBodyTemplate;
	}

	public String getSMSBody(JsonNode reqNode,String smsText){
		log.info("reqNode ::: "+reqNode);
		String smsBody = "";
		try{
			smsBody = smsText;
			smsBody = smsBody.replace("$contract_type", reqNode.get("contract_type").asText());
			smsBody = smsBody.replace("$policynumber_c", reqNode.get("policynumber_c").asText());
			smsBody = smsBody.replace("$end_date1_c", reqNode.get("end_date1_c").asText());
			smsBody = smsBody.replace("$customerName", reqNode.get("name").asText());
			smsBody = smsBody.replace("$registrationNumber", reqNode.get("vehicleRegNo").asText());
			smsBody = smsBody.replace("$petrolPumpName", reqNode.get("petrolPumpName").asText());
			
			log.info("Prepared SMS Body :"+smsBody);
		}catch(Exception e){
			log.error("error at preparing sms body",e);
		}
		log.info("prepared sms body :"+smsBody);
		return smsBody;
	}

}