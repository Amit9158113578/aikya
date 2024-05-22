package com.idep.proposal.dbstore.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TravelProposalDBReqProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelProposalDBReqProcessor.class.getName());
	CBService service = null;
	JsonNode serviceConfigNode = null;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try{
			String proposalRequest = exchange.getIn().getBody(String.class);
		    JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
		    log.info("proposalRequest in TravelProposalDBReqProcessor: "+reqNode);
		    // set header to save proposal document in database
		    exchange.getIn().setHeader("documentId", "Policies365-TravelProposalRequest");
		    exchange.getIn().setBody(reqNode);
		    
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"TravelProposalDBReqProcessor"+"|ERROR|"+"car proposal db request failed :",e);
			throw new ExecutionTerminator();
		}
	}

}
