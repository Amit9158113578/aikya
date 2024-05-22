package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CignaSaveProposalRequestFormatter implements Processor {
	
	  Logger log = Logger.getLogger(CignaSaveProposalRequestFormatter.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service =  CBInstanceProvider.getPolicyTransInstance();
	  
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator 
	{
		try 
		{
			
			JsonNode requestNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
			String  carrierQuoteId = requestNode.get("coverageDetails").get("carrierQuoteId").asText();
						
			String proposalRequest = exchange.getIn().getBody(String.class);
			log.debug("proposalRequest in CignaSaveProposalRequestFormatter"+proposalRequest);
			JsonObject docObj = JsonObject.fromJson(proposalRequest);
			docObj.put("documentType", "CignaValidateRequest");
	       /**
	        * create Validate Proposal Input Request document
	        */
	        String doc_status = service.createDocument(carrierQuoteId, docObj);
	        log.debug("Health Validate Proposal Input Request document created : "+carrierQuoteId+" : "+doc_status);
	       			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaSaveProposalRequestFormatter|",e);
			throw new ExecutionTerminator();
		}		  
	}

}