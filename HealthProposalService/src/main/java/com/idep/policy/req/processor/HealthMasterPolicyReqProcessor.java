package com.idep.policy.req.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.proposal.util.ProposalConstants;

public class HealthMasterPolicyReqProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthMasterPolicyReqProcessor.class.getName());
	CBService service = null;
	JsonNode serviceConfigNode = null;
	
	
	public void process(Exchange inExchange) {
		
	    
		   try {
					 if (this.service == null)
					 {
						this.service = CBInstanceProvider.getServerConfigInstance(); 
					    this.serviceConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ProposalConstants.SERVICE_URL_CONFIG_DOC).content().toString());
					 }
					 
					 String inputmsg = inExchange.getIn().getBody(String.class);
					 JsonNode masterReqNode =  this.objectMapper.readTree(inputmsg);
					 
					 inExchange.getIn().setHeader(ProposalConstants.PROPOSAL_SERVICE_HEADER, this.serviceConfigNode.get("Proposal-"+masterReqNode.get("carrierId").intValue()).textValue());
					 inExchange.getIn().setBody(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(masterReqNode));
			}
			 catch (JsonProcessingException e)
			 {
				  	this.log.error("JsonProcessingException at MasterPolicyReqProcessor",e);
			 }
			 catch (IOException e)
			 {
				 	this.log.error("IOException at MasterPolicyReqProcessor",e);
			 }
			 catch (Exception e)
			 {
				 	this.log.error("Exception at MasterPolicyReqProcessor",e);
			 }
		     
		}

}
