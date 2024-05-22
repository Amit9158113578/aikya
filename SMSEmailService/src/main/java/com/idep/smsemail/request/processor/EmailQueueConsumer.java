package com.idep.smsemail.request.processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.jms.JMSException;
import javax.mail.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
/**
* 
* @author  Sandeep Jadhav
* @version 1.0
* @since   25-APR-2016
*/
public class EmailQueueConsumer
{
  Logger log = Logger.getLogger(EmailQueueConsumer.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  CBService transService = CBInstanceProvider.getPolicyTransInstance();
  CBService service = CBInstanceProvider.getServerConfigInstance();
 // SendEmailService emailservice = new SendEmailService();
  SimpleDateFormat simpleDate =new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
  
  public void onMessage(String message)
  {
    try
    {
      log.info("flow in EmailQueueConsumer"+message);
         
	        JsonObject emailNode = JsonObject.fromJson(message);
	        
	        if (emailNode.getString("username").equals("ERROR"))
	        {
	          log.error("Ignoring Exception emails arrived in Q, EmailBody : " + emailNode.getString("emailBody"));
	        }
	        else
	        {
	          sendEmail(emailNode.getString("emailSubject"), emailNode.getString("emailBody"), emailNode.getString("username"),emailNode.getString("isBCCRequired"));
	          log.info("email sent successfully to : " + emailNode.getString("username"));
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
    catch (Exception e)
    {
    	log.error("failed to send email : ",e);
    }
  }
  public void sendEmail(String subject, String body, String emailId, String isBCCRequired) throws MessagingException {
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  
	  JsonObject emailConfigNode = null;
	  
	  String defaultBCCNode = null;
	  
	  Session session = null;
	    Properties props = System.getProperties();
	    if (isBCCRequired.equalsIgnoreCase("Y"))
	      defaultBCCNode = ((JsonObject)serverConfig.getDocBYId("DefaultBCCConfiguration").content()).getString("emailid").toString(); 
	    if (emailConfigNode == null) {
	      emailConfigNode = (JsonObject)serverConfig.getDocBYId("EMAILAPI-Configurations").content();
	      log.info("emailConfigNode : " + emailConfigNode);
	      props.setProperty(emailConfigNode.getString("hostKey"), emailConfigNode.getString("hostValue"));
	      props.setProperty(emailConfigNode.getString("portKey"), emailConfigNode.getString("portValue"));
	      props.setProperty(emailConfigNode.getString("sslKey"), emailConfigNode.getString("sslValue"));
	      props.setProperty(emailConfigNode.getString("authKey"), emailConfigNode.getString("authValue"));
	      props.put(emailConfigNode.getString("quitWaitKey"), emailConfigNode.getString("quitWaitValue"));
	      session = Session.getInstance(props, null);
	      log.info("properties loaded, session created");
	    } 
	      Transport transport = session.getTransport(emailConfigNode.getString("transport"));
	    try {
	      MimeMessage msg = new MimeMessage(session);
	      msg.setFrom((Address)new InternetAddress(emailConfigNode.getString("senderEmail"), emailConfigNode.getString("senderName")));
	      msg.setRecipients(Message.RecipientType.TO, (Address[])InternetAddress.parse(emailId, false));
	      InternetAddress[] BCCArray = null;
	      if (defaultBCCNode != null) {
	        BCCArray = InternetAddress.parse(defaultBCCNode, false);
	        msg.setRecipients(Message.RecipientType.BCC, (Address[])BCCArray);
	      } 
	      msg.setSubject(subject);
	      msg.setContent(body, emailConfigNode.getString("contentType"));
	      msg.setSentDate(new Date());
	      log.info("transport received");
	      log.info("connecting transport");
	      transport.connect(emailConfigNode.getString("hostValue"), 
	          emailConfigNode.getString("senderUser"), 
	          emailConfigNode.getString("senderPass"));
	      log.info("trying to send email : " + msg.getAllRecipients());
	      transport.sendMessage((Message)msg, msg.getAllRecipients());
	      transport.close();
	      if (BCCArray != null)
	        log.info("email sent  successfully to BCC Recipients: " + BCCArray); 
	      log.info("Email sent  successfully to : " + emailId);
	    } catch (MessagingException e) {
	      log.error("SendEmailService : Failed to send message : ", (Throwable)e);
	    } catch (Exception e) {
	      log.error("SendEmailService : Exception occurred : ", e);
	    } 
	    finally{
		      transport.close();
	    }
	  }


}
