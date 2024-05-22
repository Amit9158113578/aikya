package com.idep.proposal.impl.service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.res.processor.ProposalResProcessor;




public class TravelPolicyDocServiceImpl {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelPolicyDocServiceImpl.class.getName());
	
	public String onMessage(Message message) throws ExecutionTerminator
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
	      log.error("Exception at TravelPolicyDocServiceImpl : ",e);
	    }
	    catch (Exception e)
	    {
	      log.error("Exception at TravelPolicyDocServiceImpl : ",e);
	    }
    log.info("request from TravelPolicyDocServiceImpl Q : "+request);
    return request;
	}
	
	public String sendMessage(String proposal)
	{
		return proposal;
	}

}
