package com.idep.policy.carrier.res.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ReligarePolicySOAPResponseFormatter implements Processor {
	
	  Logger log = Logger.getLogger(ReligarePolicySOAPResponseFormatter.class.getName());
	  SoapConnector soapService = new SoapConnector();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
		
		  String carrierResponse  = exchange.getIn().getBody(String.class);
		  String formattedSoapRes = soapService.retriveSoapResult(carrierResponse,"int-get-policy-status-iO");
		  exchange.getIn().setBody(formattedSoapRes);
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|ReligarePolicySOAPResponseFormatter|",e);
			throw new ExecutionTerminator();
		}
		  
	}
	

}
