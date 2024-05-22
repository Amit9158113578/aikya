package com.idep.pospservice.authentication;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.pospservice.util.ExecutionTerminator;

public class ValidateEmailOTP  {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ValidateEmailOTP.class.getName());
	CBService policyTransService = CBInstanceProvider.getPolicyTransInstance();
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	public JsonNode ValidateEmailOtp(JsonNode otpDetails){
		
		try{
			ObjectNode objectNode = objectMapper.createObjectNode();
			log.info("otpDetails in ValidateEmailOTP : "+otpDetails);
			log.info("otpDetails in ValidateEmailOTP EMAILID : "+otpDetails.get("emailId").asText());	
			log.info("otpDetails in ValidateEmailOTP OTP : "+otpDetails.get("emailOtp").asText());	
			String docid = 	otpDetails.get("emailId").asText()+"-"+otpDetails.get("emailOtp").asText();
			JsonDocument emailOtpDoc =  policyTransService.getDocBYId(docid);
			if(emailOtpDoc!=null){

				JsonNode SMSDocNode = this.objectMapper.readTree(emailOtpDoc.content().toString());
				String otpCreated = SMSDocNode.get("createdDateTime").textValue();
				Date date = new Date();
				String currentDate = this.dateFormat.format(date);
					ArrayNode responseMsg = objectMapper.createArrayNode();
				Date otpCreatedDate = this.dateFormat.parse(otpCreated);
				Date currentSysDate = this.dateFormat.parse(currentDate);
				long diff = currentSysDate.getTime() - otpCreatedDate.getTime();
				long diffMinutes = diff / 60000L % 60L;
				long diffHours = diff / 3600000L % 24L;
				long diffDays = diff / 86400000L;
				this.log.debug("otp called time differnce : " + diffDays + ":" + diffHours + ":" + diffMinutes);
				if ((SMSDocNode.get("isActive").textValue().equals("N")) || (diffDays > 0L) || (diffHours > 0L) || (diffMinutes > SMSDocNode.get("expirationTime").longValue())) {
					((ObjectNode)objectNode).put("EmailOtpvalidated", false);
					responseMsg.add("email OTP already Expired");
					((ObjectNode)objectNode).put("responeMsg", responseMsg);
					return objectNode;
				}else{
					JsonNode dataNode = objectMapper.createObjectNode();
					if(dataNode == null){
						throw new ExecutionTerminator();
					}else{
						JsonObject mobileDoc = ((JsonObject)emailOtpDoc.content()).put("isActive", "N");
						((ObjectNode)objectNode).put("EmailOtpvalidated", true);
						((ObjectNode)objectNode).put("responeMsg", responseMsg);
						this.policyTransService.replaceDocument(docid, mobileDoc); 
						return objectNode;
					}
				}
			}else{
				((ObjectNode)objectNode).put("EmailOtpvalidated", false);
				return objectNode;
			}
		}catch(Exception e){
			log.error("Unable to validate EMAIL Otp : ",e);
		}
		return null;
	}
	
}
