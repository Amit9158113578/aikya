package com.idep.rest.impl.smsEmailOtp;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.rest.impl.service.SMSEmailImplService;

public class GenerateSMSEmailOTP implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	 Logger log = Logger.getLogger(GenerateSMSEmailOTP.class.getName());
	  CBService service = CBInstanceProvider.getServerConfigInstance();
	  CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	  static CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	  SimpleDateFormat simpleDate =new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
	  SMSEmailImplService smsImpl = new SMSEmailImplService();
	  static JsonDocument smsEmailConfigDoc;
	  static {
		  smsEmailConfigDoc = pospData.getDocBYId("SmsEmailConfigDoc");
	  }
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			 Integer OTP = 0;
			 JsonNode ResNode = objectMapper.createObjectNode() ;
			JsonNode smsEmailConfigNode=null;
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
			if(smsEmailConfigDoc==null){
				smsEmailConfigDoc = pospData.getDocBYId("SmsEmailConfigDoc");
			}
			smsEmailConfigNode = objectMapper.readTree(smsEmailConfigDoc.content().toString());
			if(request.has("mobileNumber")){
				exchange.getIn().setHeader("SMS", "true");
				JsonNode smsReq = smsEmailConfigNode.get("SmsRequest");
				((ObjectNode)smsReq).put("mobileNumber", request.get("mobileNumber").asText());
				exchange.setProperty("smsRequest", smsReq);
				((ObjectNode)ResNode).put("smsRequest", smsReq);
			}else{
				exchange.getIn().setHeader("SMS", "false");
			}
			
			if(request.has("emailId")){
				 OTP = smsImpl.generateOTP();
				 exchange.getIn().setHeader("EMAIL", "true");
				JsonNode emailReq = smsEmailConfigNode.get("emailOtpRequest");
				((ObjectNode)emailReq).put("username", request.get("emailId").asText());
				((ObjectNode)emailReq).put("otp", new Integer(OTP).toString());
				log.info("email OTP Genrated : "+OTP+" email : "+request.get("emailId").asText());
				((ObjectNode)emailReq.get("paramMap")).put("otp", new Integer(OTP).toString());
				Date sysDate = new Date();
				((ObjectNode)emailReq).put("generatedDate", simpleDate.format(sysDate));
				((ObjectNode)ResNode).put("emailRequest", emailReq);
				exchange.setProperty("emailRequest", emailReq);
			}else{
				exchange.getIn().setHeader("EMAIL", "false");
			}
			log.info("Generated Res Node For SMS & EMAIL OTP : "+ResNode);
			exchange.getIn().setBody(ResNode);
			
			
		}catch(Exception e){
			log.error("Unable to send POSP otp on sms & email : ",e);
		}

	}

}
