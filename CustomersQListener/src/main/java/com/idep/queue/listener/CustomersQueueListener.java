package com.idep.queue.listener;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CustomersQueueListener
{
	static Logger log = Logger.getLogger(CustomersQueueListener.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	public String onMessage(Message message) throws JsonProcessingException, IOException
	{
		String returnURL = null;
		try
		{
			ObjectNode customerRequest = objectMapper.createObjectNode();
			if ((message instanceof TextMessage))
			{
				TextMessage text = (TextMessage)message;
				returnURL = text.getText();
				String []param=null;
				String []params = returnURL.split("\\?")[1].split("\\&");
				for(int i=0 ;i<params.length;i++ ){
					log.info("Params are :"+params[i]);
					param=params[i].split("=");
					for(int j=0;j<param.length;j++){
						if(param[j].toString().equalsIgnoreCase("proposalId")){
							log.info("Proposal ID :"+param[j+1]);
							returnURL=param[j+1].toString();
						}
					}
				}
				customerRequest.put("proposalId", returnURL);
				log.info(" Customer Q message : " + customerRequest);
				message.acknowledge();
			}
			else{
				log.info("Message received by Customer Q is not in expected format, has to be an instance of text message");
			}
			return customerRequest.toString();
		}
		catch (JMSException e)
		{
			log.info("JMSException at CustomerQueueListener : ",e);
			return null;
		}
		catch (NullPointerException e) 
		{
			log.info("Null Pointer Exception :",e);
			return null;
		}
		catch (Exception e)
		{
			log.info("Exception at CustomerQueueListener : ",e);
			return null;
		}
	}
}
