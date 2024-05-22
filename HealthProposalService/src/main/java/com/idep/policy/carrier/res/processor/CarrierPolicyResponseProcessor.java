package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CarrierPolicyResponseProcessor implements Processor {
	
	
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(CarrierPolicyResponseProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  
  public void process(Exchange exchange) throws Exception {
	  
    try
    {
    	 
        /**
         *  retrieve policy response from carrier
         */
    	String policyResponse = exchange.getIn().getBody(String.class);
    	JsonNode resNode = this.objectMapper.readTree(policyResponse);
     
    	/**
    	 * get policy service input request by accessing property
    	 */
    	JsonNode proposalReqNode  = objectMapper.readTree(exchange.getProperty(ProposalConstants.HLTHPOLICY_INPUT_REQ).toString());
    	((ObjectNode)proposalReqNode).put("carrierPolicyResponse",resNode);
    	
    	//((ObjectNode)proposalReqNode).put("documentType","carrierPolicyResponse");
      
      
    	// set configuration document id for mapper
    	exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYCONF_REQ + proposalReqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
    			"-" + proposalReqNode.get(ProposalConstants.PLAN_ID).intValue());
      
    	exchange.getIn().setBody(proposalReqNode);
      
    }
    catch (Exception e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|CarrierPolicyResponseProcessor|",e);
		throw new ExecutionTerminator();
    }
  }
}

