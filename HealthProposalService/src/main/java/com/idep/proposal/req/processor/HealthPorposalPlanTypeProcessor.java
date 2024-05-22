package com.idep.proposal.req.processor;

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

/**
 * @author pravin.jakhi
 *	this class created for get document from couchbase DB and set PlanType in request.
 *eg Individual or Family  
 *
 *
 */
public class HealthPorposalPlanTypeProcessor implements Processor {

	 ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(HealthPorposalPlanTypeProcessor.class.getName());
	  CBService productInstance =  CBInstanceProvider.getProductConfigInstance();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		
		
		try{
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			/**
			 * document read from CB 
			 * **/
			JsonNode healthPlanDoc = objectMapper.readTree(productInstance.getDocBYId(ProposalConstants.HEALTHPLAN+"-"+inputReqNode.get(ProposalConstants.CARRIER_ID).asInt()+"-"+inputReqNode.get(ProposalConstants.PLAN_ID).asInt()).content().toString());
			
			String planType = healthPlanDoc.get(ProposalConstants.PLANTYPE).asText();
			
			
			 ((ObjectNode)inputReqNode).put("planType", planType);
			 
			 exchange.getIn().setBody(inputReqNode);
			}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|CignaInsuredMemberDataProcessor|",e);
			throw new ExecutionTerminator();
		}
		

	}

}
