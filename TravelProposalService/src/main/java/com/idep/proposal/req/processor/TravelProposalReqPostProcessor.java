package com.idep.proposal.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.proposal.util.ProposalConstants;


public class TravelProposalReqPostProcessor  implements Processor{

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(TravelProposalReqPostProcessor.class.getName());
	static JsonNode docConfigNode = objectMapper.createObjectNode();
	public void process(Exchange exchange) {

		try
		{
			String proposalRequest = (String)exchange.getIn().getBody(String.class);
			JsonNode reqNode = objectMapper.readTree(proposalRequest);
			
			if(reqNode.has("travellerDetails")){
				String isProposarSameAsInsured = "N";
				ArrayNode travellerDetailsNode = (ArrayNode)reqNode.get("travellerDetails");
				for(JsonNode traveller:travellerDetailsNode)
		    	  {
					if(traveller.has("relation")&& traveller.get("relation").asText().equalsIgnoreCase("Self"))
					{
						isProposarSameAsInsured = "Y";
						break;
					}		  
		    	  }
				((ObjectNode)reqNode.get("proposerDetails")).put("isProposarSameAsInsured", isProposarSameAsInsured);
		      }
			exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, objectMapper.writeValueAsString(reqNode));
			exchange.getIn().setBody(reqNode);
		}
		catch (Exception e)
		{
				log.error("Exception at TravelProposalReqPostProcessor : ", e);
		}			
	}		
}
