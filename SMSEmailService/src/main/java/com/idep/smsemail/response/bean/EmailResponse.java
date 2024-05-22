package com.idep.smsemail.response.bean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.smsemail.util.SMSConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.io.IOException;

import org.apache.log4j.Logger;

public class EmailResponse
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(EmailResponse.class.getName());
  JsonNode responseConfigNode;
  JsonNode node;
  CBService service = null;
  JsonNode errorNode = null;
  
  public String getEmailData(String emaildata) 
  {
    try
    {
    	  if(this.service==null)
		  {
			  this.service = CBInstanceProvider.getServerConfigInstance();
		      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId("ResponseMessages").content().toString());
		  }
	      JsonNode emailNode = this.objectMapper.readTree(emaildata);
	      if (emailNode.get("username").textValue().equals("ERROR"))
	      {
	    	  this.log.info("failure while preparing email template");
	    	  return ResponseSender.createResponse(this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
	      }
	      else
	      {
	    	  return ResponseSender.createResponse(this.responseConfigNode.findValue(SMSConstants.SUCC_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(SMSConstants.SUCC_CONFIG_MSG).textValue(), node);
	      }
      
    }
    catch (JsonParseException e)
    {
    	return ResponseSender.createResponse(this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
    catch (IOException e)
    {
    	return ResponseSender.createResponse(this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
    catch (Exception e)
    {
    	return ResponseSender.createResponse(this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
  }
}
