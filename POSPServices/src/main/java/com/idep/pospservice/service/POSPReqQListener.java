package com.idep.pospservice.service;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class POSPReqQListener {

	Logger log = Logger.getLogger(POSPReqQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public String onMessage(Message message) {
		String queueMsg=null;
		try
		{
			if ((message instanceof TextMessage))
			{
				TextMessage text = (TextMessage)message;
				queueMsg = text.getText();
				JsonNode queueMsgNode = objectMapper.readTree(queueMsg);
				return queueMsgNode.toString();
		 }else
			{
				this.log.error("POSP Request Q   message is not an instance of TextMessage ");
				return queueMsg;
			}
		}
		catch (JMSException e)
		{
			this.log.error("JMSException at P365HealthReqQListener : ", e);
			return queueMsg;
		}
		catch (JsonProcessingException e)
		{
			this.log.error("JsonProcessingException at P365HealthReqQListener : ", e);
			return queueMsg;
		}
		catch (IOException e)
		{
			this.log.error("IOException at P365HealthReqQListener : ", e);
			return queueMsg;
		}
	}
}
