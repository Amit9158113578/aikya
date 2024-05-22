package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class HealthPolicyServiceReqLog implements Processor {

	 Logger log = Logger.getLogger(HealthPolicyServiceReqLog.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierProposalRequest = exchange.getIn().getBody(String.class);
		log.info("Health carrier Policy request service invoked :  "+carrierProposalRequest);
		//log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYSERVICE+"|INIT|"+"Health carrier Policy request service invoked : "+carrierProposalRequest);
	}

}
