package com.idep.queue.listener;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
/*
* @author  Sandeep Jadhav
* @version 1.0
* @since   24-APR-2016
*/
public class SMSQueueListener implements MessageListener
{
  Logger log = Logger.getLogger(SMSQueueListener.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService service = CBInstanceProvider.getPolicyTransInstance();
  CBService serviceConfig = CBInstanceProvider.getServerConfigInstance();
  SendSMSService smsservice = new SendSMSService();
  SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
  
  public void onMessage(Message message)
  {
	  String msgdetails = null;
	  
	  try
	  {
    	  
    	  
	      if ((message instanceof TextMessage))
	      {
	        TextMessage text = (TextMessage)message;
	        msgdetails = text.getText();
	        JsonNode SMSNode = this.objectMapper.readTree(msgdetails);
	        String responseKey = "message sending stopped";
	        this.log.info("SMSNode :"+SMSNode);
        
		        if (SMSNode.get("mobileNumber").textValue().equals("ERROR"))
		        {
		          this.log.error("Ignoring as Exception messages arrived in Q ");
		        }
		        else if(SMSNode.get("mobileNumber").textValue().equals("BLOCKED"))
		        {
		        	this.log.error("Mobile Number has been blocked by system : "+SMSNode.get("mobileNumber").textValue());
		        }
		        else
		        {
		          String mobileNo = SMSNode.get("mobileNumber").textValue();
		          // send SMS to the user
		          this.log.info("SMSNode 2:"+SMSNode);
		          responseKey = smsservice.sendSMS(mobileNo, SMSNode.get("sms").textValue(),SMSNode.get("funcType").textValue());
		          this.log.info("message sent successfully on : " + mobileNo + " , Response Key : " + responseKey);
		          JsonObject SMSDocument = JsonObject.create();
		          SMSDocument.put("mobileNumber", mobileNo);
		          SMSDocument.put("SMS", SMSNode.get("sms").textValue());
		          Date date = new Date();
		          
		          SMSDocument.put("createdDateTime", dateFormat.format(date));

		          if(SMSNode.has("OTP"))
		          {
		        	  int otp = SMSNode.get("OTP").intValue();
					  SMSDocument.put("documentType", "GenerateOTP");
		        	  SMSDocument.put("OTP", otp);
		        	  SMSDocument.put("isActive", "Y");
		        	  SMSDocument.put("expirationTime",15);// in minutes
		        	  // create document in database
		        	  try
		        	  {
		        		  this.service.createAsyncDocument("SMS-" +mobileNo+"-"+otp , SMSDocument);
		        	  }
		        	  catch(Exception e)
			          {
			        	  log.error("failed to create SMS document asynchronously ",e);
			          }
		          }
		          else
		          {
			          long sms_seq = serviceConfig.updateDBSequence("SEQSMS");
			          SMSDocument.put("responseKey", responseKey);
			           
			          try
			          {
			        	  this.service.createAsyncDocument("SMS-" + sms_seq, SMSDocument);
			          }
			          catch(Exception e)
			          {
			        	  log.error("failed to create SMS document asynchronously ",e);
			          }
		          }
	        }
	      }
	      else
	      {
	        this.log.error("check message format in SMS Q, message must be an instance of TextMessage");
	      }
    }
    catch (JMSException e)
    {
    	this.log.error("JMSException at SMSQueueListener : ",e);
    }
    catch (JsonParseException e)
    {
    	this.log.error("unable to parse JSON message, check data format : "+msgdetails);
    }
    catch (JsonMappingException e)
    {
    	this.log.error("unable to Map JSON message, check data format : "+msgdetails);
    }
    catch (IOException e)
    {
    	this.log.error("IOException at SMSQueueListener  : ",e);
    }
    catch (Exception e)
    {
    	this.log.error("Exception at SMSQueueListener  : ",e);
    }
  }
}
