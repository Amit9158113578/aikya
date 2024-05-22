package com.idep.rest.impl.service;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.smsemail.exception.ExecutionTerminator;
import com.idep.smsemail.util.SMSConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.jms.Session;

import org.apache.log4j.Logger;

public class SMSEmailImplService
{
  ObjectMapper objectMapper = new ObjectMapper();
  Random random = new Random();
  Logger log = Logger.getLogger(SMSEmailImplService.class.getName());
  CBService service = CBInstanceProvider.getServerConfigInstance();
  CBService sessionKey = CBInstanceProvider.getBucketInstance(SMSConstants.SESSIONKEYBUCKET);
  CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
  SimpleDateFormat simpleDate =new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
  SimpleDateFormat updatedDateFormat =new SimpleDateFormat("yyyy-MM-dd");
  JsonNode supportEmailNode = null;
  JsonNode blockedMobileNode = null;
	JsonDocument serviceConfigNode =null;
  public Integer generateOTP()
  {
    return Integer.valueOf(100000 + this.random.nextInt(900000));
  }
  
@SuppressWarnings("unchecked")
public String sendSMSRequest(String msgdetails) throws JsonProcessingException {
	ValidateSessionId validate = new ValidateSessionId();
	    this.log.debug("SMS method input : " + msgdetails);
	    ObjectNode SMSDataNode = this.objectMapper.createObjectNode();
	    Integer OTP = 0;
	    try
	    {
		    if (this.blockedMobileNode == null)
		    {
		    	try
		    	{
		    		this.blockedMobileNode = this.objectMapper.readTree(this.service.getDocBYId(SMSConstants.BLOCKED_MOBILES).content().toString());
		    		serviceConfigNode = service.getDocBYId("SMSConfig");
		    	}
		    	catch(Exception e)
		    	{
		    		log.error("unable to fetch blocked mobile configuration document : "+SMSConstants.BLOCKED_MOBILES);
		    	}
		    }
		    
	      JsonNode node = this.objectMapper.readTree(msgdetails);
	      // check if mobile no is been blocked in our system
	      if(blockedMobileNode.has(node.get("mobileNumber").textValue()))
	      {
	    	  SMSDataNode.put("mobileNumber", "BLOCKED");
		      SMSDataNode.put("sms", blockedMobileNode.get(node.get("mobileNumber").textValue()).textValue());
		      return this.objectMapper.writeValueAsString(SMSDataNode);
	      }
	      else
	      {
	      String doc = this.service.getDocBYId("SMS-" + node.get("funcType").textValue()).content().toString();
	      Map<String, String> valueMap = objectMapper.readValue(doc, Map.class);
	      String SMSTemplate = valueMap.get("message");
	      String country = valueMap.get("country");
	      if(country.isEmpty())
	      {
	    	   country="91";
	    	   log.info("set country if it is not get the document :"+country);
	      }
	      this.log.debug("SMS Template retrieved from configuration DB : " + SMSTemplate);
	     
	      if(node.has("paramMap"))
	      {
		        Map<String, String> paramMap = objectMapper.readValue(node.get("paramMap").toString(), Map.class);
		        if (paramMap.containsKey("OTP"))
		        {
		        	/*
		        	 * Verifying user has tried for multiple OTP if user already reached to MAX otp limit then , User blocked for 60 mints 
		        	 * */
		        /*if(!validate.verifyUserBlocked(node)){
		        		SMSDataNode.put("mobileNumber", "BLOCKED");
		  		      SMSDataNode.put("sms", serviceConfigNode.content().getString("errorMsg").toString());
		  		      return this.objectMapper.writeValueAsString(SMSDataNode);
		        }*/
		        
		        	/**
		        	 *     ValidateSesionId method validate OTP hoe much time requested for the same sessionId   
		        	 *     if less than 5 times then sending OTP otherwise not sending OTP
		        	 * */
		        	
		          if (paramMap.get("OTP").equals("GENERATE"))// && validate.validateSessionId(node)==true )
		          {
		            OTP = generateOTP();
		            SMSTemplate = SMSTemplate.replaceAll("%OTP%", OTP.toString());
		            this.log.debug("SMSTemplate placeholders replaced : " + SMSTemplate);
		            SMSDataNode.put("OTP", OTP.intValue());
		            ((ObjectNode)node).put("OTP", OTP.intValue());
		            /**
		             * Updating session document for to maintain new OTP and validate previous OTP 
		             * */
				         /*  if(updateSessionkeyDoc(node).equalsIgnoreCase("success")){
				        	log.info("Updated Session Document successfully : "+node.get("sessionId"));   
				           }else{
				        	   log.info("OTP NOT GENRATED : User reached to max limit");
				        	   SMSDataNode.put("mobileNumber", "BLOCKED");
				        	   SMSDataNode.put("sms", serviceConfigNode.content().getString("errorMsg").toString());
					  		      return this.objectMapper.writeValueAsString(SMSDataNode);
				           }*/
		          }else{
		        	  log.info("OTP NOT GENRATED : User reached to max limit");
		        	  SMSDataNode.put("mobileNumber", "BLOCKED");
		        	  SMSDataNode.put("sms", serviceConfigNode.content().getString("errorMsg").toString());
		  		      return this.objectMapper.writeValueAsString(SMSDataNode);
		          }
		        }
		        else {
		          this.log.info(" paramMap does not have request for OTP generation so OTP Skipped");
		        }
		        
		        Iterator<String> iterator = paramMap.keySet().iterator();
		        while (iterator.hasNext())
		        {
		          String paramKey = iterator.next();
		          String paramvalue = paramMap.get(paramKey).toString();
		          this.log.debug(paramKey + " : " + paramvalue);
		          SMSTemplate = SMSTemplate.replaceAll("%" + paramKey + "%", paramvalue);
		        }
	      }
	      /*
	       * prefix Country code removed. 
	       * **/
	        SMSDataNode.put("mobileNumber", node.get("mobileNumber").textValue());
	        if(node.has("funcType"))
	        SMSDataNode.put("funcType", node.get("funcType").textValue());
	        //SMSDataNode.put("country", country);
	        SMSDataNode.put("sms", SMSTemplate);
	        log.info("SMS Response node :"+SMSDataNode);
	      return this.objectMapper.writeValueAsString(SMSDataNode);
	      }
	      
	    }
	    catch (JsonParseException e)
	    {
	      this.log.error("unable to parse JSON input provided, unexpected character occurred : ",e);
	      SMSDataNode.put("mobileNumber", "ERROR");
	      SMSDataNode.put("sms", "ERROR");
	      return this.objectMapper.writeValueAsString(SMSDataNode);
	    }
	    catch (JsonMappingException e)
	    {
	      this.log.error("Please check input values. Unrecognized field occurred, unable to map input values to bean : ",e);
	      SMSDataNode.put("mobileNumber", "ERROR");
	      SMSDataNode.put("sms", "ERROR");
	      return this.objectMapper.writeValueAsString(SMSDataNode);
	    }
	    catch (IOException e)
	    {
	      this.log.error("IOException occurred, unable to write response : ",e);
	      SMSDataNode.put("mobileNumber", "ERROR");
	      SMSDataNode.put("sms", "ERROR");
	      return this.objectMapper.writeValueAsString(SMSDataNode);
	    }
	    catch (Exception e)
	    {
	      this.log.error("unable to send SMS : ",e);
	      SMSDataNode.put("mobileNumber", "ERROR");
	      SMSDataNode.put("sms", "ERROR");
	      return this.objectMapper.writeValueAsString(SMSDataNode);
	    }
    
  }
  
  @SuppressWarnings("unchecked")
public String sendEmailRequest(String emaildetails) throws JsonProcessingException {
	  
	ObjectNode emailDataNode = this.objectMapper.createObjectNode();  
   
    try
    {
      JsonNode node = this.objectMapper.readTree(emaildetails);
      JsonDocument emailTemplateDoc = this.service.getDocBYId("Email-" + node.get("funcType").textValue());
      if(emailTemplateDoc!=null)
      {
    	  
    	  String doc = this.service.getDocBYId("Email-" + node.get("funcType").textValue()).content().toString();
          Map<String, String> valueMap = objectMapper.readValue(doc, Map.class);
          String EMailsubject = (String)valueMap.get("subject");
          String EMailbody = (String)valueMap.get("body");
          if(node.has("paramMap"))
          {
            Map<String, String> paramMap = objectMapper.readValue(node.get("paramMap").toString(), Map.class);
            Iterator<String> iterator = paramMap.keySet().iterator();
            // iterate over map to replace placeholders
            while (iterator.hasNext())
            {
              String paramKey = iterator.next();
              String paramvalue = paramMap.get(paramKey).toString();
              EMailbody = EMailbody.replaceAll("%" + paramKey + "%", paramvalue);
            }
          }
          this.log.debug("Email Body after replacing placeholders : " + EMailbody);
          if(node.has("isBCCRequired")){
        	  emailDataNode.put("username", node.get("username").textValue());
              emailDataNode.put("emailBody", EMailbody);
              emailDataNode.put("emailSubject", EMailsubject);
              emailDataNode.put("isBCCRequired", node.get("isBCCRequired").textValue());
              if(node.has("otp")){
            	  emailDataNode.put("otp", node.get("otp").asText());  
              }
          }else{
        	  emailDataNode.put("username", node.get("username").textValue());
              emailDataNode.put("emailBody", EMailbody);
              emailDataNode.put("emailSubject", EMailsubject);
              emailDataNode.put("isBCCRequired","N");
              if(node.has("otp")){
            	  emailDataNode.put("otp", node.get("otp").asText());  
              }
          }
          return this.objectMapper.writeValueAsString(emailDataNode);
      }
      else
      {
    	  log.error("Email Template document not found : Email-" + node.get("funcType").textValue());
    	  emailDataNode.put("username", "ERROR");
          emailDataNode.put("emailBody", "ERROR");
          emailDataNode.put("emailSubject", "ERROR");
          return this.objectMapper.writeValueAsString(emailDataNode);
      }
      
    }
    catch (JsonParseException e)
    {
      this.log.error("unable to parse JSON input provided, unexpected character occurred",e);
      emailDataNode.put("username", "ERROR");
      emailDataNode.put("emailBody", "ERROR");
      emailDataNode.put("emailSubject", "ERROR");
      return this.objectMapper.writeValueAsString(emailDataNode);
    }
    catch (JsonMappingException e)
    {
      this.log.error("Please check input values. Unrecognized field occurred, unable to map input values to bean",e);
      emailDataNode.put("username", "ERROR");
      emailDataNode.put("emailBody", "ERROR");
      emailDataNode.put("emailSubject", "ERROR");
      return this.objectMapper.writeValueAsString(emailDataNode);
    }
    catch (IOException e)
    {
      this.log.error("IOException occurred, unable to write response");
      emailDataNode.put("username", "ERROR");
      emailDataNode.put("emailBody", "ERROR");
      emailDataNode.put("emailSubject", "ERROR");
      return this.objectMapper.writeValueAsString(emailDataNode);
    }
    catch (Exception e)
    {
      this.log.error("unable to send an email : ",e);
      emailDataNode.put("username", "ERROR");
      emailDataNode.put("emailBody", "ERROR");
      emailDataNode.put("emailSubject", "ERROR");
      return this.objectMapper.writeValueAsString(emailDataNode);
    }
    
  }
  
  public String contactUSInfo(String userdetails)
  {
    return userdetails;
  }
  
  public String replyUser(String userdetails)
  {
	    String emailResponse = null;
	    try
	    {
	      JsonNode userInfoNode = this.objectMapper.readTree(userdetails);
	      ObjectNode emailInput = this.objectMapper.createObjectNode();
	      emailInput.put("username", userInfoNode.findValue("userEmail").textValue());
	      emailInput.put("funcType", userInfoNode.findValue("funcType").textValue());
	      emailInput.put("paramMap", userInfoNode.findValue("paramMap"));
	      emailResponse = sendEmailRequest(emailInput.toString());
	    }
	    catch (Exception e)
	    {
	      emailResponse = "Exception";
	    }
	    return emailResponse;
  }
  
  public String sendInternalNotification(String userdetails)
  {
	    ObjectNode emailInput = this.objectMapper.createObjectNode();
	    
	    try
	    {
	      if (this.supportEmailNode == null)
	      {
	    	  try
	    	  {
	    		  this.supportEmailNode = this.objectMapper.readTree(this.service.getDocBYId("SupportEmailDetails").content().toString());
	    	  }
	    	  catch(Exception e)
	    	  {
	    		  log.error("unable to fetch SupportEmailDetails configuration document");
	    	  }
	        
	      }
	      JsonNode userInfoNode = this.objectMapper.readTree(userdetails);
	      log.info("USER Details :"+userdetails);
	      log.info("USER Functype Details :"+userInfoNode.findValue("funcType").textValue());
	      log.info("USER supportEmailNode :"+supportEmailNode);
	      String supportEmail = this.supportEmailNode.findValue(userInfoNode.findValue("funcType").textValue()).textValue();
	      emailInput.put("username", supportEmail);
	      emailInput.put("funcType", "TEAM-" + userInfoNode.findValue("funcType").textValue());
	      ObjectNode emailBody = this.objectMapper.createObjectNode();
	      emailBody.put("USEREMAIL", userInfoNode.findValue("userEmail").textValue());
	      emailBody.put("EMAILCONTENT", userInfoNode.findValue("emailContent").textValue());
	      emailInput.put("paramMap", emailBody);
	      
	      return sendEmailRequest(emailInput.toString());
	    }
	    catch (Exception e)
	    {
	      this.log.error("unable to send internal notification : ",e);
	      emailInput.put("username", "ERROR");
	      emailInput.put("emailBody", "ERROR");
	      emailInput.put("emailSubject", "ERROR");
	      return emailInput.toString();
	    }
  }
  
//@SuppressWarnings("unchecked")
public String sendNotifications(String msgdetails)
   
  {

	/*SMSEmailResponse response = new SMSEmailResponse();
    
    try
    {
	      JsonNode messageDetails = this.objectMapper.readTree(msgdetails);
	      this.restClient = SyncGatewayTransInstance.getSyncGatewayInstance();
	      String content = this.restClient.getSyncDocument(messageDetails.get("username").asText());
	      this.log.info("content from couchbase sync : " + content);
	      
	      Map<String, Object> valueMap = objectMapper.readValue(content, Map.class);
	      if (valueMap.containsKey("_id"))
	      {
	        this.log.debug("mobileNumber : " + valueMap.get("mobileNumber"));
	        response.setMobileNumber(valueMap.get("mobileNumber").toString());
	      }
	      else
	      {
	        this.log.debug("username : " + messageDetails.get("username").asText() + " is not registered so setting mobileNumber to 0");
	        response.setMobileNumber("0");
	      }
	      response.setFuncType(messageDetails.get("username").asText());
	      response.setUsername(messageDetails.get("username").asText());
	      response.setParamMap(objectMapper.readValue(messageDetails.get("username").asText(),HashMap.class));
	      //this.log.info(this.objectMapper.writeValueAsString(response));
	      return this.objectMapper.writeValueAsString(response);
    }
    catch (Exception e)
    {
	      this.log.error("unable to send user notification : ",e);
	      response.setMobileNumber("0");
	      response.setUsername("ERROR");
	      return this.objectMapper.writeValueAsString(response);
    }*/
   
	return null;
	
  }

	public String updateSessionkeyDoc(JsonNode node){
		try{
	
			String sessionId = node.get("sessionId").asText();
			log.info("session ID in : updateSessionkeyDoc "+sessionId);
			ValidateSessionId validate = new ValidateSessionId();
			JsonObject documentContent = validate.getSessionDocId(node);
			String documentId = documentContent.getString("documentId").toString();
			/*JsonObject documentContent  = sessionKey.getDocBYId(sessionId).content();*/
			log.info("session ID in : updateSessionkeyDoc documentContent : "+documentContent);
			JsonObject previousOTPlist = JsonObject.create();
			if(documentContent!=null){
				JsonNode sessionDocNode = objectMapper.readTree(documentContent.toString());
				if(sessionDocNode.has("previousOTP")){
					/**
					 * insert new OTP and  last OTP moving into previous OTP Array
					 * */
					JsonArray previousOTP = documentContent.getArray("previousOTP"); 
					previousOTPlist.put("creationDate",sessionDocNode.get("creationDate").asText());
					previousOTPlist.put("OTP",sessionDocNode.get("OTP").asInt());
					previousOTP.add(previousOTPlist);
					documentContent.put("previousOTP", previousOTP);
					documentContent.put("creationDate", simpleDate.format(new Date()));
					documentContent.put("OTP",node.get("OTP").asInt());
					int count =(sessionDocNode.get("OTPCount").asInt()+1);	
					documentContent.put("OTPCount",count);
				}else if(sessionDocNode.has("OTP")){
					/**
					 * insert new OTP Second Time and first OTP OR last OTP moving into previous OTP Array
					 * */
					previousOTPlist.put("creationDate",sessionDocNode.get("creationDate").asText());
					previousOTPlist.put("OTP",sessionDocNode.get("OTP").asInt());
					JsonArray previousOTP = JsonArray.create();//empty().add(previousOTPlist.toString());
					previousOTP.add(previousOTPlist);
					documentContent.put("previousOTP",JsonArray.fromJson(previousOTP.toString()));
					documentContent.put("creationDate", simpleDate.format(new Date()));
					documentContent.put("OTP",node.get("OTP").asInt());
					int count =(sessionDocNode.get("OTPCount").asInt()+1);	
					documentContent.put("OTPCount",count);
				}else{
					/**
					 * insert OTP first Time
					 * */
					documentContent.put("creationDate", simpleDate.format(new Date()));
					documentContent.put("OTP",node.get("OTP").asInt());
					documentContent.put("isExpired","N");
					documentContent.put("OTPCount",1);
					documentContent.put("updatedDate",updatedDateFormat.format(new Date()));
				}
				sessionKey.replaceDocument(documentId, documentContent);
				return "success";
			}else{
				log.error("unable to read sessionDocument : "+sessionId);
			}
		}catch(Exception e){
			log.error("unable to update SessionKey document : ",e);
		}
		
		return "failure";
	}	
}
