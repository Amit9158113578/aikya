package com.idep.summaryUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.apache.log4j.Logger;

public class RequestListener
{
  Logger log = Logger.getLogger(RequestListener.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  JsonNode reqInfoNode;
  
  public String onMessage(Message message)
    throws JsonProcessingException, IOException
  {
    try
    {
      if ((message instanceof TextMessage))
      {
        TextMessage text = (TextMessage)message;
        this.reqInfoNode = this.objectMapper.readTree(text.getText());
      }
    }
    catch (JMSException e)
    {
      this.log.error("Exception at ProposalQListener : ", e);
    }
    catch (Exception e)
    {
      this.log.error("Exception at ProposalQListener : ", e);
    }
    return this.reqInfoNode.toString();
  }
}

