package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.res.processor.ProposalResHandler;

public class FutureGeneraliResponseProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalResHandler.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		JsonNode requestDocNode =null;
		
		try {
			
			String carrierResponse = exchange.getIn().getBody(String.class);
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
						
			exchange.getIn().setBody(objectMapper.writeValueAsString(carrierResNode));
		}catch(Exception e){
			
			throw new ExecutionTerminator();
			
		}
		
	}

}
