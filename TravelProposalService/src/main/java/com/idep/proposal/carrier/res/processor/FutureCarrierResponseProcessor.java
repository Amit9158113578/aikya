package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class FutureCarrierResponseProcessor implements Processor {	
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(FutureCarrierResponseProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  
  public void process(Exchange exchange) throws Exception {
	  
    try{
		
    	String policyResponse = exchange.getIn().getBody(String.class);
    	JsonNode resNode = this.objectMapper.readTree(policyResponse);
    	log.info("policyResponse future :"+policyResponse);
    	if(resNode.get("Root").get("Policy").has("Status") && resNode.get("Root").get("Policy").get("Status").asText().equalsIgnoreCase("Successful"))
    	{
    		ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODESUCESS);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGSUCESS);
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, policyResponse);
			exchange.getIn().setBody(obj);
    	}
    	else
    	{
    		ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, resNode.get("Root").get("Policy").get("ErrorMessage").asText());
			exchange.getIn().setBody(obj);
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Future Travel carrier proposal Response : "+policyResponse.toString());
			throw new ExecutionTerminator();
		}	
	}
    catch(Exception e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|FutureCarrierResponseProcessor|",e);
		throw new ExecutionTerminator();		
	}
    
  }
}

