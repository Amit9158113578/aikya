package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class KotakLifeProposalResponseValidator  implements Processor {

	Logger log = Logger.getLogger(KotakLifeProposalResponseValidator.class);
	ObjectMapper objectMapper = new ObjectMapper();
	ObjectNode obj = this.objectMapper.createObjectNode();
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			if(inputReqNode.get("CreateProposalResponse").get("ErrorMessage").asText().equals("null")){
			//	ObjectNode obj = this.objectMapper.createObjectNode();
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODESUCESS); 
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGSUCESS);
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode);		
				exchange.getIn().setBody(obj);
		
			}else{
				
				obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
				obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
				log.info("the error message is::"+inputReqNode.get("CreateProposalResponse").get("ErrorMessage").asText());
				obj.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("CreateProposalResponse").get("ErrorMessage").asText());
				log.info("the error obj set as:"+obj);
				exchange.getIn().setBody(obj);
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|"+"Kotak Life carrier proposal Response : "+inputReqNode.toString());
				throw new ExecutionTerminator(objectMapper.writeValueAsString(obj));
			}
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|FAIL|LifeProposalResponseValidator",e);
			throw new ExecutionTerminator(objectMapper.writeValueAsString(obj));			
		}
	}

}
