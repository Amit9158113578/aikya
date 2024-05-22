package com.idep.policyrenewproposal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;
import com.idep.policyrenew.util.PolicyRenewConstatnt;

public class CarProposalResProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarProposalResProcessor.class.getName());
	JsonNode errorNode=null;
	@Override
	public void process(Exchange exchange) throws Exception {
		try {
			
			String proposalResponse = exchange.getIn().getBody(String.class);
			JsonNode proposalResNode = this.objectMapper.readTree(proposalResponse);
			/* set response in exchange body */
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(PolicyRenewConstatnt.PROPOSAL_RES_CODE, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(PolicyRenewConstatnt.SUCC_CONFIG_CODE).asInt());
			obj.put(PolicyRenewConstatnt.PROPOSAL_RES_MSG, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(PolicyRenewConstatnt.SUCC_CONFIG_MSG).asText());
			obj.put(PolicyRenewConstatnt.PROPOSAL_RES_DATA, proposalResNode);
			
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
			//exchange.getUnitOfWork().done(exchange);
			}
		catch(Exception e)
		{
			  log.error("|ERROR|"+"car proposal response processor failed:",e);
			  ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(PolicyRenewConstatnt.PROPOSAL_RES_CODE, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue());
		      objectNode.put(PolicyRenewConstatnt.PROPOSAL_RES_MSG, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue());
		      objectNode.put(PolicyRenewConstatnt.PROPOSAL_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
		      //exchange.getUnitOfWork().done(exchange);
		      throw new ExecutionTerminator();
		}
		 
	}

}
