package com.idep.smsemail.request.processor;

import java.util.Random;

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
import com.idep.smsemail.util.SMSConstants;



public class LoginRequestProcessor implements Processor {

	 ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(LoginRequestProcessor.class.getName());
	  CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance(); 
	    
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		   SMSEmailImplService emailImplService=new SMSEmailImplService();
		   ObjectNode loginDataNode = this.objectMapper.createObjectNode();
		   try{
			   String msgdetails = exchange.getIn().getBody().toString();
		  	   JsonNode loginNode = this.objectMapper.readTree(msgdetails);
		  	   String errormsg=null;
		  	 if(loginNode.has(SMSConstants.MOBILE_NO))
		  	 {
		  		   try{
		  		         String mobileNumber = loginNode.get(SMSConstants.MOBILE_NO).asText();
		  		         if(mobileNumber!="" || mobileNumber!=null )
		  		         {
		  			        	JsonDocument serviceConfigNode = serverConfig.getDocBYId("SMSConfig");
		  			        	if(serviceConfigNode==null){
		  			        		log.error("unable to load SMSConfig Document");
		  			        		errormsg="unable to send OTP";
		  			        	}else{
		  			        		errormsg = serviceConfigNode.content().getString("errorMsg").toString();
		  			        	}
		  								 String smsResponse = emailImplService.sendSMSRequest(loginNode.toString());
		  								 if(smsResponse!=null)
		  								 {
			  								 JsonNode smsresponsenode = this.objectMapper.readTree(smsResponse);
			  								 log.info("Mobile number resposne at login time : "+smsresponsenode.get(SMSConstants.MOBILE_NO).asText());
			  								if(smsresponsenode.has(SMSConstants.MOBILE_NO) && !smsresponsenode.get(SMSConstants.MOBILE_NO).asText().equalsIgnoreCase("BLOCKED")){
			  								 loginDataNode.put(SMSConstants.MOBILE_NO, smsresponsenode.get(SMSConstants.MOBILE_NO).textValue());
			  								 loginDataNode.put("sms", smsresponsenode.get("sms").textValue());
			  								 loginDataNode.put("OTP", smsresponsenode.get("OTP").intValue());
//			  								 loginDataNode.put("country", smsresponsenode.get("country").intValue());
			  								 exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.TRUE);
			  								 exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode));
			  								}else {
			  									  this.log.error("unable to found send sms request details : ");
			  									loginDataNode.put("mobileNumber", "BLOCKED");
			  									loginDataNode.put("sms",errormsg );
					  							  exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.FALSE);
					  							  exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode)); 
			  								 }
			  							 }
		  								 else
		  								 {
		  									  this.log.error("unable to found send sms request details : ");
				  							  loginDataNode.put(SMSConstants.MOBILE_NO, "ERROR");
				  							  loginDataNode.put("login", "NULL");
				  							  exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.FALSE);
				  							  exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode)); 
		  								 }
		  		         }
		  		         else
		  		         {
		  		        	 this.log.error("not found mobile number : ");
		  		        	 loginDataNode.put(SMSConstants.MOBILE_NO, "ERROR");
		  					  loginDataNode.put("login", "NULL");
		  					exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.FALSE);
		  					exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode));
		  		         }
		  		     }
		  		    catch(Exception e)
		  		     {
		  			  this.log.error("unable to found user profile Details : ",e);
		  			  loginDataNode.put(SMSConstants.MOBILE_NO, "ERROR");
		  			  loginDataNode.put("login", "ERROR");
		  			exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.FALSE);
		  			exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode)); 
		  		     }
		       	 }
		  	 else
		  	   {
		  		  this.log.error("unable to enter an mobile number Details : ");
		  		  loginDataNode.put(SMSConstants.MOBILE_NO, "ERROR");
		  		  loginDataNode.put("login", "ERROR");
		  		  exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.FALSE);
			  	  exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode)); 
		  	   }
		  	}
		  	catch(Exception e)
		  	{
		  		this.log.error("unable to send an login Details : ",e);
		  		loginDataNode.put(SMSConstants.MOBILE_NO, "ERROR");
		  		loginDataNode.put("login", "ERROR");
		  		exchange.getIn().setHeader(SMSConstants.VALIDATION_FLAG,SMSConstants.FALSE);
		  		exchange.getIn().setBody(this.objectMapper.writeValueAsString(loginDataNode));
		  	}
		  	
		    }
	}


