package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.req.processor.HealthGeneratePolicyNumberProcessor;
import com.idep.proposal.util.ProposalConstants;

public class NIAPreHealthRequestProposalProcessor implements Processor {

	Logger log = Logger.getLogger(NIAPreHealthRequestProposalProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	
	public void process(Exchange exchange) throws ExecutionTerminator {		
		try
		{
		    /**
		     *  set header to save proposal document in database
		     */
			String proposalRequest = (String)exchange.getIn().getBody(String.class);
		    JsonNode reqNode = objectMapper.readTree(proposalRequest);
		    exchange.getIn().setHeader("documentId", "PreHealthProposalRequest-"+reqNode.get(ProposalConstants.CARRIER_ID).intValue());
		    /**
		     * get PreHealthProposalRequest configuration document and save it in exchange property.
		     * this document will be used while forming carrier request and response
		     */
		    log.info("PreHealthProposalRequest-"+reqNode.get(ProposalConstants.CARRIER_ID).intValue());
		    JsonNode healthPolicyNumberConfigNode = objectMapper.readTree(serverConfig.getDocBYId("PreHealthProposalRequest-"+reqNode.get(ProposalConstants.CARRIER_ID).intValue()).content().toString());
			exchange.setProperty("SoapReqConfDocument",healthPolicyNumberConfigNode);
			exchange.getIn().setBody(reqNode);
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|HealthGeneratePolicyNumberProcessor|",e);
			throw new ExecutionTerminator();
		}
		  
	}
}
