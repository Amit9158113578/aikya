package com.idep.policy.pdfreq.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.idep.api.impl.SoapConnector;

public class SOAPResultProcessor implements Processor {
	
	Logger log = Logger.getLogger(SOAPResultProcessor.class.getName());
	SoapConnector  soapService = new SoapConnector();

	@Override
	public void process(Exchange exchange) throws Exception {

		try
		{
			String pdfResponse  = exchange.getIn().getBody(String.class);
			String pdfSoapResponse = soapService.retriveSoapResult(pdfResponse, "GetSignPolicyPDFResult");
			log.debug("pdfSoapResponse : "+pdfSoapResponse);
			String formattedResponse = pdfSoapResponse.replaceAll("i:nil=\"true\"", "");
			exchange.getIn().setBody(formattedResponse);
			
		}
		
		catch(Exception e)
		{
			log.error("Policy PDF digital sign process could not be completed ",e);
		}
	}

}
