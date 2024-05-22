package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.idep.proposal.util.ProposalConstants;

public class TravelProposalServiceResLogProcessor implements Processor {
	Logger log = Logger.getLogger(TravelProposalServiceResLogProcessor.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierProposalResponse = exchange.getIn().getBody(String.class);
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PROPOSALRES+"|SUCCESS|"+"proposal response calculated successfully"+carrierProposalResponse);
	}

}
