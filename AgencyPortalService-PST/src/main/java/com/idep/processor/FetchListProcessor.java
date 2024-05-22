package com.idep.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.model.AgencyAccessManager;
import com.idep.model.AgentAccessManager;
import com.idep.model.MiscAccessManager;
import com.idep.util.AgencyConstants;

public class FetchListProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FetchListProcessor.class.getName());
	
	public void process(Exchange exchange)
	  {
	    try
	    {
	    	String input = exchange.getIn().getBody().toString();
	    	JsonNode errorNode = null;
	    	JsonNode reqNode = this.objectMapper.readTree(input);
	    	if(reqNode.has("action")&&reqNode.has("roleId")&& reqNode.has("username")&& reqNode.get("action").asText().equals("FETCH_AGENCY_LIST"))
	    	{
		        long roleId = reqNode.get("roleId").asLong();
		        String user = reqNode.get("username").asText();
		        log.info("Inside FetchListProcessor -- Input Role Id : "+roleId);
		        
		        String role = new MiscAccessManager().getRole(roleId);
		        
		        log.info("Inside FetchListProcessor -- Role Name : "+role);
		        
		        if(role!=null)
		        {
		        	try{
			        	String agencylist = new String();
			        	String agentlist = new String();
			        	agencylist = new AgencyAccessManager().fetchAgencyList();
			        	agentlist = new AgentAccessManager().fetchAgentList(role,user);
			        	JsonNode agencyDetails = objectMapper.readTree(agencylist);
				        JsonNode agentDetails = objectMapper.readTree(agentlist);
				        ObjectNode data = this.objectMapper.createObjectNode();
				        data.put("agencyDetails", agencyDetails);
				        data.put("agentDetails", agentDetails);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", data);
				        
				        exchange.getIn().setBody(finalresultNode);
		        	}
		        	catch(Exception e){
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "Failure");
				        finalresultNode.put("data", "");
				        
				        exchange.getIn().setBody(finalresultNode);
		        	}
		        }
		        else
		        {
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "Not Authorised");
			        finalresultNode.put("data", errorNode);
			        
			        exchange.getIn().setBody(finalresultNode);
		        }
	    	}
	    	else if(reqNode.has("action")&&reqNode.has("agencyId")&& reqNode.has("username")&& reqNode.get("action").asText().equals("FETCH_REPORT_TO"))
	    	{
	    		try{
		    		String agencyId = reqNode.get("agencyId").asText();
		    		String managerTeamleadlist = new String();
		    		managerTeamleadlist = new MiscAccessManager().fetchManagerTeamleadList(agencyId);
			        JsonNode data = objectMapper.readTree(managerTeamleadlist);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data", data);
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
	    	else if(reqNode.has("action")&& reqNode.has("username")&&reqNode.get("action").asText().equals("FETCH_ROLE"))
	    	{
	    		log.info("Action :"+reqNode.get("action").asText());
		    		try{
		    			String roleList = new String();
			    		roleList = new MiscAccessManager().fetchRoleList();
				        JsonNode data = objectMapper.readTree(roleList);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", data);
				        exchange.getIn().setBody(finalresultNode);
		    		}
		    		catch(Exception e)
		    		{
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", "");
				        exchange.getIn().setBody(finalresultNode);
		    		}
	    	}
	    	else if(reqNode.has("action")&& reqNode.has("username")&& reqNode.has("agencyId")&& reqNode.has("roleId")
	    			&& reqNode.get("action").asText().equals("FETCH_CUST"))
	    	{  log.info("Inside FetchListProcessor FETCH_CUST ");
	    		log.info("Action :"+reqNode.get("action").asText());
	    		if(reqNode.get("action").asText().equals("FETCH_CUST"))
	    		{
		    		try{
		    			String custList = new String();
		    			custList = new MiscAccessManager().fetchCustList(reqNode.get("username").asText(),reqNode.get("roleId").asInt(),reqNode.get("agencyId").asText());
				        JsonNode data = objectMapper.readTree(custList);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        finalresultNode.put("message", "success");
				        finalresultNode.put("data", data);
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
	    	else
	    	{
		        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
		        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
		        finalresultNode.put("message", "Not Authorised");
		        finalresultNode.put("data", errorNode);
		        
		        exchange.getIn().setBody(finalresultNode);
	    	}
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at FetchListProcessor ", e);
	    }
	  }
}
