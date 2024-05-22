package com.idep.queue.listener;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;

public class OfflineCustomerQListener {
	static Logger log = Logger.getLogger(OfflineCustomerQListener.class.getName());
	public String onMessage(Message message) throws JsonProcessingException, IOException
	{
		String offlineCustReq = null;
		try
		{
			if ((message instanceof TextMessage))
			{
				TextMessage text = (TextMessage)message;
				offlineCustReq = text.getText();
				return offlineCustReq;
			}
			log.info("Message received by offline Customer Q is not in expected format, has to be an instance of text message");
		}
		catch (JMSException e)
		{
			log.info("JMSException at OfflineCustomerQListener : ",e);
		}
		catch (NullPointerException e) 
		{
			log.info("Null Pointer Exception :",e);
		}
		catch (Exception e)
		{
			log.info("Exception at OfflineCustomerQListener : ",e);

		}
		return null;
	}
}
