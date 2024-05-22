package com.idep.proposal.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.util.ProposalConstants;

public class ProposalServiceResponse {
	
	
	 public static String createResponse(int responseCode, String message, JsonNode node)
	  {
	    JsonNodeFactory factory = new JsonNodeFactory(true);
	    ObjectNode responseNode = new ObjectNode(factory);
	    responseNode.put(ProposalConstants.PROPOSAL_RES_CODE, responseCode);
	    responseNode.put(ProposalConstants.PROPOSAL_RES_MSG, message);
	    responseNode.put(ProposalConstants.PROPOSAL_RES_DATA, node);
	    
	    return responseNode.toString();
	  }

}
