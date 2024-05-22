package com.idep.profession.request.validation;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PBQuoteListener {

	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode reqInfo;
	public String onMessage(Message message) throws JsonProcessingException, IOException, JMSException
	  {
		 if ((message instanceof TextMessage))
	        {
	          	TextMessage text = (TextMessage)message;
	           reqInfo = this.objectMapper.readTree(text.getText());
	           return this.reqInfo.toString();
	        }
		   return reqInfo.toString();
     }
}
