package com.idep.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bean.UserProfile;
import com.idep.model.UserAccessManager;
import com.idep.util.AgencyConstants;

public class UserProfileProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UserProfileProcessor.class.getName());
	
	public void process(Exchange exchange)
	  {
	    try
	    {
	    	String input = exchange.getIn().getBody().toString();
	    	String action = null;
	    	JsonNode reqNode = this.objectMapper.readTree(input);
	        if(reqNode.has("action"))
	        {
	        	action = reqNode.get("action").asText();
	        }else{
	        	log.error("Action not found..!! ");
	        }
	        if(action.equals("FETCH"))
	        {
		        if(reqNode.has("Id"))
		        {
		        	String UserId = reqNode.get("Id").asText();
			        UserProfile userdata = new UserProfile();
			        userdata = new UserAccessManager().getUserDetails(UserId);
			        
			        String userDetails = objectMapper.writeValueAsString(userdata);
			        JsonNode data = objectMapper.readTree(userDetails);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data", data);
			        exchange.getIn().setBody(finalresultNode);
		        }else{
		        	log.error("User Id  not found..!! ");
		        }
	        }
	        else if(action.equals("UPDATE"))
	        {
		        if(reqNode.has("userDetails"))
		        {
		        	String userDetails = objectMapper.writeValueAsString(reqNode.get("userDetails"));
			        log.info("Inside UserProfileProcessor -- userDetails : "+userDetails);
		        	
			        UserProfile userdata = objectMapper.readValue(userDetails, UserProfile.class);
			        log.info("Inside UserProfileProcessor -- userdata : "+userdata.toString());
			        
			        String status = new UserAccessManager().updateUserDetails(userdata);

			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data", status);
			        exchange.getIn().setBody(finalresultNode);
		        }else{
		        	log.error("User Details not found..!! ");
		        }
	        }
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at UserProfileProcessor ", e);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
	        finalresultNode.put("message", "success");
	        finalresultNode.put("data", "Unable to Update Profile. Please try Again Later!");
	        exchange.getIn().setBody(finalresultNode);
	    }
	  }
}