package com.idep.policy.queue.listener;

import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PolicyDocumentReqQListener {
	
	
	Logger log = Logger.getLogger(PolicyDocumentReqQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	  
	  public String onMessage(Message message) throws JsonProcessingException, IOException
	  {
		  String request = "";
		  
		    try
		    {
		      if ((message instanceof TextMessage))
		      {
		        TextMessage text = (TextMessage)message;
		        request = text.getText();
		      }
		    }
		    catch (JMSException e)
		    {
		      log.error("Exception at PolicyReqQListener : ",e);
		    }
		    catch (Exception e)
		    {
		      log.error("Exception at PolicyReqQListener : ",e);
		    }
	    log.info("request from PolicyDoc Q : "+request);
	    return request;
	  }

}
