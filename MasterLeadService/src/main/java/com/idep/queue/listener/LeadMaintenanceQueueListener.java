package com.idep.queue.listener;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;


public class LeadMaintenanceQueueListener {

	  Logger log = Logger.getLogger(LeadMaintenanceQueueListener.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  
	  public String onMessage(Message message)
	    throws JsonProcessingException, IOException
	  {
		  
	    JsonNode reqInfoNode = null;
	    try
	    {
	      if ((message instanceof TextMessage))
	      {
	        TextMessage text = (TextMessage)message;
	        this.log.info(" Lead Q message : " + text.getText());
	        
	        reqInfoNode = this.objectMapper.readTree(text.getText());
	        
	        message.acknowledge();
	        this.log.info("message acknowledged by client");
	      }
	      else
	      {
	        this.log.error("Message received by Leads Q is not in expected format, has to be an instance of text message");
	      }
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at LeadsQueueListener : ", e);
	    }
	    return reqInfoNode.toString();
	  }
}
