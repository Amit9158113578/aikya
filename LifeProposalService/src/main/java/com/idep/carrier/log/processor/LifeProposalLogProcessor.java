package com.idep.carrier.log.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author 	yogesh.shisode
 * @version	1.0
 * @since	23-MAR-2018
 *
 */
public class LifeProposalLogProcessor implements Processor {
	Logger log = Logger.getLogger(LifeProposalLogProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;

	public void process(Exchange exchange) throws IOException {
		JsonNode proposalReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		exchange.setProperty(ProposalConstants.LOG_REQ, "Life|"+proposalReqNode.findValue("carrierId")+"|PROPOSAL"+"|"+proposalReqNode.findValue("proposalId").asText()+"|");
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PROPOSALREQ+"|"+"proposal request received");
	}
}