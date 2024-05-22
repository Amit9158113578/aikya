
package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TravelCarrierPolicyResponseProcessor implements Processor {	
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(TravelCarrierPolicyResponseProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  
  public void process(Exchange exchange) throws Exception {
	  
    try
    {
        /**
         *  retrieve policy response from carrier
         */
    	String policyResponse = exchange.getIn().getBody(String.class);
    	JsonNode resNode = this.objectMapper.readTree(policyResponse);
    	JsonNode TravelProposalIDNode;
    	log.debug("Travel Proposal ID  "+exchange.getProperty(ProposalConstants.TravelProposalID));
    	
    	String TravelProposalID = exchange.getProperty(ProposalConstants.TravelProposalID).toString();
    	if(TravelProposalID != null){
    		TravelProposalIDNode = this.objectMapper.readTree(TravelProposalID);
    	}else
    	{
    		 log.debug(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|unable to load Proposal Document :  "+ProposalConstants.TravelProposalID);
       	  throw new ExecutionTerminator();
    	}
    	/**
    	 * get policy service input request by accessing property
    	 */
    	JsonNode proposalReqNode  = objectMapper.readTree(exchange.getProperty(ProposalConstants.TRVLPOLICY_INPUT_REQ).toString());
    	((ObjectNode)proposalReqNode).put("carrierPolicyResponse",resNode);
    	((ObjectNode)proposalReqNode).put("TravelProposalIDResponse",TravelProposalIDNode);

    	// set configuration document id for mapper
    	/*exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYCONF_REQ + proposalReqNode.get(ProposalConstants.CARRIER_ID).intValue());
    	+ "-"+ proposalReqNode.get(ProposalConstants.PLAN_ID).intValue()
    	*/
    	
    	 exchange.getIn().setHeader("documentId", ProposalConstants.TravelPolicyREQCONF+"-"+proposalReqNode.get(ProposalConstants.CARRIER_ID).intValue());
    	((ObjectNode)proposalReqNode).put("requestType",ProposalConstants.TravelPolicyREQCONF); 

    	exchange.getIn().setBody(proposalReqNode);      
    }
    catch (Exception e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|TravelCarrierPolicyResponseProcessor|",e);
		throw new ExecutionTerminator();
    }
  }
}

