package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.carrier.req.processor.ABHIProposalReqStoreProcessor;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *
 */
public class ABHIProposalResValidator implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIProposalResValidator.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
		String input = exchange.getIn().getBody(String.class);
		JsonNode inputReq = objectMapper.readTree(input);
		JsonNode receiptCreationNode = inputReq.get("ReceiptCreationResponse");
		if(receiptCreationNode.has("errorNumber")){
			if(!receiptCreationNode.get("errorNumber").asText().trim().equalsIgnoreCase("0")){
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|"+receiptCreationNode);
				throw new ExecutionTerminator();
			}
		}
		JsonNode policyCreationNode = inputReq.get("PolCreationRespons");
		if(policyCreationNode.has("policyNumber") && policyCreationNode.get("policyNumber").asText().equalsIgnoreCase("00") && !policyCreationNode.get("stpflag").asText().equalsIgnoreCase("STP") ){
			
			((ObjectNode)policyCreationNode).put("policyNumber", policyCreationNode.get("proposalNumber"));
			((ObjectNode)policyCreationNode).put("proposalStatus", "NSTP");
			((ObjectNode)inputReq).put("PolCreationRespons", policyCreationNode);
			log.debug("Added Proposal STatus AS NSTP inn Aditya Birla Proposal response : "+inputReq);
		}else if(!policyCreationNode.get("stpflag").asText().equalsIgnoreCase("STP")){
		
			if(policyCreationNode.has("proposalNumber")){
				((ObjectNode)policyCreationNode).put("policyNumber", policyCreationNode.get("proposalNumber"));
				((ObjectNode)policyCreationNode).put("proposalStatus", "NSTP");
				((ObjectNode)inputReq).put("PolCreationRespons", policyCreationNode);
				log.debug("Added Proposal STatus AS NSTP inn Aditya Birla Proposal response : "+inputReq);
			}else if(policyCreationNode.has("carrierProposalNo")){
					((ObjectNode)policyCreationNode).put("policyNumber", policyCreationNode.get("carrierProposalNo"));
					((ObjectNode)policyCreationNode).put("proposalStatus", "NSTP");
					((ObjectNode)inputReq).put("PolCreationRespons", policyCreationNode);
					log.debug("Added Proposal STatus AS NSTP inn Aditya Birla Proposal response : "+inputReq);
					}
			}
		exchange.getIn().setBody(inputReq);
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ABHIProposalResValidator|",e);
			throw new ExecutionTerminator();
		}
	}

}
