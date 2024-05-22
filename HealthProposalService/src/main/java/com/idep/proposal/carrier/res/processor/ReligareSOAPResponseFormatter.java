package com.idep.proposal.carrier.res.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;

public class ReligareSOAPResponseFormatter implements Processor {
	
	  Logger log = Logger.getLogger(ReligareSOAPResponseFormatter.class.getName());
	  SoapConnector soapService = new SoapConnector();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
		
	      log.info("ReligareSOAPResponseFormatter invoked");		
		  String carrierResponse  = exchange.getIn().getBody(String.class);
		  String formattedSoapRes = soapService.retriveSoapResult(carrierResponse,"policy");
		  log.info("formattedSoapRes : "+formattedSoapRes);
		  exchange.getIn().setBody(formattedSoapRes);
			
		}
		catch(Exception e)
		{
			log.error("Exception at SOAPRequestFormatter : ");
			throw new ExecutionTerminator();
		}
		  
	}
	

}
