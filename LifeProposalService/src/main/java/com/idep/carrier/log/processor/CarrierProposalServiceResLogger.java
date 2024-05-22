package com.idep.carrier.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * 
 * @author 	yogesh.shisode
 * @version	1.0
 * @since	23-MAR-2018
 *
 */
public class CarrierProposalServiceResLogger implements Processor {
	Logger log = Logger.getLogger(CarrierProposalServiceResLogger.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierProposalResponse = exchange.getIn().getBody(String.class);
		log.info("carrierProposalResponse : " + carrierProposalResponse);
		//log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.PROPOSALRES+"|SUCCESS|"+"proposal response calculated successfully"+carrierProposalResponse);
	}
}