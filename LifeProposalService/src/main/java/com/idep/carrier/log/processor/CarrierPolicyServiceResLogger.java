package com.idep.carrier.log.processor;

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
public class CarrierPolicyServiceResLogger implements Processor {

	Logger log = Logger.getLogger(CarrierPolicyServiceResLogger.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	public void process(Exchange exchange) throws Exception {
		JsonNode policyReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICY_RESPONSE+"|SUCCESS|"+"Policy calculated successfully :"+policyReqNode.findValue("policyNo").asText());
	}
}
