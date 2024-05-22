package com.idep.policy.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;


public class HDFCUWResponseProcessor implements Processor  {

	
	Logger log = Logger.getLogger(HDFCUWResponseProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
			
			String input = exchange.getIn().getBody(String.class);
			log.debug("HDFCUWResponseProcessor Policy Res : "+input);
			JsonNode policyRes = objectMapper.readTree(input);
			
			/*JsonObject proposalDoc = transService.getDocBYId(policyRes.get("transactionStausInfo").get("proposalId").asText()).content();
			log.info("HDFCUWResponseProcessor Policy Res : "+policyRes);
			JsonNode healthProposalRes = objectMapper.readTree(proposalDoc.get("healthProposalResponse").toString()); */
			
			if(policyRes.get("transactionStausInfo").get("policyNo").asText().equalsIgnoreCase("0")){
				//((ObjectNode)policyRes.get("transactionStausInfo")).put("policyNo", policyRes.get("carrierProposalResponse").get("CustomerId"));
				((ObjectNode)policyRes).put("policyNo", policyRes.get("transactionStausInfo").get("policyNo"));
				((ObjectNode)policyRes.get("carrierProposalResponse")).put("proposalStatus", "NSTP");
			}else{
				((ObjectNode)policyRes.get("carrierProposalResponse")).put("proposalStatus", "STP");
			}
			
			log.info("HDFCUWResponseProcessor policyRes : "+policyRes);
			exchange.getIn().setBody(policyRes);
		}catch(Exception e){
		log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|"+"HDFCUWResponseProcessor : ",e);
		throw new ExecutionTerminator();
		}
	}
}
