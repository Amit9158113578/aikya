package com.idep.proposal.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/*
* Class gets the referenceId from Client and send back as response
* @author  Sandeep Jadhav
* @version 1.0
* @since   25-OCT-2016
*/
public class ProposalResProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalResProcessor.class.getName());
	JsonNode errorNode=null;

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		
		try {
			
			String carrierResponse = exchange.getIn().getBody(String.class);
			JsonNode carrierResponseNode = objectMapper.readTree(carrierResponse);
			this.log.info("Health Proposal carrierResponse :"+carrierResponse);
		
			/* set response in exchange body */
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1000);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, "success");
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, carrierResponseNode);
			exchange.getIn().setBody(obj);
			
		}
		catch(Exception e)
		{
			this.log.error("ProposalResProcessor Exception : ",e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1002);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, "server error");
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, errorNode);
			exchange.getIn().setBody(obj);
		}
		 
	}

}
