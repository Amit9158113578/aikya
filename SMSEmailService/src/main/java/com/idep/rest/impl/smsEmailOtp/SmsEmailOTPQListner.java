package com.idep.rest.impl.smsEmailOtp;

import org.apache.log4j.Logger;
import javax.jms.Message;
import javax.jms.TextMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SmsEmailOTPQListner {
	Logger log = Logger.getLogger(SmsEmailOTPQListner.class.getName());
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
			}
		}catch(Exception e){
			log.error("unable to transfer SMSEmailOTPQ request : ",e);
			return queueMsg.toString();	
		}
		return null;
	}
}
