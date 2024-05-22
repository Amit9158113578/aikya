package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class TravelPolicyResLogProcessor implements Processor {

	Logger log = Logger.getLogger(TravelPolicyResLogProcessor.class.getName());
	 ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {

		JsonNode policyReqNode  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		
		log.info(ProposalConstants.POLICY_RESPONSE+"|SUCCESS|"+"Policy calculated successfully :"+policyReqNode.findValue("policyNo").asText());
		
	}

}
