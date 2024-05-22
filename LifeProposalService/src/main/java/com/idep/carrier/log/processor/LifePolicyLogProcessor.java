package com.idep.carrier.log.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author 	yogesh.shisode
 * @version	1.0
 * @since	23-MAR-2018
 *
 */
public class LifePolicyLogProcessor implements Processor {
	Logger log = Logger.getLogger(LifePolicyLogProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;

	public void process(Exchange exchange) throws IOException {
		try{
			JsonNode policyReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
			exchange.setProperty(ProposalConstants.LOG_REQ, "Car|"+policyReqNode.findValue("carrierId")+"|POLICY"+"|"+policyReqNode.findValue("proposalId").asText()+ "|");
			log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|"+"policy request received policy No :"+policyReqNode.findValue("policyNo"));
		}catch(Exception e){
			log.error("Not able to set LifePolicyLogProcessor property : ", e);
		}
	}
}