package com.idep.processor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bean.UserProfile;
import com.idep.model.AgentAccessManager;
import com.idep.util.AgencyConstants;

public class AgentMaintProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AgentMaintProcessor.class.getName());
	
	public void process(Exchange exchange)
	  {
	    try
	    {
	    	String input = exchange.getIn().getBody().toString();

	    	String action=null;
	    	String sessionId = null;
	    	JsonNode reqNode = this.objectMapper.readTree(input);

	        if(reqNode.has("action")&&reqNode.has("sessionId"))
	        {
	        	action = reqNode.get("action").asText();
	        	sessionId = reqNode.get("sessionId").asText();
	        }else{
	        	log.error("Action/Session Id not found..!! ");
	        }

	        if(action.equals("ADD") || action.equals("UPDATE"))
	        {
		    	String status = new String();
		    	String agentDetails = null;
		    	log.info("Inside AgentMaintProcessor -- action : "+action);
		        if(reqNode.has("agentDetails"))
		        {
		        	agentDetails = objectMapper.writeValueAsString(reqNode.get("agentDetails"));
		        	log.info("Inside AgentMaintProcessor -- agentDetails : "+agentDetails);
		        }else{
		        	log.error("Agent details not found..!! ");
		        }
		        
	        	UserProfile agent = objectMapper.readValue(agentDetails, UserProfile.class);
	        	log.info("Inside AgentMaintProcessor -- agent object : "+objectMapper.writeValueAsString(agent));
	        	try{
	        		if(action.equals("ADD")){
	        			status = new AgentAccessManager().addAgent(agent,sessionId);
	        		}
	        		else if (action.equals("UPDATE"))
	        		{
	        			status = new AgentAccessManager().updateAgent(agent,sessionId);
	        		}
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        if(AgencyConstants.ADD_NEW_AGENT_FAILURE==status || AgencyConstants.AGENT_UPDATE_FAILURE==status){
			        	finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "failure");
			        }else{
			        	finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
			        }
			        
			        finalresultNode.put("data", status);
			        exchange.getIn().setBody(finalresultNode);
	        	}catch (Exception e){
	        		this.log.error("Exception at AgentMaintProcessor while adding or updating agent", e);
			        
	        		JsonNode data = objectMapper.readTree(status);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "failure");
			        finalresultNode.put("data", data);
			        exchange.getIn().setBody(finalresultNode);
	        	}
	        }
	        else if (action.equals("FETCH"))
	        {
	        	String agentId =  new String();
	        	UserProfile agentDetails = new UserProfile();
	        	if(reqNode.has("agentId"))
		        {
	        		agentId = reqNode.get("agentId").asText();
		        }else{
		        	log.error("Agent details not found in request..!! ");
		        }        	    	
	        	try{
	        		agentDetails = new AgentAccessManager().fetchAgent(agentId);
		        	String agentdata = objectMapper.writeValueAsString(agentDetails);
		        	log.info("Final Agent data : "+agentdata);
		        	JsonNode data = objectMapper.readTree(agentdata);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data", data);
			        exchange.getIn().setBody(finalresultNode);
	        	}catch (Exception e){
	        		this.log.error("Exception at AgentMaintProcessor--addAgency", e);
	        		String agentdata = objectMapper.writeValueAsString(agentDetails);
	        		JsonNode data = objectMapper.readTree(agentdata);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "failure");
			        finalresultNode.put("data", data);
			        exchange.getIn().setBody(finalresultNode);
	        	}
	        }
	        else if (action.equals("DELETE"))
	        {	
	        	
	        	String status = new String();
	        	if(reqNode.has("agentId"))
		        {
	        		String agentId =  new String();
	        		agentId = reqNode.get("agentId").asText();      	    	
		        	try{
		        		status = new AgentAccessManager().deleteAgent(agentId,sessionId);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", status);
				        exchange.getIn().setBody(finalresultNode);
		        	}catch (Exception e){
		        		this.log.error("Exception at AgentMaintProcessor--deleteAgent", e);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "failure");
				        finalresultNode.put("data", status);
				        exchange.getIn().setBody(finalresultNode);
		        	}
		        }  
	        }
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at AgentMaintProcessor ", e);
	    }
	  }
}
