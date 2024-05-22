package com.idep.smsemail.response.bean;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.smsemail.request.processor.LoginRequestProcessor;
import com.idep.smsemail.util.SMSConstants;

public class EmailResponseProcessor implements Processor{

	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode responseConfigNode;
	  CBService service = null;
	  JsonNode errorNode = null;;
	  Logger log = Logger.getLogger(EmailResponseProcessor.class.getName());
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		 String response = exchange.getIn().getBody().toString();
		 ObjectNode error = this.objectMapper.createObjectNode();
		 JsonNode smsNode = this.objectMapper.readTree(response);
		 try
		    {
		    	  if(this.service==null)
				  {
					  this.service = CBInstanceProvider.getServerConfigInstance();
				      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId("ResponseMessages").content().toString());
				  }
			      
			      
			      if (smsNode.get("mobileNumber").textValue().equals("ERROR"))
			      {
			    	 ObjectNode objectNode = this.objectMapper.createObjectNode();
					 objectNode.put(SMSConstants.RESPONSECODE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue());
					 objectNode.put(SMSConstants.MESSAGE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue());
					 objectNode.put(SMSConstants.RESPONSEDATA, error);
					 exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			      }
			      else if (smsNode.get("mobileNumber").textValue().equals("USER NOT FOUND"))
			      {
			    	     ObjectNode objectNode = this.objectMapper.createObjectNode();
						 objectNode.put(SMSConstants.RESPONSECODE, this.responseConfigNode.get(SMSConstants.MOBILE_NO_NOT_EXIST_CODE).intValue());
						 objectNode.put(SMSConstants.MESSAGE, this.responseConfigNode.get(SMSConstants.MOBILE_NO_NOT_EXIST_MSG).textValue());
						 objectNode.put(SMSConstants.RESPONSEDATA, error);
						 exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			      }
			      else
			      { 
			    	 ObjectNode data = this.objectMapper.createObjectNode();
			    	 ObjectNode objectNode = this.objectMapper.createObjectNode();
					 objectNode.put(SMSConstants.RESPONSECODE, this.responseConfigNode.get(SMSConstants.SUCC_CONFIG_CODE).intValue());
					 objectNode.put(SMSConstants.MESSAGE, this.responseConfigNode.get(SMSConstants.SUCC_CONFIG_MSG).textValue());
					 objectNode.put(SMSConstants.RESPONSEDATA,data);
					 exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			      }
		      
		    }
		    catch (JsonParseException e)
		    {
		    	ObjectNode objectNode = this.objectMapper.createObjectNode();
				 objectNode.put(SMSConstants.RESPONSECODE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue());
				 objectNode.put(SMSConstants.MESSAGE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue());
				 objectNode.put(SMSConstants.RESPONSEDATA, error);
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
		    }
		    catch (IOException e)
		    {
		    	ObjectNode objectNode = this.objectMapper.createObjectNode();
				 objectNode.put(SMSConstants.RESPONSECODE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue());
				 objectNode.put(SMSConstants.MESSAGE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue());
				 objectNode.put(SMSConstants.RESPONSEDATA, error);
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
		    }
		    catch (Exception e)
		    {
		    	ObjectNode objectNode = this.objectMapper.createObjectNode();
		    	 objectNode.put(SMSConstants.RESPONSECODE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_CODE).intValue());
				 objectNode.put(SMSConstants.MESSAGE, this.responseConfigNode.findValue(SMSConstants.ERROR_CONFIG_MSG).textValue());
				objectNode.put(SMSConstants.RESPONSEDATA, error);
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
		    }
		  }
	}


