package com.idep.policy.document.req.processor;

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

public class PolicyDocumentReqProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(PolicyDocumentReqProcessor.class.getName());
  CBService proposalService =  CBInstanceProvider.getPolicyTransInstance();
  CBService serviceConfig =  CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
	    try
	    {
	    	
	      String policyDocRequest = exchange.getIn().getBody(String.class);
	      JsonNode reqNode = this.objectMapper.readTree(policyDocRequest);
	      
	      /**
	       * get proposal document from transaction bucket
	       */
	      JsonNode proposalDocNode = objectMapper.readTree(proposalService.getDocBYId(reqNode.get("proposalId").asText()).content().toString());
	      ((ObjectNode)reqNode).put("requestType", "HealthPolicyDocumentRequest");
	      ((ObjectNode)reqNode).putAll((ObjectNode)proposalDocNode);
	      
	      /**
	       * set policy PDF request in exchange property
	       */
	      exchange.setProperty(ProposalConstants.HLTHPOLICYDOC_INPUT_REQ, reqNode);
	      
	      /**
	       * set policy PDF configuration in exchange property
	       */
	      JsonNode policyConfigNode=null;
	      try{
	    	  policyConfigNode = objectMapper.readTree(serviceConfig.getDocBYId("HealthPolicyDocumentRequest-"+reqNode.get("carrierId").asInt()).content().toString());
	      }catch(Exception e){
	    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCREQ+"|ERROR|Document Not found|"+"HealthPolicyDocumentRequest-"+reqNode.get("carrierId").asInt(),e);
				throw new ExecutionTerminator();
	      }
	       exchange.setProperty(ProposalConstants.HLTHPOLICYDOC_CONFIG, policyConfigNode);
	      
	      exchange.getIn().setBody(reqNode);
	      
	    }
	    catch (Exception e)
	    {
	    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCREQ+"|ERROR|",e);
			throw new ExecutionTerminator();
	    }
  	}
}
