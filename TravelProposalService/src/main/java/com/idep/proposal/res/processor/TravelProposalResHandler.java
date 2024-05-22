package com.idep.proposal.res.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TravelProposalResHandler implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelProposalResHandler.class.getName());
	
	public void process(Exchange exchange) throws ExecutionTerminator 
	{
		JsonNode requestDocNode =null;
		try {
			
			String carrierResponse = exchange.getIn().getBody(String.class);
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			requestDocNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
			((ObjectNode)requestDocNode).put(ProposalConstants.CARRIER_RESPONSE, carrierResNode);
		    // set response configuration document id
			exchange.getIn().setHeader("documentId",ProposalConstants.PROPOSAL_RESCONF+
		    		  requestDocNode.get(ProposalConstants.CARRIER_ID).intValue()+ "-" +
		    		  requestDocNode.get(ProposalConstants.PLAN_ID).intValue());
			exchange.getIn().setBody(requestDocNode);
		    log.debug("TravelProposalResHandler requestDocNode : "+requestDocNode);
		}
		catch(Exception e)
		{
			log.error("TravelProposalResHandler Exception : ",e);
			throw new ExecutionTerminator();
		}
		 
	}

}
