package com.idep.proposal.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class HealthProposalResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(HealthProposalResProcessor.class.getName());
	  
	public HealthProposalResProcessor()  {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		try
		  {
			  String carrierRequest = exchange.getIn().getBody(String.class);
			  JsonNode carrierReqNode = this.objectMapper.readTree(carrierRequest);
			  exchange.getIn().setBody(carrierReqNode.get(ProposalConstants.CARRIER_MAPPER_REQ));
			 
		  }
		  catch(Exception e)
		  {
			  log.info("Eception at HeathProposalResProcessor : "+e);
		  }
		
		
	}

}
