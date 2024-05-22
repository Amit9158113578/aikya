package com.idep.smsemail.response.bean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.smsemail.util.SMSConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.io.IOException;

import org.apache.log4j.Logger;

public class SMSResponse
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(SMSResponse.class.getName());
  JsonNode responseConfigNode;
  CBService service = null;
  JsonNode errorNode = null;
  
  public String getSMSData(String smsdata) 
  {

	    ObjectNode node = this.objectMapper.createObjectNode();
	  
	    try
	    {
	    	if(this.service==null)
			  {
				  this.service = CBInstanceProvider.getServerConfigInstance();
			      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId("ResponseMessages").content().toString());
			  }
	    	
	      JsonNode smsNode = this.objectMapper.readTree(smsdata);
	      
	      if (smsNode.get("mobileNumber").textValue().equals("ERROR"))
	      {
	    	  return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
	      }
	      else if (smsNode.get("mobileNumber").textValue().equals("BLOCKED"))
	      {
	    	  return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.BLOCKED_MOBILES_CODE).intValue(), smsNode.get("sms").textValue(), this.errorNode);
	      }
	      
	      else
	      {
	    	  return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.SUCC_CONFIG_CODE).intValue(), this.responseConfigNode.get(SMSConstants.SUCC_CONFIG_MSG).textValue(), node);
	      }
	      
	    }
	    catch (JsonParseException e)
	    {
	    	this.log.error("JsonParseException : ",e);
	    	return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
	    }
	    catch (JsonMappingException e)
	    {
	    	this.log.error("JsonMappingException :",e);
	    	return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
	    }
	    catch (IOException e)
	    {
	    	this.log.error("IOException : ",e);
	    	return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
	    }
	    catch (Exception e)
	    {
	    	this.log.error("Exception : ",e);
	    	return ResponseSender.createResponse(this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.get(SMSConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
	    }
	    
	  }
}
