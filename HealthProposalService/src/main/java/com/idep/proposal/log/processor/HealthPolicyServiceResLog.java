package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class HealthPolicyServiceResLog implements Processor {

	 Logger log = Logger.getLogger(HealthPolicyServiceResLog.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierPolicyRes = exchange.getIn().getBody(String.class);
		log.info(ProposalConstants.POLICYSERVICE+"|SUCCESS|"+"Health carrier Policy request service invoked : "+carrierPolicyRes);
		//log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYSERVICE+"|SUCCESS|"+"Health carrier Policy request service invoked : "+carrierPolicyRes);
	}

}
