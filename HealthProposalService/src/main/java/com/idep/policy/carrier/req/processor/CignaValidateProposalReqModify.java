package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * Class to modify validate proposal request
 * @author shweta.joshi
 *
 */
public class CignaValidateProposalReqModify implements Processor {
	
	  Logger log = Logger.getLogger(CignaValidateProposalReqModify.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service =  CBInstanceProvider.getPolicyTransInstance();
	  
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator 
	{
		try 
		{
			String validateRequest  = exchange.getIn().getBody(String.class);
			JsonNode validateReqNode = objectMapper.readTree(validateRequest);
			/**
			 * Check number of product present in policyProductDOList
			 */
			if(validateReqNode.get("listofPolicyTO").get("policyProductDOList").size()>1)
			{
				/**
				 * Put value for sumInsured in riders policyProductInsuredDOList  
				 */
				ArrayNode riderInsuredDOList = (ArrayNode) validateReqNode.get("listofPolicyTO").get("policyProductDOList").get(1).get("policyProductInsuredDOList");
				for(JsonNode riderInsuredNode : riderInsuredDOList)
				{
					((ObjectNode)riderInsuredNode).put("sumInsured", riderInsuredNode.get("baseSumAssured"));
					log.debug("sumInsured value of riderInsuredNode"+riderInsuredNode.get("sumInsured"));
				}
			}
			exchange.getIn().setBody(objectMapper.writeValueAsString(validateReqNode));
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaValidateProposalReqModify|",e);
			throw new ExecutionTerminator();
		}		  
	}
}

