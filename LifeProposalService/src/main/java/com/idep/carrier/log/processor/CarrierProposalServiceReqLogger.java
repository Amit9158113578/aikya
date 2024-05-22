package com.idep.carrier.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author 	yogesh.shisode
 * @version	1.0
 * @since	23-MAR-2018
 *
 */
public class CarrierProposalServiceReqLogger implements Processor {
	Logger log = Logger.getLogger(CarrierProposalServiceReqLogger.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierProposalRequest = exchange.getIn().getBody(String.class);
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|SUCCESS|"+"carrier proposal request service invoked : "+carrierProposalRequest);
	}
}