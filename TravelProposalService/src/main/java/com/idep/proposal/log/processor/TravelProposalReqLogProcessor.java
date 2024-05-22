package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class TravelProposalReqLogProcessor implements Processor {
	 Logger log = Logger.getLogger(TravelProposalReqLogProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		 JsonNode proposalReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		 exchange.setProperty(ProposalConstants.LOG_REQ, "Travel|"+proposalReqNode.findValue("carrierId")+"|PROPOSAL"+"|"+proposalReqNode.findValue(ProposalConstants.PROPOSAL_ID).asText()+"|");
		 log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PROPOSALREQ+"|"+"proposal request received");
	}
}