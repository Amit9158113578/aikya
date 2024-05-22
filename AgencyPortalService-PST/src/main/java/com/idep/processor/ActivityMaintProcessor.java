package com.idep.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.model.ActivityLogAccessManager;
import com.idep.util.AgencyConstants;

public class ActivityMaintProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ActivityMaintProcessor.class.getName());
	
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
		        	String activityData = new String();
		        	activityData = new ActivityLogAccessManager().getActivityLog();
			        
			        if(activityData.isEmpty())
			        {
			        	JsonNode data = objectMapper.readTree(activityData);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "Failure");
				        finalresultNode.put("data", data);
				        exchange.getIn().setBody(finalresultNode);
			        }else{
			        	JsonNode data = objectMapper.readTree(activityData);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode",  AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", data);
				        exchange.getIn().setBody(finalresultNode);
			        }
	        }
	        else if(action.equals("DELETE"))
	        {
	        	int Id = 0;
	        	if(reqNode.has("Id"))
		        {
		        	Id = reqNode.get("Id").asInt();
		        }
	        	String status = new String();
	        	status = new ActivityLogAccessManager().deleteActivityLog(Id);
		       
		        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
		        finalresultNode.put("responseCode",  AgencyConstants.SUCCESS_CODE);
		        finalresultNode.put("message", "success");
		        finalresultNode.put("data", status);
		        exchange.getIn().setBody(finalresultNode);
	        }
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at ActivityMaintProcessor ", e);
	    }
	  }
}
