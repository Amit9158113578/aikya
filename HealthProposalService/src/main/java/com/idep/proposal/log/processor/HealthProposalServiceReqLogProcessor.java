package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.idep.proposal.util.ProposalConstants;

public class HealthProposalServiceReqLogProcessor implements Processor {
	Logger log = Logger.getLogger(HealthProposalServiceReqLogProcessor.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierProposalRequest = exchange.getIn().getBody(String.class);
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|SUCCESS|"+"Health carrier proposal request service invoked : "+carrierProposalRequest);
	}

}
