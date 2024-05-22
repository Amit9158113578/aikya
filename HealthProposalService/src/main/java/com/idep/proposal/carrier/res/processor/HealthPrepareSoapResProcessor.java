/**
 * 
 */
package com.idep.proposal.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
//import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author vipin.patil
 *
 */
public class HealthPrepareSoapResProcessor implements Processor{

	Logger log = Logger.getLogger(HealthPrepareSoapResProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();;
	JsonNode configDocNode = null;

	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
	try
	{
		String soapResponse = exchange.getIn().getBody(String.class);
		if(!exchange.getProperties().containsKey("carrierFetchDocInputRequest"))
		{
		JsonNode reqNode = objectMapper.readTree(exchange.getProperty("carrierInputRequest").toString());
		int carrierId = reqNode.get("carrierId").intValue();
		int productId = reqNode.get("planId").intValue();
		if(reqNode.has("premiumDetails"))
		{
		String policyType = reqNode.get("premiumDetails").get("policyType").textValue();
		if(reqNode.get("requestType").textValue().equals("CarProposalRequest"))
		{
		configDocNode = objectMapper.readTree((serverConfig.getDocBYId(reqNode.get("requestType").asText()+"-"+carrierId+"-"+productId+"-"+policyType).content().toString()));
		}
		else
		{
			configDocNode = objectMapper.readTree((serverConfig.getDocBYId(reqNode.get("requestType").asText()+"-"+carrierId+"-"+productId).content().toString()));
			log.info("Request Type CarPolicyHolder");
		}
		}
		}
		else
		{
			JsonNode fetchDocResNode = objectMapper.readTree(exchange.getProperty("carrierFetchDocInputRequest").toString());
			int carrierId = fetchDocResNode.get("carrierId").intValue();
			int productId = fetchDocResNode.get("productId").intValue();
			configDocNode = objectMapper.readTree((serverConfig.getDocBYId(fetchDocResNode.get("requestType").asText()+"-"+carrierId+"-"+productId).content().toString()));
		}
		SoapConnector extService = new SoapConnector();
		String tagName =configDocNode.get("resultTagName").asText();
		String soapResult = extService.prepareSoapResult(soapResponse, tagName);
		log.info("Soap Result : "+soapResult);
		exchange.getIn().setBody(soapResult);
	}
		catch(Exception e)
		{
			log.error("Exception at CarPrepareSoapResProcessor for Proposal ",e);
			
		}
	}
}

