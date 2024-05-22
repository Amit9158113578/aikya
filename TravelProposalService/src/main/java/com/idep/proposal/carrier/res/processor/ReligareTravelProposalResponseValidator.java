package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ReligareTravelProposalResponseValidator implements Processor {

	Logger log = Logger.getLogger(ReligareTravelProposalResponseValidator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			log.info( "TravelProposalResponseValidator Status : " +inputReqNode.get("xmlRes"));
			if(inputReqNode.get("xmlRes").has("error-lists")){
				
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("xmlRes").get("error-lists").get("err-description").asText());
				exchange.getIn().setBody(obj);
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Travel carrier proposal Response : "+inputReqNode.toString());
				throw new ExecutionTerminator();
			}else{
				exchange.getIn().setBody(inputReqNode);
				log.info("TravelProposalResponseValidator  Success : "+inputReqNode.toString());
			}
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|ReligareTravelProposalResponseValidator",e);
			throw new ExecutionTerminator();			
		}
	}
}
