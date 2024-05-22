package com.idep.proposal.req.transformer;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;


import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/**
 * This processor transforms the address in a proper format by removing unwanted
 * characters in ui request.
 * */


public class KotakLifeRequestTransformer implements Processor {

	Logger log = Logger.getLogger(KotakLifeRequestTransformer.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
			String proposalRequest = exchange.getIn().getBody(String.class);
			JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
			String modifiedRequest=null;
				
			//Proposer communication Address
			
			if (reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS).has("address"))
			{	 
			String proposerAddr = reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS).get("address").asText();			
			 modifiedRequest = proposerAddr.replaceAll("( ,)|(, )|( ,)|(,$)", " ");
			modifiedRequest = modifiedRequest.replaceAll("( ,)|(, )|( ,)|(,$)", " ");
			((ObjectNode) reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS)).put(("address"),modifiedRequest);
			}
			if (reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS).has("address"))
			{	    
				String proposerAddr1 = reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS).get("address").asText();			
				String modifiedRequest1 = proposerAddr1.replaceAll("( ,)|(, )|( ,)|(,$)", " ");
				modifiedRequest1 = modifiedRequest1.replaceAll("( ,)|(, )|( ,)|(,$)", " ");
				((ObjectNode) reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS)).put(("address"),modifiedRequest1);			
			}
			
			//city
			if (reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS).has("city"))
			{
			String propserCity = reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS).get("city").asText();
			String modifiedRequest3 = propserCity.toString();
			modifiedRequest = modifiedRequest3.toLowerCase();
			((ObjectNode) reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS)).put(("city"),modifiedRequest);
			}
			
			if (reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS).has("city"))
			{
				String proposerAddr1 = reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS).get("city").asText();			
				String modifiedRequest1 = proposerAddr1.replaceAll("( ,)|(, )|( ,)|(,$)", " ");
				modifiedRequest1 = modifiedRequest1.replaceAll("( ,)|(, )|( ,)|(,$)", " ");
				((ObjectNode) reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS)).put(("city"),modifiedRequest1);    
			}
			
			//state
			if (reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS).has("state"))
			{
			String propserState = reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS).get("state").asText();	
			String modifiedRequest5 = propserState.toString();
			modifiedRequest = modifiedRequest5.toLowerCase();
			((ObjectNode) reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.COMMUNICATION_ADDRESS)).put(("state"),modifiedRequest);
			}
			
			//perm state
			if (reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS).has("state"))
			{
			String permState = reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS).get("state").asText();	
			String modifiedRequest4 = permState.toString();
			modifiedRequest = modifiedRequest4.toLowerCase();
			((ObjectNode) reqNode.get(ProposalConstants.ADDRESS_DETAILS).get(ProposalConstants.PERMANENT_ADDRESS)).put(("state"),modifiedRequest);
			}			
			exchange.getIn().setBody(reqNode);
			
			
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.KOTAKLIFEREQTRANSFORMER+"|ERROR|"+" Exception at KotakLifeRequestTransformer :",e);
			new ExecutionTerminator();
		}

	}

}