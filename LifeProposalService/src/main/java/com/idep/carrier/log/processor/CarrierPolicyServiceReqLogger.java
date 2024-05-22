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
public class CarrierPolicyServiceReqLogger implements Processor{
	Logger log = Logger.getLogger(CarrierPolicyServiceReqLogger.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierPolicyRequest = exchange.getIn().getBody(String.class);
		log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|SUCCESS|"+"carrier policy request service invoked : "+carrierPolicyRequest);
	}
}