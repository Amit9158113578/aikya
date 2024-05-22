package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *
 */
public class HDFCHealthProposalResponseValidator implements Processor {

	Logger log = Logger.getLogger(HDFCHealthProposalResponseValidator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			if(inputReqNode.get("responseCode").asText().equals("P365RES100")){
				exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.TRUE);
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODESUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGSUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("data"));
				obj.put(ProposalConstants.PROPOSAL_ID, inputReqNode.get("data").get("proposalId"));
				obj.put(ProposalConstants.DOCUMENT_TYPE, inputReqNode.get("data").get("documentType"));
				exchange.getIn().setBody(obj);
				log.debug("HealthProposalResponseValidator  Success : "+inputReqNode.toString());
			}else{
				exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.FALSE);
				ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("WsResultSet").get("WsMessage").asText());
				exchange.getIn().setBody(obj);
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Health carrier proposal Response : "+inputReqNode.toString());
			}
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|HealthProposalResponseValidator",e);
			throw new ExecutionTerminator();			
		}
	}
}
