package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class HealthPolicyDocDownloadLogReq implements Processor {
	 Logger log = Logger.getLogger(HealthPolicyDocDownloadLogReq.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		JsonNode policyReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		 exchange.setProperty(ProposalConstants.LOG_REQ, "Health|"+policyReqNode.findValue("carrierId")+"|POLICY"+"|"+policyReqNode.findValue(ProposalConstants.PROPOSAL_ID).asText()+ "|");
		 log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCREQ+"|"+"policy Document request received policy No :"+policyReqNode.findValue("policyNo"));
	}
}
