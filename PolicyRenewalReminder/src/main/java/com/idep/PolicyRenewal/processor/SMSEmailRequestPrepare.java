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

public class SMSEmailRequestPrepare implements Processor
{
	PolicyRenwalDataProvider dataProvider = new PolicyRenwalDataProvider();
	PolicyRenewalReminderServiceImpl service = new PolicyRenewalReminderServiceImpl();
	ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null;
	static String emailTemplateDOC = null;
	JsonNode emailTemplateNode = null;
	static Logger log = Logger.getLogger(SMSEmailRequestPrepare.class.getName());

	static{
		if (serverConfig == null){
			serverConfig = CBInstanceProvider.getServerConfigInstance();
			emailTemplateDOC = ((JsonObject)serverConfig.getDocBYId("PolicyRenewalTemplate").content()).toString();
		}
	}

	public void process(Exchange exchange) throws Exception	{
		JsonNode smsEmailNode = objectMapper.createObjectNode();
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		String bodyEmail = null;
		String emailSubject = null;
		String smsBody = null;
		JsonNode urlNode = null;
		emailTemplateNode = objectMapper.readTree(emailTemplateDOC);
		log.info("Policy emailTemplateNode :"+emailTemplateNode);
		log.info("Got SMS Email Request : " + reqNode);

		urlNode = dataProvider.prepareURL(reqNode);
		if(reqNode.has("isExpiredCase")){
			bodyEmail = getEmailBody(reqNode, emailTemplateNode.findValue("expiredPolicy"));
			emailSubject = getEmailSubject(reqNode,emailTemplateNode.findValue("expiredPolicy")).replace(PolicyRenewalConstatnt.POLICY_VARIABLE, exchange.getProperty(PolicyRenewalConstatnt.POLICY_NUMBER).toString());
			smsBody = getSMSBody(reqNode, emailTemplateNode.findValue("expiredPolicy"));
		}else{

			bodyEmail = getEmailBody(reqNode, emailTemplateNode.findValue("expiringPolicy"));			
			emailSubject = getEmailSubject(reqNode,emailTemplateNode.findValue("expiringPolicy")).replace(PolicyRenewalConstatnt.POLICY_VARIABLE, exchange.getProperty(PolicyRenewalConstatnt.POLICY_NUMBER).toString());
			smsBody = getSMSBody(reqNode, emailTemplateNode.findValue("expiringPolicy"));
		}
		String emailShortURL = service.getShortURL(urlNode.get("emailURL").asText());
		bodyEmail = bodyEmail.replace("$emailURL",emailShortURL);
		bodyEmail = bodyEmail.replace("$otherURL", emailShortURL);
		((ObjectNode)smsEmailNode).put("mailId", urlNode.get("emailId").asText());
		((ObjectNode)smsEmailNode).put("smsId", urlNode.get("smsId").asText());
		((ObjectNode)smsEmailNode).put("emailBody", bodyEmail);
		((ObjectNode)smsEmailNode).put("emailSubject", emailSubject);
		((ObjectNode)smsEmailNode).put("username", reqNode.get("reminderMailId").asText());
		if ((reqNode.has("reminderMailId")) && (reqNode.get("reminderMailId") == null)) {
			log.info("Email Id Not Present");
		}
		((ObjectNode)smsEmailNode).put("isBCCRequired", "Y");
		if (reqNode.get(PolicyRenewalConstatnt.MOBILE_NUMBER) != null){
			((ObjectNode)smsEmailNode).put("mobileNumber", reqNode.get(PolicyRenewalConstatnt.MOBILE_NUMBER));
			smsBody = smsBody.replace(PolicyRenewalConstatnt.POLICY_VARIABLE, exchange.getProperty(PolicyRenewalConstatnt.POLICY_NUMBER).toString());
			log.info("SMS LOB :"+reqNode.get("businessLineId").asText());
			smsBody = smsBody.replace("$smsURL", service.getShortURL(urlNode.get("smsURL").asText()));
			((ObjectNode)smsEmailNode).put("sms", smsBody);
		}
		log.info("Policy Renewal Reminder Email Content : " + smsEmailNode);
		if(reqNode.has("messageId") && reqNode.get("messageId") != null){
			((ObjectNode) smsEmailNode).put("messageId",reqNode.get("messageId"));
		}
		exchange.getIn().setBody(objectMapper.writeValueAsString(smsEmailNode));
	}

	public String getEmailSubject(JsonNode reqNode, JsonNode emailTemplateNode){
		if ((reqNode.has(PolicyRenewalConstatnt.BUSINESS_LINE)) && (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE) != null)){
			if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 1) {
				return oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.LIFE_EMAIL_TEMPLATE).get(PolicyRenewalConstatnt.EMAIL_SUBJECT).asText());
			}
			if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 2) {
				return oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get(PolicyRenewalConstatnt.EMAIL_SUBJECT).asText());
			}
			if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 3) {
				return oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get(PolicyRenewalConstatnt.EMAIL_SUBJECT).asText());
			}
			if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 4) {
				return oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.HEALTH_EMAIL_TEMPLATE).get(PolicyRenewalConstatnt.EMAIL_SUBJECT).asText());
			}
		}
		return "";
	}

	public String getEmailBody(JsonNode reqNode, JsonNode emailTemplateNode) {
		String emailTemplate = "";
		String emailBodyTemplate = "";
		log.info("reqNode for replace" + reqNode);
		if ((reqNode.has(PolicyRenewalConstatnt.BUSINESS_LINE)) && (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE) != null)) {
			if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 1)
			{
				emailTemplate = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.LIFE_EMAIL_TEMPLATE).get("preEmailBody").asText());
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.LIFE_EMAIL_TEMPLATE).get("tableHeading").asText();
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.LIFE_EMAIL_TEMPLATE).get("emailQuoteTable").asText();
				for (JsonNode jsonNode : reqNode.findValue("lifeQuoteResponse"))
				{
					emailTemplate = emailTemplate + emailBodyTemplate.replace("$carrierName", jsonNode.get("insuranceCompany").asText());
					emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.PRIMIUM_VARIABLE, jsonNode.get("netPremium").asText());
					emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.CARRIER_ID_VARIABLE, jsonNode.findValue("carrierId").asText());
					emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.QUOTE_ID_VARIABLE, reqNode.findValue("QUOTE_ID").asText());
				}
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.LIFE_EMAIL_TEMPLATE).get("tableEnd").asText();
				emailTemplate = emailTemplate + oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.LIFE_EMAIL_TEMPLATE).get("postEmailBody").asText());
			}
			else if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 2)
			{
				emailTemplate = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("preEmailBody").asText());
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("tableHeading").asText();

				if( reqNode.has("isResponseNull") && reqNode.get("isResponseNull").asBoolean() == false ){
					emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("emailQuoteTable").asText();
					for (JsonNode jsonNode : reqNode.findValue("bikeQuoteResponse"))
					{
						emailTemplate = emailTemplate + emailBodyTemplate.replace("$carrierName", jsonNode.get("insuranceCompany").asText());
						emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.PRIMIUM_VARIABLE, jsonNode.get("netPremium").asText());
						emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.QUOTE_ID_VARIABLE, reqNode.findValue("QUOTE_ID").asText());
						emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.CARRIER_ID_VARIABLE, jsonNode.findValue("carrierId").asText());
						emailTemplate = emailTemplate.replace("$carrierName", jsonNode.findValue("insuranceCompany").asText());
						emailTemplate = emailTemplate.replace("$ncbPercentage", jsonNode.findValue("ncbPercentage").asText());
						emailTemplate = emailTemplate.replace("$insuredDeclareValue", String.valueOf(jsonNode.findValue("insuredDeclareValue").asInt()));
						emailTemplate = emailTemplate.replace("$basicCoverage", String.valueOf(jsonNode.findValue("basicCoverage").asInt()));
						emailTemplate = emailTemplate.replace("$totalDiscountAmount", String.valueOf(jsonNode.findValue("totalDiscountAmount").asInt()));
						emailTemplate = emailTemplate.replace("$netPremium", String.valueOf(jsonNode.findValue("netPremium").asInt()));
					}
				}
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("tableEnd").asText();
				emailTemplate = emailTemplate + oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("postEmailBody").asText());
			}
			else if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 3)
			{
				emailTemplate = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("preEmailBody").asText());
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("tableHeading").asText();
				if( reqNode.has("isResponseNull") && reqNode.get("isResponseNull").asBoolean() == false ){
					emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("emailQuoteTable").asText();
					for (JsonNode jsonNode : reqNode.findValue("carQuoteResponse"))
					{
						emailTemplate = emailTemplate + emailBodyTemplate.replace("$carrierName", jsonNode.get("insuranceCompany").asText());
						emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.PRIMIUM_VARIABLE, jsonNode.get("netPremium").asText());
						emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.CARRIER_ID_VARIABLE, jsonNode.findValue("carrierId").asText());
						emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.QUOTE_ID_VARIABLE, reqNode.findValue("QUOTE_ID").asText());
						emailTemplate = emailTemplate.replace("$ncbPercentage", jsonNode.findValue("ncbPercentage").asText());
						emailTemplate = emailTemplate.replace("$insuredDeclareValue", String.valueOf(jsonNode.findValue("insuredDeclareValue").asInt()));
						emailTemplate = emailTemplate.replace("$basicCoverage", String.valueOf(jsonNode.findValue("basicCoverage").asInt()));
						emailTemplate = emailTemplate.replace("$totalDiscountAmount", String.valueOf(jsonNode.findValue("totalDiscountAmount").asInt()));
						emailTemplate = emailTemplate.replace("$netPremium", String.valueOf(jsonNode.findValue("netPremium").asInt()));
						emailTemplate = emailTemplate.replace("$carrierName", jsonNode.findValue("insuranceCompany").asText());
					}
				}
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("tableEnd").asText();
				emailTemplate = emailTemplate + oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.VEHICLE_EMAIL_TEMPLATE).get("postEmailBody").asText());
			}
			else if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 4)
			{
				emailTemplate = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.HEALTH_EMAIL_TEMPLATE).get("preEmailBody").asText());
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.HEALTH_EMAIL_TEMPLATE).get("tableHeading").asText();
				emailBodyTemplate = emailBodyTemplate + emailTemplateNode.get(PolicyRenewalConstatnt.HEALTH_EMAIL_TEMPLATE).get("emailQuoteTable").asText();
				emailTemplate = emailTemplate + oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.HEALTH_EMAIL_TEMPLATE).get("postEmailBody").asText());
				if(reqNode.has("carrierName")){
					emailTemplate = emailTemplate.replace("$insurerName", reqNode.get("carrierName").asText());
				}
			}
		}
		emailTemplate = replaceRegistrationNumber(emailTemplate, reqNode);
		if(reqNode.has(PolicyRenewalConstatnt.POLICY_NUMBER) && reqNode.get(PolicyRenewalConstatnt.POLICY_NUMBER) != null){
			emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.POLICY_VARIABLE, reqNode.get(PolicyRenewalConstatnt.POLICY_NUMBER).asText());
		}
		if(reqNode.has("customerName") && reqNode.get("customerName") != null ){
			emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.CUSTOMER_NAME_VARIABLE, reqNode.get(PolicyRenewalConstatnt.CUSTOMER_NAME).asText());
		}else{
			emailTemplate = emailTemplate.replace(PolicyRenewalConstatnt.CUSTOMER_NAME_VARIABLE, reqNode.get(PolicyRenewalConstatnt.CUSTOMER_DEFAULT).asText());
		}
		log.info("Prepared Email Body :" + emailTemplate);
		return emailTemplate;
	}

	public String oneTimeReplace(JsonNode reqNode, String replaceText)
	{
		for (JsonNode replaceNode : emailTemplateNode.get("replaceVariable")) {
			if ((reqNode.has(replaceNode.get("value").asText())) && (reqNode.findValue(replaceNode.get("value").asText()).asText() != null)) {
				replaceText = replaceText.replace(replaceNode.get("key").asText(), reqNode.findValue(replaceNode.get("value").asText()).asText());
			} else if (reqNode.has(replaceNode.get("value").asText())) {
				replaceText = replaceText.replace(replaceNode.get("key").asText(), String.valueOf(reqNode.findValue(replaceNode.get("value").asText()).intValue()));
			} else {
				log.info("Not found value of :" + replaceNode.get("value").asText());
			}
		}
		log.info("replaceText :" + replaceText);
		return replaceText;
	}

	public String getSMSBody(JsonNode reqNode, JsonNode emailTemplateNode){
		String smsBody = "";
		if ((reqNode.has(PolicyRenewalConstatnt.BUSINESS_LINE)) && (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE) != null)) {
			if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 1) {
				smsBody = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.SMS_TEMPLATE)
						.get("lifeSMSTemplate").asText());
			} else if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 2) {
				smsBody = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.SMS_TEMPLATE)
						.get("vehicleSMSTemplate").asText());
				smsBody = replaceRegistrationNumber(smsBody, reqNode);
			} else if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 3) {
				smsBody = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.SMS_TEMPLATE)
						.get("vehicleSMSTemplate").asText());
				smsBody = replaceRegistrationNumber(smsBody, reqNode);
			} else if (reqNode.get(PolicyRenewalConstatnt.BUSINESS_LINE).asInt() == 4) {
				smsBody = oneTimeReplace(reqNode, emailTemplateNode.get(PolicyRenewalConstatnt.SMS_TEMPLATE)
						.get("healthSMSTemplate").asText());
			}
		}
		log.info("Prepared SMS Body :" + smsBody);
		return smsBody;
	}

	public String replaceRegistrationNumber(String text, JsonNode reqNode){
		if(reqNode.has(PolicyRenewalConstatnt.REG_NUMBER) ){
			log.info("Reg number tt :"+reqNode.get(PolicyRenewalConstatnt.REG_NUMBER).asText());
			text = text.replace(PolicyRenewalConstatnt.REG_NO_VARIABLE, reqNode.get(PolicyRenewalConstatnt.REG_NUMBER).asText());
		}else if(reqNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO) != null ){
			log.info("Reg number tt :"+reqNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO).get(PolicyRenewalConstatnt.REG_NUMBER).asText());
			text = text.replace(PolicyRenewalConstatnt.REG_NO_VARIABLE, reqNode.findValue(PolicyRenewalConstatnt.VEHICLE_INFO).get(PolicyRenewalConstatnt.REG_NUMBER).asText());
		}
		return text;
	}
}
