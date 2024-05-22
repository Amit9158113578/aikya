package com.idep.policyrenewproposal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;

public class BikeProposalDBReqProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarProposalDBReqProcessor.class.getName());
	CBService service = null;
	JsonNode serviceConfigNode = null;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try
		{
			String proposalRequest = exchange.getIn().getBody(String.class);
		    JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
		    // set header to save proposal document in database
		    exchange.getIn().setHeader("documentId", "Policies365-BikeProposalRequest");
		    exchange.getIn().setBody(reqNode);
		    
		}
		catch(Exception e)
		{
			throw new ExecutionTerminator();
		}
	}

}
