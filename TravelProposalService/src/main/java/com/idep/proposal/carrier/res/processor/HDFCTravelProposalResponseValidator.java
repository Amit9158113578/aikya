package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class HDFCTravelProposalResponseValidator implements Processor {

	Logger log = Logger.getLogger(HDFCTravelProposalResponseValidator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			log.info( "TravelProposalResponseValidator Status : " +inputReqNode.get("WsResultSet").get("WsStatus").asText());
			if(inputReqNode.get("WsResultSet").get("WsStatus").asText().equals("0")){
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODESUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGSUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode);
				exchange.getIn().setBody(obj);
				log.info("TravelProposalResponseValidator  Success : "+inputReqNode.toString());
			}else{
				log.info("In case of failure response : "+inputReqNode.get("WsResultSet").get("WsMessage").asText());
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("WsResultSet").get("WsMessage").asText());
				exchange.getIn().setBody(obj);
				log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Travel carrier proposal Response : "+inputReqNode.toString());
				throw new ExecutionTerminator();
			}
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|TravelProposalResponseValidator",e);
			throw new ExecutionTerminator();			
		}
	}
}

