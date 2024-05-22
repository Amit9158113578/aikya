package com.idep.policy.req.processor;

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

public class PolicyDocumentReqProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(PolicyDocumentReqProcessor.class.getName());
  CBService proposalService =  CBInstanceProvider.getPolicyTransInstance();
  CBService serviceConfig =  CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
	    try
	    {
	    	
	      String policyDocRequest = exchange.getIn().getBody(String.class);
	      JsonNode reqNode = this.objectMapper.readTree(policyDocRequest);
	      
	      /**
	       * get proposal document from transaction bucket
	       */
	      JsonNode proposalDocNode = objectMapper.readTree(proposalService.getDocBYId(reqNode.get("proposalId").asText()).content().toString());
	     
	      ((ObjectNode)reqNode).putAll((ObjectNode)proposalDocNode);
	      log.info("Proposal Id data for policyDocRequest : " +reqNode);
	      
	      /**
	       * set policy PDF request in exchange property
	       */
	      exchange.setProperty(ProposalConstants.TRAVPOLICYDOC_INPUT_REQ, reqNode);
	      ((ObjectNode)reqNode).put("requestType", "TravelPolicyDocumentRequest");
	      
	      /**
	       * set policy PDF configuration in exchange property
	       */
	      JsonNode policyConfigNode=null;
	      try{
	    	  policyConfigNode = objectMapper.readTree(serviceConfig.getDocBYId("TravelPolicyDocumentRequest-"+reqNode.get("carrierId").asInt()).content().toString());
	    	  if(policyConfigNode==null){
	    		  log.error("unable to read Policy Configuration document : "+"TravelPolicyDocumentRequest-"+reqNode.get("carrierId").asInt());
	    	  }
	      }catch(Exception e){
	    	 log.error("unable to download Policy Document : " ,e);
				throw new ExecutionTerminator();
	      }
	       exchange.setProperty(ProposalConstants.TRAVPOLICYDOC_CONFIG, policyConfigNode);
	      
	      exchange.getIn().setBody(reqNode);
	      
	    }
	    catch (Exception e)
	    {
	    	log.error("unable to download Policy Document : " ,e);
			throw new ExecutionTerminator();
	    }
  	}
}
