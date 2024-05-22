package com.idep.proposal.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ProposalResHandler implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalResHandler.class.getName());
	
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		
		JsonNode requestDocNode =null;
		
		try {
			
			String carrierResponse = exchange.getIn().getBody(String.class);
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			
			requestDocNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
			if(requestDocNode.has("policyId")){
			((ObjectNode)carrierResNode).put("policyId", requestDocNode.get("policyId").textValue());
			}
			((ObjectNode)requestDocNode).put(ProposalConstants.CARRIER_RESPONSE, carrierResNode);
			
		    // set response configuration document id
		    
			exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF,ProposalConstants.PROPOSALCONF_RES+
		    		  requestDocNode.get(ProposalConstants.CARRIER_ID).intValue()+ "-" +
		    		  requestDocNode.get(ProposalConstants.PLAN_ID).intValue());
		    
		    exchange.getIn().setBody(requestDocNode);
		    log.info("ProposalResHandler requestDocNode : "+requestDocNode);
		  
		}
		
		catch(Exception e)
		{
			this.log.error("ProposalResHandler Exception : ",e);
			throw new ExecutionTerminator();
		}
		 
	}

}
