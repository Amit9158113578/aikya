package com.idep.proposal.impl.service;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.res.processor.ProposalResProcessor;
/*
* @author  kuldeep patil
* @version 1.0
* @since   26-APRIL-2018
*/
public class TravelProposalServiceImpl {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelProposalServiceImpl.class.getName());
	
	public void submitTravelProposal(String proposal) throws ExecutionTerminator
	{
		JsonNode reqNode =null;
		
		try {
				reqNode = this.objectMapper.readTree(proposal);
				objectMapper.writeValueAsString(reqNode);
		
			}
		catch(Exception e)
			{
				throw new ExecutionTerminator();
			}
	}
	
	public String sendMessage(String proposal)
	{
		return proposal;
	}

}
