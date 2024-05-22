package com.idep.proposal.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/*
* Class gets the referenceId from Client and send back as response
* @author  Shweta Joshi
*/
public class ProposalResProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalResProcessor.class.getName());
	JsonNode errorNode=null;
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator 
	{	
		try {
			String carrierResponse = exchange.getIn().getBody(String.class);
			JsonNode carrierResponseNode = objectMapper.readTree(carrierResponse);
			if(exchange.getProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID) != null){
				((ObjectNode)carrierResponseNode).put(ProposalConstants.ENCRYPT_PROPOSAL_ID, exchange.getProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID).toString());
			}
			else{
				log.info("NOTE - Encrypted ProposalId for Car not sent in response ");
			}
			/* set response in exchange body */
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1000);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, "success");
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, carrierResponseNode);
			exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
		}
		catch(Exception e)
		{
			this.log.error("ProposalResProcessor Exception : ",e);
			ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1002);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, "server error");
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, errorNode);
			exchange.getIn().setBody(obj);
		}
	}
}