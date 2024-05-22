package com.idep.policy.res.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class TravelPolicyResHandler implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(TravelPolicyResHandler.class.getName());
  
  public void process(Exchange exchange) throws Exception
  {
   
	  try {
			
			String carrierResponse = exchange.getIn().getBody(String.class);
			this.log.debug("Proposal carrierResponse :"+carrierResponse);
			JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
			
			 JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.TRVLPOLICY_INPUT_REQ).toString());
			 ((ObjectNode)requestDocNode).put(ProposalConstants.CARRIER_RESPONSE, carrierResNode);
		      // set response configuration document id
		    /*  exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF,ProposalConstants.POLICYCONF_RES+
		    		  requestDocNode.get(ProposalConstants.CARRIER_ID).intValue());
		    + "-" + requestDocNode.get(ProposalConstants.PLAN_ID).intValue()	
		     */
			 
			 exchange.getIn().setHeader("documentId", ProposalConstants.POLICYCONF_RES+requestDocNode.get(ProposalConstants.CARRIER_ID).intValue());
			 ((ObjectNode)requestDocNode).put("requestType","TravelPolicyRESCONF");			 
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
			
		}
		catch(Exception e)
		{
			this.log.error("ProposalResHandler Exception : ",e);
		}
  }
}
