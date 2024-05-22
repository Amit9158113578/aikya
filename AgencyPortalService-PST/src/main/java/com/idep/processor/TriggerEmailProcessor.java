package com.idep.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.model.MiscAccessManager;
import com.idep.util.AgencyConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TriggerEmailProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TriggerEmailProcessor.class.getName());
	
	public void process(Exchange exchange)
	  {
	    try
	    {
	    	String input = exchange.getIn().getBody().toString();
	    	JsonNode errorNode = null;
	    	JsonNode reqNode = this.objectMapper.readTree(input);
	    	if(reqNode.has("action")&& reqNode.get("action").asText().equals("FORGOTPASS"))
	    	{
		    	if(reqNode.has("mobile")&&reqNode.has("email")&&reqNode.has("newPass"))
		    	{
			    	try{
			    		long mobile = reqNode.get("mobile").asLong();
				    	String email = reqNode.get("email").asText();
				    	String newPass = reqNode.get("newPass").asText();
				    	String status = new MiscAccessManager().resetPass(email,mobile,newPass);
				    	
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", status);
				        
				        exchange.getIn().setBody(finalresultNode);
			    	}
			    	catch(Exception e)
			    	{
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "Failure");
				        finalresultNode.put("data", "");
				        exchange.getIn().setBody(finalresultNode);
			    	}
		    	}
	    	}
	    	
	    	else{
		        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
		        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
		        finalresultNode.put("message", "Failure");
		        finalresultNode.put("data", errorNode);
	    		log.info("Required details not present");
	    		
	    		exchange.getIn().setBody(finalresultNode);
	    	}
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at ForgotPasswordProcessor ", e);
	    }
	  }
}
