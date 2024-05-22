package com.idep.proposal.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class TravelProposalResProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(TravelProposalResProcessor.class.getName());
	  
	public TravelProposalResProcessor() 
	{
		// TODO Auto-generated constructor stub
	}
	@Override
	public void process(Exchange exchange) throws Exception {
		try
		  {
			  String carrierRequest = exchange.getIn().getBody(String.class);
			  JsonNode carrierReqNode = this.objectMapper.readTree(carrierRequest);
			  //exchange.getIn().setBody(carrierReqNode.get(ProposalConstants.CARRIER_MAPPER_REQ));
			  log.debug("ProposalRequest node in TravelProposalResProcessor:"+carrierReqNode);
			  exchange.getIn().setBody(objectMapper.writeValueAsString(carrierReqNode));
		  }
		  catch(Exception e)
		  {
			  log.error("Exception at TravelProposalResProcessor : ",e);
		  }
	}
}
