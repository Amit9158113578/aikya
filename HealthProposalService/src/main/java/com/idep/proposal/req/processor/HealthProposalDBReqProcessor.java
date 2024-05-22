package com.idep.proposal.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;
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
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|HealthProposalDBReqProcessor|",e);
			throw new ExecutionTerminator();
		}
		  
	}
}
