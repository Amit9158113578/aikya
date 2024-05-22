package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class StarHealthTokenRefProcessor implements Processor
{
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(StarHealthTokenRefProcessor.class.getName());
	  
	  public void process(Exchange exchange)  throws Exception {
		  
	    try
	    {

	    	String tokenGenReq = (String)exchange.getIn().getBody(String.class);
	        JsonNode tokenGenReqNode = this.objectMapper.readTree(tokenGenReq);
	        
	    	JsonNode proposalResNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_PROP_RES).toString());
	    	String URL = exchange.getIn().getHeader("requestURL").toString();
	    	

	    	/**
	    	 * set reference token in URL
	    	 */
	    	
	    	URL = URL.replaceAll("%REFERENCEID%", proposalResNode.get("referenceId").asText());
	    	log.debug("modified URL for teken : "+URL);
	    	log.debug("modified request for teken : "+tokenGenReqNode);
	    	
	    	exchange.getIn().setHeader("requestURL", URL);
	    	exchange.getIn().setBody(tokenGenReqNode);
	    	
	    }
	    catch(Exception e)
	    {
	    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|StarHealthTokenRefProcessor|",e);
			throw new ExecutionTerminator();
	    }
	}
}
