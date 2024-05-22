package com.idep.policyrenewproposal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
/**
 * 
 * @author sandeep.jadhav
 * Set Header to load configuration document, required to create proposal document
 */
public class HealthProposalDBReqProcessor implements Processor
{
	Logger log = Logger.getLogger(HealthProposalDBReqProcessor.class.getName());
		  
	public void process(Exchange exchange) throws ExecutionTerminator {
			
		try
		{
		    /**
		     *  set header to save proposal document in database
		     */
		    exchange.getIn().setHeader("documentId", "Policies365-HealthProposalRequest");
		   
		    
		}
		catch(Exception e)
		{
			log.error("|ERROR|HealthProposalDBReqProcessor|",e);
			throw new ExecutionTerminator();
		}
		  
	}
}
