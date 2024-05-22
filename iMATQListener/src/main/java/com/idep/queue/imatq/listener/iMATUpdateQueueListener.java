package com.idep.queue.imatq.listener;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;
/*
 * @author  Gauri Bhalerao
 * @version 1.0
 * @since   05-May-2019
 */
public class iMATUpdateQueueListener 
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(iMATUpdateQueueListener.class.getName());

	public String onMessage(Message message)
	{
		JsonNode reqInfoNode = null; 
		try
		{
			log.info("QueuListener message method"+message);
			if ((message instanceof TextMessage))
			{
				TextMessage text = (TextMessage)message;
				JsonObject qmessage = JsonObject.fromJson(text.getText());

				reqInfoNode = objectMapper.readTree(qmessage.toString());
			} 
		}
		catch (JMSException e)
		{
			this.log.error("JMSException at iMATQueueListener : ",e);
			reqInfoNode = null;
		}
		catch (JsonParseException e)
		{
			this.log.error("JsonParseException at iMATQueueListener  : ",e);
			reqInfoNode = null;
		}
		catch (JsonMappingException e)
		{
			this.log.error("JsonMappingException at iMATQueueListener  : ",e);
			reqInfoNode = null;
		}
		catch (IOException e)
		{
			this.log.error("IOException at iMATQueueListener  : ",e);
			reqInfoNode = null;
		}
		catch (Exception e)
		{
			this.log.error("Exception at iMATQueueListener  : ",e);
			reqInfoNode = null;
		}
		log.info("Request in QueueListener Class "+reqInfoNode);
		return reqInfoNode.toString(); 
	}
}
