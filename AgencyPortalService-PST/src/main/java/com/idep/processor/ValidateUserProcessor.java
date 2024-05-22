package com.idep.processor;

import org.apache.camel.Exchange;


import com.idep.Tokenizer.API.Tokenizer;

import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bean.User;
import com.idep.model.MiscAccessManager;
import com.idep.model.UserAccessManager;
import com.idep.util.AgencyConstants;

public class ValidateUserProcessor implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ValidateUserProcessor.class.getName());
	
	public void process(Exchange exchange)
	  {
	    try
	    {
	        String credentials = exchange.getIn().getBody().toString();
	        log.info("Inside ValidateUserProcessor user input --"+credentials);
	        JsonNode errorNode = null;
	        JsonNode reqNode = this.objectMapper.readTree(credentials);
	        String Username = reqNode.get("Username").asText();
	        String pass = reqNode.get("Password").asText();
	        
			User user = new User();
			String role = new String();
			user = new UserAccessManager().validateUser(Username, pass);
	        role = new MiscAccessManager().getRole(user.getRoleId());
	                 
   	        String userdata = objectMapper.writeValueAsString(user);
	        JsonNode data = objectMapper.readTree(userdata);
	        ((ObjectNode)data).put("role", role);
	       //write logic for sending token
	        
	        String token=Tokenizer.createToken(Username);
	        ((ObjectNode)data).put("token",token);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
		    if(user.getIsActive()=="N")
		        {
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "User is not active");
			        finalresultNode.put("data", errorNode);
		        	exchange.getIn().setBody(finalresultNode);
		        }
		        else
		        {   finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data", data);
		        	exchange.getIn().setBody(finalresultNode);
		        }
		    exchange.getIn().setBody(finalresultNode);
	        }
	    catch (Exception e)
	    {
		      this.log.error("Exception at ValidateUserProcessor ", e);
		      ObjectNode finalresultNode = this.objectMapper.createObjectNode();  
		      finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
		      finalresultNode.put("message", "Failure");
		      finalresultNode.put("data",e.getMessage());
		      exchange.getIn().setBody(finalresultNode);
	    }
	  }
}
