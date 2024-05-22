package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

public class FutureHealthProposerDataProcessor implements Processor
{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaHealthProposalRequestFormatter.class.getName());
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	
	 public void process(Exchange exchange) 
	 {
		 try
		 {
			 String input = (String)exchange.getIn().getBody(String.class);
		     JsonNode inputReq = objectMapper.readTree(input);
		     log.info("carrierResponseNode in Future"+inputReq);
		     JsonNode healthReqConfigNode = objectMapper.readTree(serverConfig.getDocBYId("HealthProposalRequest-"+inputReq.get("carrierId").asText()+"-"+inputReq.get("planId").asText()).content().toString());
		     log.info("FutureGenerali ConfigDoc: "+healthReqConfigNode);
			    /**
			     * set request configuration document id HealthProposalRequest
			     */
			 exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF,healthReqConfigNode);
		     exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, objectMapper.writeValueAsString(inputReq));	      
		     exchange.getIn().setBody(this.objectMapper.writeValueAsString(inputReq));
		 }
		 catch(Exception e)
		 {
			 log.error("Exception at CignaHealthProposalRequestFormatter : ", e); 
		 }
	 }
}

