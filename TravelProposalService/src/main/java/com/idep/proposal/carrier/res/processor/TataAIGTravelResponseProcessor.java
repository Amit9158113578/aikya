package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TataAIGTravelResponseProcessor implements Processor {

	Logger log = Logger.getLogger(TataAIGTravelResponseProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			log.info( "TravelProposalResponseValidator Status : " +inputReqNode.get("TINS_XML_DATA").get("Segment").get("ErrorCode").asText());
			if(inputReqNode.get("TINS_XML_DATA").get("Segment").get("ErrorCode").asText().equals("0")){
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODESUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGSUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode);
				exchange.getIn().setBody(obj);
				log.info("TravelProposalResponseValidator  Success : "+inputReqNode.toString());
			}else{
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1010);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEFAIL);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("TINS_XML_DATA").get("Segment").get("ErrorMessage").asText());
				exchange.getIn().setBody(obj);
				log.info("Travel carrier proposal Response : "+inputReqNode.toString());
				throw new ExecutionTerminator();
			}
		}catch(Exception e){
			log.error("|FAIL|TravelProposalResponseValidator",e);
			throw new ExecutionTerminator();			
		}
	}
}

