package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class StarHealthPaymentResProcessor implements Processor{

	 ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(StarHealthPaymentResProcessor.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		
		try{
			
			
			JsonNode inputReNode  =objectMapper.readTree(exchange.getIn().getBody(String.class));
			
			
			if(inputReNode.has("status")){
				log.info("Star Health Payment Validate Service Reposne  : "+inputReNode);
			}else{
				((ObjectNode)inputReNode).put("status", "FAILURE");
			}
			
			
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1000);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, "success");
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReNode);
			exchange.getIn().setBody(obj);
			
		}catch(Exception e){
			log.error("Error occured at StarHealthPaymentResProcessor : ",e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1001);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, "failure");
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, "");
			exchange.getIn().setBody(obj);
			throw new ExecutionTerminator();
		}
	}
}
