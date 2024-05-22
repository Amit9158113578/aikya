package com.idep.travelquote.queue.listener;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author akash.kumawat
 *
 */

public class P365TravelReqQListener {
	Logger log = Logger.getLogger(P365TravelReqQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public String onMessage(Message message){
		String queueMsg=null;
		try{
			if((message instanceof TextMessage)){
				TextMessage text = (TextMessage)message;
				queueMsg = text.getText();
				JsonNode queueMsgNode = objectMapper.readTree(queueMsg);
				this.log.debug("queueMsgNode : " + queueMsgNode);
				return queueMsgNode.toString();
			}else{
				this.log.info("P365TravelReqQListener message is not an instance of TextMessage : "+ message.getJMSDestination());
				return queueMsg;
			}
		}catch(JMSException e){
			this.log.error("P365TravelReqQListener JMS Exception ",e);
			return queueMsg;
		}catch(JsonProcessingException e){
			this.log.error("JsonProcessingException at P365TravelReqQListener : ", e);
			return queueMsg;
		}catch(IOException e){
			this.log.error("P365TravelReqQListener IO Exception ",e);
			return queueMsg;
		}
	}
}
