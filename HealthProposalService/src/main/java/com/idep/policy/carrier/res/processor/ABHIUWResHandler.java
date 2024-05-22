package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;



public class ABHIUWResHandler implements Processor {
	Logger log = Logger.getLogger(HDFCUWResponseProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String input = exchange.getIn().getBody(String.class);
			log.info("ABHIUWResponseProcessor Policy Res : "+input);
			JsonNode policyRes = objectMapper.readTree(input);
			
			JsonObject proposalDoc = transService.getDocBYId(policyRes.get("carrierRequestForm").get("proposalId").asText()).content();
			
			JsonNode healthProposalRes = objectMapper.readTree(proposalDoc.get("healthPolicyResponse").toString()); 
			
			if(policyRes.get("carrierRequestForm").get("policyNo").asText().equalsIgnoreCase("00")){
				((ObjectNode)policyRes.get("carrierRequestForm")).put("policyNo", healthProposalRes.get("carrierPropoosalNo"));
				((ObjectNode)policyRes.get("carrierRequestForm")).put("proposalStatus", "NSTP");
			}
			log.info("ABHIUWResponseProcessor policyRes : "+policyRes);
			exchange.getIn().setBody(policyRes);
		}catch(Exception e){
			
			
		}
	}

}
