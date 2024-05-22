package com.idep.queue.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.comm.service.SendEmailService;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
/**
* 
* @author  Sandeep Jadhav
* @version 1.0
* @since   25-APR-2016
*/
public class EmailQueueConsumer implements MessageListener
{
  Logger log = Logger.getLogger(EmailQueueConsumer.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService transService = CBInstanceProvider.getPolicyTransInstance();
  CBService service = CBInstanceProvider.getServerConfigInstance();
  SendEmailService emailservice = new SendEmailService();
  SimpleDateFormat simpleDate =new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
  
  public void onMessage(Message message)
  {
    try
    {
      
      if ((message instanceof TextMessage))
      {
       
	        TextMessage text = (TextMessage)message;
	        String msg = text.getText();
	        
	        JsonObject emailNode = JsonObject.fromJson(msg);
	        
	        if (emailNode.getString("username").equals("ERROR"))
	        {
	          this.log.error("Ignoring Exception emails arrived in Q, EmailBody : " + emailNode.getString("emailBody"));
	        }
	        else
	        {
	          emailservice.sendEmail(emailNode.getString("emailSubject"), emailNode.getString("emailBody"), emailNode.getString("username"),emailNode.getString("isBCCRequired"));
	          this.log.info("email sent successfully to : " + emailNode.getString("username"));
	        }
	        
	        
	        
	        try
	        {
	        	JsonNode emailOtpNode = objectMapper.readTree(emailNode.toString());
	        	if(emailOtpNode.has("otp")){
	        		Date sysDate = new Date();
	        		emailNode.put("createdDateTime", simpleDate.format(sysDate));
	        		emailNode.put("isActive", "Y");
	        		emailNode.put("expirationTime", 15);
	        		String docId = emailOtpNode.get("username").asText()+"-"+emailOtpNode.get("otp").asText();
	        		transService.createAsyncDocument(docId, emailNode);
	        		log.info("OTP email document stored : "+docId);
	        	}else{
	        	long email_seq = service.updateDBSequence("SEQEMAIL");
	        	transService.createAsyncDocument("Email" + email_seq, emailNode);
	        	}
	        }
	        catch(Exception e)
	        {
	        	log.error("failed to create Email document asynchronously");
	        }
	        
      }
      else
      {
    	  this.log.error("check message format in EmailsQ, message must be an instance of TextMessage");
      }
    }
    catch (JMSException e)
    {
      this.log.error("failed to send email : ",e);
    }
    catch (Exception e)
    {
    	this.log.error("failed to send email : ",e);
    }
  }
}
