package com.idep.pospservice.authentication;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.pospservice.util.POSPServiceConstant;
import com.idep.Tokenizer.API.Tokenizer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.pospservice.authentication.UserProfileLoader;
public class OTPValidator implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(OTPValidator.class.getName());
	JsonNode responseConfigNode = null;
	JsonNode blockedMobileNode = null;
	JsonNode defaultOTPNode = null;
	CBService service = null;
	CBService policyTransService = CBInstanceProvider.getPolicyTransInstance();
	CBService pospData = CBInstanceProvider.getBucketInstance("PospData");
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	SimpleDateFormat SysFormat = new SimpleDateFormat("dd-MM-yyyy");
	SimpleDateFormat docDateFormat = new SimpleDateFormat("ddMMyyyyhhmmssS");
	JsonNode errorNode;
	//GetTrainingStatus gts = new GetTrainingStatus();
	UserProfileLoader profileLoader = new UserProfileLoader();
	ValidateEmailOTP emailOtp = new ValidateEmailOTP();
	@Override
	public void process(Exchange exchange) throws Exception {
		try
		{
			JsonNode otpResNode = objectMapper.createObjectNode();
			if (this.service == null) {
				try
				{
					this.service = CBInstanceProvider.getServerConfigInstance();
					this.responseConfigNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("ResponseMessages").content()).toString());
					this.blockedMobileNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("BlockedMobilesList").content()).toString());

					JsonDocument defaultOTPDoc = this.service.getDocBYId("defaultOTPConfig");
					if (defaultOTPDoc != null) {
						this.defaultOTPNode = this.objectMapper.readTree(((JsonObject)defaultOTPDoc.content()).toString());
					}
				}
				catch (Exception e)
				{
					this.log.error("Failed to load configuration documents : ", e);
				}
			}
			JsonNode otpInputNode = objectMapper.readTree(exchange.getIn().getBody().toString());
			this.log.info("otpInputNode value :" + otpInputNode);
			String mobileNumber = otpInputNode.get("mobileNumber").asText();
			
			if(otpInputNode.has("emailId")){
				
				if(otpInputNode.get("emailId").asText().length() > 0){
				
					if(otpInputNode.has("emailOtp")){
						log.info("Validating EmailOtp : "+otpInputNode);
						otpResNode = emailOtp.ValidateEmailOtp(otpInputNode);
						log.info("EMail OTP Validation Respnse : "+otpResNode);
					}	
				}
			}
			
			
			if (this.blockedMobileNode.has(mobileNumber)) {
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.BLOCKED_MOBILES_CODE).asInt());
				objectNode.put(POSPServiceConstant.RES_MSG ,this.blockedMobileNode.get(mobileNumber).textValue());
				objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			}
			String otp = otpInputNode.get("OTP").asText();
			JsonDocument jsondoc = this.policyTransService.getDocBYId("SMS-" + mobileNumber + "-" + otp);
			if (jsondoc == null)
			{
				if ((this.defaultOTPNode.has(otp)) && (this.defaultOTPNode.get(otp).asText().equalsIgnoreCase("Y")))
				{
						((ObjectNode)otpResNode).put("SMSOtpvalidated", true);
						JsonNode profileResult = getUserProfile(mobileNumber);
						if(profileResult.get("profileFound").asBoolean()){
						((ObjectNode)otpResNode).put("isProfileVerified",profileResult.get("isProfileVerified").asText());
						((ObjectNode)otpResNode).put("isTraningCompleted",profileResult.get("isTraningCompleted").asText());
						((ObjectNode)otpResNode).put("comment",profileResult.get("comment").asText());
						((ObjectNode)otpResNode).put("agentId",profileResult.get("agentId").asText());
						((ObjectNode)otpResNode).put("emailId",profileResult.get("emailId").asText());
						((ObjectNode)otpResNode).put("firstName",profileResult.get("firstName").asText());
						((ObjectNode)otpResNode).put("lastName",profileResult.get("lastName").asText());
						((ObjectNode)otpResNode).put("organisationName",profileResult.get("organisationName").asText());
						String Authorization="";
						if(exchange.getIn().getHeader("Authorization")!=null){
							Authorization = exchange.getIn().getHeader("Authorization").toString();
							log.info("Authorization found in headers : "+Authorization);
						}else{
							log.info("Authorization not found in headeres");
						}
							if(Authorization.length()==0){
							/**
							 * if Authorization not found in exchange headers then generating new and storing in DB and sending to UI.
							 * **/
							String token = generateUserToken(mobileNumber,profileResult.get("agentId").asText());
							((ObjectNode)otpResNode).put("Authorization",token);	
						}
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,otpResNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
						
						}else{
							JsonNode errorNode = objectMapper.createObjectNode();
							ObjectNode objectNode = this.objectMapper.createObjectNode();
							objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.ERROR_CODE_VAL);
							objectNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
							((ObjectNode)errorNode).put("error", POSPServiceConstant.PROFILE_ERROR_MSG+" "+mobileNumber);
							objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
							exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
						}
				}else{
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NO_RECORDS_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.NORECORD_MESSAGES).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}

			}else{
				JsonNode SMSDocNode = this.objectMapper.readTree(((JsonObject)jsondoc.content()).toString());
				String otpCreated = SMSDocNode.get("createdDateTime").textValue();
				Date date = new Date();
				String currentDate = this.dateFormat.format(date);

				Date otpCreatedDate = this.dateFormat.parse(otpCreated);
				Date currentSysDate = this.dateFormat.parse(currentDate);
				long diff = currentSysDate.getTime() - otpCreatedDate.getTime();
				long diffMinutes = diff / 60000L % 60L;
				long diffHours = diff / 3600000L % 24L;
				long diffDays = diff / 86400000L;
				this.log.debug("otp called time differnce : " + diffDays + ":" + diffHours + ":" + diffMinutes);
				if ((SMSDocNode.get("isActive").textValue().equals("N")) || (diffDays > 0L) || (diffHours > 0L) || (diffMinutes > SMSDocNode.get("expirationTime").longValue())) {
					ObjectNode objectNode = this.objectMapper.createObjectNode();
					ArrayNode otpResponse = (ArrayNode)otpResNode.get("responeMsg");
					((ObjectNode)otpResNode).put("SMSOtpvalidated", true);
					otpResponse.add("OTP already expired , Try again");
					objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.EXPIRED_OTP_CODE).asInt());
					objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.EXPIRED_OTP_MSG).asText());
					objectNode.put(POSPServiceConstant.RES_DATA,otpResNode);
					exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
				}else{
						JsonObject mobileDoc = ((JsonObject)jsondoc.content()).put("isActive", "N");
						this.policyTransService.replaceDocument("SMS-" + mobileNumber + "-" + otp, mobileDoc);
						((ObjectNode)otpResNode).put("SMSOtpvalidated", true);
						/*
						 * if token not found in request then generate
						 * **/
						
						JsonNode profileResult = getUserProfile(mobileNumber);
						if(profileResult.get("profileFound").asBoolean()){
						((ObjectNode)otpResNode).put("isProfileVerified",profileResult.get("isProfileVerified").asText());
						((ObjectNode)otpResNode).put("isTraningCompleted",profileResult.get("isTraningCompleted").asText());
						((ObjectNode)otpResNode).put("comment",profileResult.get("comment").asText());
						((ObjectNode)otpResNode).put("agentId",profileResult.get("agentId").asText());
						((ObjectNode)otpResNode).put("emailId",profileResult.get("emailId").asText());
						((ObjectNode)otpResNode).put("firstName",profileResult.get("firstName").asText());
						((ObjectNode)otpResNode).put("lastName",profileResult.get("lastName").asText());
						((ObjectNode)otpResNode).put("organisationName",profileResult.get("organisationName").asText());
						String Authorization="";
						if(exchange.getIn().getHeader("Authorization")!=null){
							Authorization = exchange.getIn().getHeader("Authorization").toString();
							log.info("Authorization found in headers : "+Authorization);
						}else{
							log.info("Authorization not found in headeres");
						}
							if(Authorization.length()==0){
							/**
							 * if Authorization not found in exchange headers then generating new and storing in DB and sending to UI.
							 * **/
							String token = generateUserToken(mobileNumber,profileResult.get("agentId").asText());
							((ObjectNode)otpResNode).put("Authorization",token);	
						}
						ObjectNode objectNode = this.objectMapper.createObjectNode();
						objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_CODE).asInt());
						objectNode.put(POSPServiceConstant.RES_MSG ,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.SUCC_CONFIG_MSG).asText());
						objectNode.put(POSPServiceConstant.RES_DATA,otpResNode);
						exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
						
						}else{
							JsonNode errorNode = objectMapper.createObjectNode();
							ObjectNode objectNode = this.objectMapper.createObjectNode();
							objectNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.ERROR_CODE_VAL);
							objectNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
							((ObjectNode)errorNode).put("error", POSPServiceConstant.PROFILE_ERROR_MSG+" "+mobileNumber);
							objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
							exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
						}
				}
			}
		}	
		catch (Exception e)
		{
			this.log.error("unable to validate OTP seems DB is not responding well: ", e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_CODE).asInt());
			objectNode.put(POSPServiceConstant.RES_MSG,DocumentDataConfig.getConfigDocList().get(POSPServiceConstant.RESPONSE_MESSAGES).get(POSPServiceConstant.FAILURE_MESSAGES).asText());
			objectNode.put(POSPServiceConstant.RES_DATA,errorNode);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			
		}

	}
	
	
	public JsonNode getUserProfile(String mobileNumber){
	
		try{
			String docId = POSPServiceConstant.POSP_USER_PROFILE+mobileNumber;
			log.info("getUserProfile() User Document fetching from DB : "+docId);
			JsonDocument userProfileDoc = pospData.getDocBYId(docId);
			/*boolean tranningCompleted =true;
			if(userProfileDoc!=null){
			*//**
			 * verifying Agent Course Details if user profile verified but still training status flag false then it will call 			 * **//*
				JsonNode userData = objectMapper.readTree(userProfileDoc.content().toString());
				if(userData.get("isProfileVerified").asText().equalsIgnoreCase("true") && userData.get("isTraningCompleted").asText().equalsIgnoreCase("false") ){
					ObjectNode request = objectMapper.createObjectNode();
					request.put("mobileNumber", mobileNumber);
					gts.GetMoodleTranningStatus(request.toString());
					tranningCompleted = false;
					log.info("gts.GetMoodleTranningStatus(mobileNumber) called to check Tranning Status");
				}
			}
			
			if(!tranningCompleted){
				userProfileDoc = pospData.getDocBYId(docId);	
			}*/
			
			ObjectNode resProfile = objectMapper.createObjectNode();
			if(userProfileDoc!=null){
				
				
				
				JsonNode userProfile = objectMapper.readTree(userProfileDoc.content().toString());
						
				resProfile.put("profileFound", true);
				
				if(userProfile.has("isProfileVerified")){
				resProfile.put("isProfileVerified", userProfile.get("isProfileVerified").asText());
				}else if(userProfile.has("isVerified")){
					resProfile.put("isProfileVerified", userProfile.get("isVerified").asText());	
				}
				if(userProfile.has("isTraningCompleted")){
					resProfile.put("isTraningCompleted", userProfile.get("isTraningCompleted").asText());
					}
				if(userProfile.has("agentId")){
					resProfile.put("agentId", userProfile.get("agentId").asText());
					}
				if(userProfile.has("comment")){
					resProfile.put("comment", userProfile.get("comment").asText());
					}else{
						resProfile.put("comment", "");	
					}
				if(userProfile.has("email")){
					resProfile.put("emailId", userProfile.get("email").asText());
					}else{
						resProfile.put("emailId", "");	
					}
				if(userProfile.has("firstName")){
					resProfile.put("firstName", userProfile.get("firstName").asText());
					}else{
						resProfile.put("firstName", "");	
					}
				if(userProfile.has("lastName")){
					resProfile.put("lastName", userProfile.get("lastName").asText());
					}else{
						resProfile.put("lastName", "");	
					}
				if(userProfile.has("organisationName")){
					resProfile.put("organisationName", userProfile.get("organisationName").asText());
					}else{
						resProfile.put("organisationName", "");	
					}
			}else{
				log.error("user Profile not found : "+docId);
				resProfile.put("profileFound", false);
			}
			
			return resProfile;
		}catch(Exception e){
			log.error("user Profile not found : ",e);
		}
		
		
		return null;
	}
	
	public String generateUserToken(String mobNumber,String agentId){
		try{
			
					String tokenValue =  mobNumber+"-"+agentId;
					String token = Tokenizer.createToken(tokenValue);
					
					ObjectNode docObj = objectMapper.createObjectNode();
					docObj.put("Authorization", token);
					docObj.put("creationDate", dateFormat.format(new Date()));
					docObj.put("validTill", SysFormat.format(new Date()));
					
					docObj.put("isExpired", "N");
					docObj.put("mobileNumber", mobNumber);
					docObj.put("agentId", agentId);
					docObj.put("documentType", "pospUserToken");
					String docId = "PospToken-"+docDateFormat.format(new Date());
					String doc_status = pospData.createDocument(docId,JsonObject.fromJson(objectMapper.writeValueAsString(docObj)));
						log.info("Session Document created : "+docId+" Doc Genrated : "+doc_status);
			return token;
			
			
		}catch(Exception e){
			log.error("Unable to generate POSP Token : ",e);
		}
		
		return null;
	}
	
}
