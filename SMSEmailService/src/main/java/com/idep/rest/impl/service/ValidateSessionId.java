package com.idep.rest.impl.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.jms.Session;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.smsemail.exception.ExecutionTerminator;
import com.idep.smsemail.util.SMSConstants;



public class ValidateSessionId {

	 Logger log = Logger.getLogger(ValidateSessionId.class.getName());
	 ObjectMapper objectMapper = new ObjectMapper();
	 CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	 CBService sessionkeyConfig = CBInstanceProvider.getBucketInstance(SMSConstants.SESSIONKEYBUCKET);
	 SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
	 SimpleDateFormat updatedDateFormat =new SimpleDateFormat("yyyy-MM-dd");
	 JsonNode serviceConfigNode=null;
	@SuppressWarnings("unused")
	public  boolean validateSessionId(JsonNode inputReq) throws ExecutionTerminator{
		try{
			String documentId=null;
			List<JsonObject> documentList=null;
			JsonObject documentContent =null;
			
			 if (this.serverConfig == null)
			 {
				this.serverConfig = CBInstanceProvider.getServerConfigInstance(); 
			    serviceConfigNode = this.objectMapper.readTree(serverConfig.getDocBYId("SMSConfig").content().toString());
			    log.info("SMSConfig Doc found in DB ");
			 }
			 
			if(inputReq.has("sessionId")){
				
			JsonObject docObj = JsonObject.create();
				String sessionId = inputReq.findValue("sessionId").asText();
				if(sessionId.length()>0){
					JsonArray paramobj = JsonArray.create();
					paramobj.add(sessionId);
					documentList=sessionkeyConfig.executeConfigParamArrQuery(SMSConstants.SESSIONIDQUERY,paramobj);
					log.debug("Query : out put : "+documentList);
					if(documentList.size()>0 && documentList!=null){
						documentContent =documentList.get(0);
						documentId = documentContent.getString("documentId").toString();
					}
					
									log.info("Session ID in UI Request :"+sessionId);
									System.out.println("documentContent : "+documentContent);
										docObj =documentContent;
										JsonNode sessionDocNode= objectMapper.readTree(docObj.toString());
										if(sessionDocNode.has("previousOTP")){
											/** 
											 * if user user requested OTP  same Session id more than 5 then we are not sending SMS to customer 
											 * **/
											/*if(sessionDocNode.has("previousOTP")){
												log.info("previous OTP Array : "+sessionDocNode.get("previousOTP"));
											}*/
											if(sessionDocNode.get("OTPCount").asInt()==serviceConfigNode.get("otpMaxLimit").asInt()){
												/*
												 * SMS max limit reached , isExpired flag set as invalid and also user blocked for 1 hour 
												 */
												log.info("User has reached to max OTP limit");
												return false;
											}else{
												return true;
											}
										}else if(sessionDocNode.has("OTP")){
											/**Sending  true for this is valid sessionId and  **/
											log.info("SessionId Valid and user request less than max limit");
											return true;
										}else{
											docObj.put("mobileNumber", inputReq.get("mobileNumber").asText());
											sessionkeyConfig.replaceDocument(documentId, docObj);
											log.info("SessionId document in Mobile number updated  ");
											/**Sending  true for this is valid sessionId and user first time request for OTP **/
											return true;
										}
									
					
				}else{
					log.error("Unable to validate SessionId not found in Request : "+inputReq);
					
					//throw new ExecutionTerminator();
				}
			}else{
				log.error("Unable to validate SessionId not found : "+inputReq);
				//throw new ExecutionTerminator();
			}
		}catch(Exception e){
			log.error("Unable to validate SessionId :",e);
			e.printStackTrace();
			throw new ExecutionTerminator();
		}
		return false;
		
	}
	
	public  boolean verifyUserBlocked(JsonNode inputReq) throws ExecutionTerminator{
		
		
		try{
			List<JsonObject> sessiondocList=null;
			String mobileNo= inputReq.get("mobileNumber").asText();
			this.serverConfig = CBInstanceProvider.getServerConfigInstance(); 
		    serviceConfigNode = this.objectMapper.readTree(serverConfig.getDocBYId("SMSConfig").content().toString());
			JsonArray paramobj = JsonArray.create();
			paramobj.add(mobileNo);
			paramobj.add(updatedDateFormat.format(new Date()));
			log.info("passing param to Query : "+paramobj);
			sessiondocList=sessionkeyConfig.executeConfigParamArrQuery(SMSConstants.MOBILESESSIONIDQUERY, paramobj);
			log.debug("QUERY OUT PUT FOR SessionID user : "+sessiondocList);
			if(sessiondocList.size() > 0 && sessiondocList!=null){
				JsonNode sessions = objectMapper.readTree(sessiondocList.get(0).toString());
				log.info("User document found in SessionKey  : "+sessions);
				String otpCreated = sessions.get("creationDate").textValue();
				Date date = new Date();
				String currentDate = simpleDate.format(date);
				// convert string to date format
				Date otpCreatedDate = simpleDate.parse(otpCreated);
				Date currentSysDate = simpleDate.parse(currentDate);
				long diff = currentSysDate.getTime() - otpCreatedDate.getTime();
				long diffMinutes = diff / (60 * 1000) % 60;
				long diffHours = diff / (60 * 60 * 1000) % 24;
				long diffDays = diff / (24 * 60 * 60 * 1000);
				log.info(" diffMinutes : "+diffMinutes+" diffHours : "+diffHours);
				if(sessions.has("OTPCount")){
				if(sessions.get("OTPCount").asInt()==serviceConfigNode.get("otpMaxLimit").asInt() && (diffHours < 1 && diffMinutes < serviceConfigNode.get("userBlockedMinutes").asInt()))
				{
					log.error("user has been blocked for 60 mints user tried multiple OTP : "+mobileNo);
					return false;
				}
				else
				{
					return true;
				}
				}else{
					return true;
				}
			}else{
				/*
				 * Valid user  
				 * **/
				return true;
			}
		}catch(Exception e){
			log.error("unable to validate user sessionId : ",e);
		}
		return false;

	}
	
	
	public JsonObject getSessionDocId(JsonNode inputReq){
		JsonObject documentCont =null;
		try{
		JsonArray paramobj = JsonArray.create();
		paramobj.add(inputReq.get("sessionId").asText());
		List<JsonObject> documentList=sessionkeyConfig.executeConfigParamArrQuery(SMSConstants.SESSIONIDQUERY,paramobj);
		//log.info("Query : : "+documentList);
		documentCont=JsonObject.create();
		if(documentList.size()>0 && documentList!=null){
			documentCont =documentList.get(0);
		}
		}catch(Exception e){
			log.error("unable to find SessonId document : "+inputReq.get("sessionId").asText());
			log.error("unable to find SessonId document : ",e);	
		}
		return documentCont;
	}
}
