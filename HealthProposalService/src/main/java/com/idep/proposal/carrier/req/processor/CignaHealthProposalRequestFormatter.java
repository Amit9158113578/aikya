package com.idep.proposal.carrier.req.processor;

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

public class CignaHealthProposalRequestFormatter implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaHealthProposalRequestFormatter.class.getName());
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	
	 public void process(Exchange exchange) throws ExecutionTerminator 
	 {
		 try
		 {
			 String carrierResponse = (String)exchange.getIn().getBody(String.class);
		     JsonNode carrierResponseNode = objectMapper.readTree(carrierResponse);
		     
		     JsonNode requestNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
		      /**
		       * put generated ProposalNum into proposalRequest  
		       */	      
		      ((ObjectNode)requestNode).put("carrierProposalNum",carrierResponseNode.get("quoteResult").get("listGeneratePolicyNumTO").get("proposalNum").textValue());
		     log.debug("carrierProposalNum: "+carrierResponseNode.get("quoteResult").get("listGeneratePolicyNumTO").get("proposalNum").textValue());
	 	     
	          exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, objectMapper.writeValueAsString(requestNode));
	          /**
		       * Read Product ,Rider details from Product Data and append it into requestNode
		       * 
		       */
		      log.debug("HealthPlanDocId"+"HealthPlan-"+requestNode.get(ProposalConstants.CARRIER_ID).intValue()+"-"+requestNode.get(ProposalConstants.PLAN_ID).intValue());
			  JsonNode HealthCarrierReqNode = objectMapper.readTree(productService.getDocBYId("HealthPlan-"+requestNode.get(ProposalConstants.CARRIER_ID).intValue()+"-"+requestNode.get(ProposalConstants.PLAN_ID).intValue()).content().toString());
			  //JsonNode HealthCarrierReqNode = objectMapper.readTree(productService.getDocBYId("HealthPlan-35-21").content().toString());
	          ((ObjectNode)requestNode).put("HealthCarrierDetails",HealthCarrierReqNode);
	         
			  exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestNode));
		 }
		 catch(Exception e)
		 {
			 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaHealthProposalRequestFormatter|",e);
				throw new ExecutionTerminator();
		 }
	 }
}
