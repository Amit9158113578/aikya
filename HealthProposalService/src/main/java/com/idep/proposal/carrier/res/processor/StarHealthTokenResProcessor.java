package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.carrier.req.processor.StarHealthTokenRefProcessor;
import com.idep.proposal.util.ProposalConstants;

public class StarHealthTokenResProcessor implements Processor
{
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(StarHealthTokenRefProcessor.class.getName());
	  
	  public void process(Exchange exchange)  throws Exception {
		  
	    try
	    {
	    	String tokenGenRes = (String)exchange.getIn().getBody(String.class);
	        JsonNode tokenGenResNode = this.objectMapper.readTree(tokenGenRes);
	        
	    	JsonNode proposalResNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_PROP_RES).toString());
	    	((ObjectNode)proposalResNode).putAll((ObjectNode)tokenGenResNode);
	    	
	    	exchange.getIn().setBody(proposalResNode);
	    	
	    }
	    catch(Exception e)
	    {
	    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|StarHealthTokenResProcessor|",e);
			throw new ExecutionTerminator();
	    }
	}
}
