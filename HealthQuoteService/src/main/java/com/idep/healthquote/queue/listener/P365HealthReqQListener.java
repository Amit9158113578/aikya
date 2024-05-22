package com.idep.healthquote.queue.listener;

import java.io.IOException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class P365HealthReqQListener {

	Logger log = Logger.getLogger(P365HealthReqQListener.class.getName());
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
				//log.info("p365msg: " +queueMsgNode.toString());

				ObjectNode planType = (ObjectNode) queueMsgNode.get("inputMessage").get("quoteParam");
				ObjectNode productInfo = (ObjectNode)queueMsgNode.get("inputMessage").get("productInfo");
				
				if(productInfo.has("carrierId") && productInfo.has("planId"))
				{

				if((productInfo.get("carrierId").asInt() == 25 && productInfo.get("planId").asInt() == 60) ||(productInfo.get("carrierId").asInt() == 25 && productInfo.get("planId").asInt() == 61)||(productInfo.get("carrierId").asInt() == 60 && productInfo.get("planId").asInt() == 75) || (productInfo.get("carrierId").asInt() == 60 && productInfo.get("planId").asInt() == 76) || (productInfo.get("carrierId").asInt() == 60 && productInfo.get("planId").asInt() == 77) || (productInfo.get("carrierId").asInt() == 60 && productInfo.get("planId").asInt() == 78))

				{
					if(planType.has("planType") && productInfo.has("planType"))
					{					
						if(planType.get("planType").textValue().equalsIgnoreCase("F"))
			 		{
						planType.put("planType","I");
						productInfo.put("planType", "I");
						
					}
					}
					//log.info("Updated p365msg with quoteParam plantype:: " +queueMsgNode.toString());
				}
				
				
			}
				return queueMsgNode.toString();
				}
			else
			{
				this.log.error("P365HealthReqQ  message is not an instance of TextMessage ");
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
