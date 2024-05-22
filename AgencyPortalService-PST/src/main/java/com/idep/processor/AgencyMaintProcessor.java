package com.idep.processor;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bean.Agency;
import com.idep.bean.Branch;
import com.idep.model.AgencyAccessManager;
import com.idep.util.AgencyConstants;

public class AgencyMaintProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AgencyMaintProcessor.class.getName());
	
	public void process(Exchange exchange)
	  {
		JsonNode data;
		String status = new String();
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
	        if(action.equals("ADDBRANCH"))
	        {
	        	String branchDetails = null;
		    	log.info("Inside AgencyMaintProcessor -- action : "+action+" sessionId :"+sessionId);
		        branchDetails = objectMapper.writeValueAsString(reqNode);
		        log.info("Inside AgencyMaintProcessor -- branchDetails : "+branchDetails);
		        JsonNode branchdetails = objectMapper.readTree(branchDetails);
	        	try{
	        		if(action.equals("ADDBRANCH")){
	        			status = new AgencyAccessManager().addBranch(branchdetails,sessionId);
	    		        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	    		        if(AgencyConstants.BRANCH_ADD_SUCCESS==status){
					        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        	finalresultNode.put("message", "success");
					        }
					        else{
					        	finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
						        finalresultNode.put("message", "failure");
						        
					        }
				        finalresultNode.put("data", status);
	    		        exchange.getIn().setBody(finalresultNode);
	        		}
	        	}catch (Exception e){
	        		this.log.error("Exception at AgencyMaintProcessor while adding branch", e);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "failure");
			        finalresultNode.put("data", status);
			        exchange.getIn().setBody(finalresultNode);
	        	}
	        }
	        else if(action.equals("ADD") || action.equals("UPDATE"))
	        {
		    	String agencyDetails = null;
		    	String branchDetails = null;
		    	log.info("Inside AgencyMaintProcessor -- action : "+action+" sessionId :"+sessionId);
		        if(reqNode.has("agencyDetails")&& reqNode.has("branchDetails"))
		        {
		        	agencyDetails = objectMapper.writeValueAsString(reqNode.get("agencyDetails"));
		        	branchDetails = objectMapper.writeValueAsString(reqNode.get("branchDetails"));
		        	log.info("Inside AgencyMaintProcessor -- agencyDetails : "+agencyDetails);
		        	log.info("Inside AgencyMaintProcessor -- branchDetails : "+branchDetails);
		        }else{
		        	log.error("Agency details not found..!! ");
		        }
		        
	        	Agency agency = objectMapper.readValue(agencyDetails, Agency.class);
	        	log.info("Inside AgencyMaintProcessor -- agency object : "+objectMapper.writeValueAsString(agency));
	        	
	        	JsonNode branchdetails = objectMapper.readTree(branchDetails);
	        	
	        	ArrayList<Branch> branchList = new ArrayList<Branch>();
	        	for (JsonNode Jsonbranch : branchdetails) {
	        		Branch branch = objectMapper.readValue(Jsonbranch.toString(),Branch.class);
	        		branchList.add(branch);
	        		log.info("Inside AgencyMaintProcessor -- branch object : "+branch.toString());
				}
	        	String bDetails = objectMapper.writeValueAsString(branchList);
	        	log.info("Inside AgencyMaintProcessor -- branch list : "+bDetails);
	        	try{
	        		if(action.equals("ADD")){
	        			status = new AgencyAccessManager().addAgency(agency,branchList,sessionId);
	    		        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	    		        if(AgencyConstants.AGENCY_ADD_SUCCESS==status){
					        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        	finalresultNode.put("message", "success");
					        }
					        else{
					        	finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
						        finalresultNode.put("message", "failure");
						        
					        }
				        finalresultNode.put("data", status);
	    		        exchange.getIn().setBody(finalresultNode);
	        		}
	        		else if (action.equals("UPDATE"))
	        		{
	        			status = new AgencyAccessManager().updateAgency(agency,branchList,sessionId);
	    		        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	    		        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
	    		        finalresultNode.put("message", "success");
	    		        finalresultNode.put("data", status);
	    		        exchange.getIn().setBody(finalresultNode);
	        		}
	        	}catch (Exception e){
	        		this.log.error("Exception at AgencyMaintProcessor while adding or updating agency", e);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "failure");
			        finalresultNode.put("data", status);
			        exchange.getIn().setBody(finalresultNode);
	        	}
	        }
	        else if (action.equals("FETCH"))
	        {
	        	String agencyId =  new String();
	        	String agencyDetails = new String();
	        	if(reqNode.has("agencyId"))
		        {
	        		agencyId = reqNode.get("agencyId").asText();
		        }else{
		        	log.error("Agency details not found in request..!! ");
		        }        	    	
	        	try{
	        		agencyDetails = new AgencyAccessManager().fetchAgency(agencyId);
		        	data = objectMapper.readTree(agencyDetails);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
			        finalresultNode.put("message", "success");
			        finalresultNode.put("data", data);
			        exchange.getIn().setBody(finalresultNode);
	        	}catch (Exception e){
	        		this.log.error("Exception at AgencyMaintProcessor--addAgency", e);
			        
	        		data = objectMapper.readTree(agencyDetails);
			        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
			        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
			        finalresultNode.put("message", "failure");
			        finalresultNode.put("data", data);
			        exchange.getIn().setBody(finalresultNode);
	        	}
	        }
	        else if (action.equals("DELETE"))
	        {	
	        	
	        	
	        	if(reqNode.has("agencyId"))
		        {
	        		String agencyId =  new String();
	        		agencyId = reqNode.get("agencyId").asText();      	    	
		        	try{
		        		status = new AgencyAccessManager().deleteAgency(agencyId,sessionId);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        if(AgencyConstants.AGENCY_DELETE_FAILURE==status){
					        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
					        finalresultNode.put("message", "failure");
					        }
					        else{
					        	finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
					        	finalresultNode.put("message", "success");
					        }
				       
				        finalresultNode.put("data", status);
				        exchange.getIn().setBody(finalresultNode);
		        	}catch (Exception e){
		        		this.log.error("Exception at AgencyMaintProcessor--addAgency", e);
				        
		        		data = objectMapper.readTree(status);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "failure");
				        finalresultNode.put("data", data);
				        exchange.getIn().setBody(finalresultNode);
		        	}
		        }else if(reqNode.has("branchId")){
		        	String branchId =  new String();
	        		branchId = reqNode.get("branchId").asText();      	    	
		        	try{
		        		status = new AgencyAccessManager().deleteAgencyBranch(branchId,sessionId);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				       
				        if(AgencyConstants.BRANCH_DELETE_FAILURE==status){
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "failure");
				        }
				        else{
				        	finalresultNode.put("responseCode", AgencyConstants.SUCCESS_CODE);
				        	finalresultNode.put("message", "success");
				        }
				        
				       
				        finalresultNode.put("data", status);
				        exchange.getIn().setBody(finalresultNode);
		        	}catch (Exception e){
		        		this.log.error("Exception at AgencyMaintProcessor--addAgency", e);
				        
		        		data = objectMapper.readTree(status);
				        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
				        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
				        finalresultNode.put("message", "failure");
				        finalresultNode.put("data", data);
				        exchange.getIn().setBody(finalresultNode);
		        	}
		        	log.error("Branch details not found..!! ");
		        }  
	        }
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at AgencyMaintProcessor ", e);
	        ObjectNode finalresultNode = this.objectMapper.createObjectNode();
	        if(status.isEmpty()){status = "Could Not Add Agency. Please Try Again!";}
	        finalresultNode.put("responseCode", AgencyConstants.FAILURE_CODE);
	        finalresultNode.put("message", "failure");
	        finalresultNode.put("data",status);
	        exchange.getIn().setBody(finalresultNode);
	    }
	  }
}
