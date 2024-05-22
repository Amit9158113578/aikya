package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;


public class TataAIGSoapResFormatter implements Processor 
{
	
	Logger log = Logger.getLogger(TataAIGSoapResFormatter.class.getName());
	public void process(Exchange exchange) throws Exception 
	{	
		
		String formatedReposne=null;
		String tagName="TINS_XML_DATA";
		try{
		
			String proposalResponse  = exchange.getIn().getBody(String.class);
			/*calling for removing header from Carrier response*/
			SoapConnector extService = new SoapConnector();
			String formattedSoapRes = extService.getSoapResult(proposalResponse,tagName);
			 log.info("TataAIG TataAIGSoapResFormatter modifiedRequest "+formattedSoapRes);

			exchange.getIn().setBody("<TINS_XML_DATA>"+formattedSoapRes+"</TINS_XML_DATA>");
			

		}catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ "|ERROR|"+" Exception at TataAIGResFormatter for response :"+formatedReposne,e);
			throw new ExecutionTerminator();
		}
	}

}
