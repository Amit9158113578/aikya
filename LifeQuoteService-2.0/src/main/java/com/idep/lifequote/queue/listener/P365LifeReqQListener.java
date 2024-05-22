package com.idep.lifequote.queue.listener;

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
 * @author yogesh.shisode
 *
 */
public class P365LifeReqQListener {
	Logger log = Logger.getLogger(P365LifeReqQListener.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public String onMessage(Message message){
		String queueMsg=null;
		try{
			if((message instanceof TextMessage)){
				TextMessage text = (TextMessage)message;
				queueMsg = text.getText();
				JsonNode queueMsgNode = objectMapper.readTree(queueMsg);
				return queueMsgNode.toString();
			}else{
				this.log.info("P365LifeReqQListener message is not an instance of TextMessage : "+ message.getJMSDestination());
				return queueMsg;
			}
		}catch(JMSException e){
			this.log.error("P365LifeReqQListener JMS Exception ",e);
			return queueMsg;
		}catch(JsonProcessingException e){
			this.log.error("JsonProcessingException at P365LifeReqQListener : ", e);
			return queueMsg;
		}catch(IOException e){
			this.log.error("P365LifeReqQListener IO Exception ",e);
			return queueMsg;
		}
	}
}