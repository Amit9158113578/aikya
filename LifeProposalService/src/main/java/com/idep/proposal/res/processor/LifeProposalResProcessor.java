package com.idep.proposal.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author  yogesh.shisode
 * @version 1.0
 * @since   23-MAR-2018
 * @Description: Class gets the referenceId from Client and send back as response
 */
public class LifeProposalResProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeProposalResProcessor.class.getName());
	JsonNode errorNode=null;

	@Override
	public void process(Exchange exchange) throws Exception
	
	{
	    try
	    {
	    	
	      String policyResponse = (String)exchange.getIn().getBody(String.class);
	      
	      
	      JsonNode policyResNode = this.objectMapper.readTree(policyResponse);
	      
	      
	      ObjectNode obj = this.objectMapper.createObjectNode();
	      obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODESUCESS);
	      obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESECODESUCESS);
	      obj.put(ProposalConstants.PROPOSAL_RES_DATA, policyResNode);
	      exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
	    }
	    catch (Exception e)
	    {
	      this.log.error("LifePolicyResProcessor Exception : ", e);
	      ObjectNode obj = this.objectMapper.createObjectNode();
	      obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODE_ERROR);
	      obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
	      obj.put(ProposalConstants.PROPOSAL_RES_DATA, this.errorNode);
	      exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
	    }
	  }
	}