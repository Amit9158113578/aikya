/**
 * 
 */
package com.idep.proposal.req.processor;

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
 * This CarPrepareSoapReqProcessor class is use to pass required data to SoapConnector to create soap request
 * format, in that we required request data, schemaLocation and requestType,
 * requestType we required to fetch the required configuration document to form a request.
 * @author vipin.patil
 * Mar 13 2017
 */
public class HealthPrepareSoapReqProcessor implements Processor {

	Logger log = Logger.getLogger(HealthPrepareSoapReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector extService = new SoapConnector();
	CBService serverConfig = null;
	JsonNode configDocNode = null;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try
		{
			this.serverConfig = CBInstanceProvider.getServerConfigInstance();
			String carrierXMLReq = exchange.getIn().getBody(String.class);
			log.info("carrierXMLReq: "+carrierXMLReq);
			//if(!carrierXMLReqNode.get("requestType").asText().equals("CarPolicyDocRequest"))
			log.info("carrierFetchDocInputRequest :::: "+exchange.getProperties().containsKey("carrierFetchDocInputRequest"));
			log.info("carrierFetchDocInputRequest :::: "+exchange.getProperties().containsKey("carrierFetchDocInputRequest"));
			if(!exchange.getProperties().containsKey("carrierFetchDocInputRequest"))
			{
				log.info("has carrierFetchDocInputRequest");
			JsonNode reqNode = objectMapper.readTree(exchange.getProperty("carrierInputRequest").toString());
			log.info("reqNode"+reqNode);
			int carrierId = reqNode.get("carrierId").intValue();
			log.info("reqNode planID "+reqNode.get("planId").intValue());
			int productId = reqNode.get("planId").intValue();
			/**
			 * Here we check the if the request contains premiumDetails or not
			 * This is added for createPolicy doc, because at the time of create policy we don't have premiumDetails
			 */
			if(reqNode.has("premiumDetails"))
			{
				log.info("has premiumDetails");
			String policyType = reqNode.get("premiumDetails").get("policyType").textValue();
			log.info("policyType"+policyType);
			//log.info("Values are : "+carrierId+ " , "+productId+" , "+policyType);
			//log.info("Doc ID : "+reqNode.get("requestType").asText()+"-"+carrierId+"-"+productId+"-"+policyType);
			/**
			 * Here we are checking requestType to fetch the exact configuration document for mapping for New India.
			 */
			if(reqNode.get("requestType").textValue().equals("HealthProposalRequest"))
			{
				log.info("has HealthProposalRequest");
				configDocNode = objectMapper.readTree((serverConfig.getDocBYId(reqNode.get("requestType").asText()+"-"+carrierId+"-"+productId+"-"+policyType).content().toString()));
				log.info("Request Type HealthProposalRequest");
			}
			else
			{
				configDocNode = objectMapper.readTree((serverConfig.getDocBYId(reqNode.get("requestType").asText()+"-"+carrierId+"-"+productId).content().toString()));
				log.info("Request Type HealthPolicyHolder");
			}
			}
			}
			else
			{
				JsonNode carrierXMLReqNode = objectMapper.readTree(exchange.getProperty("carrierFetchDocInputRequest").toString());
				log.info("get property carrierXMLReqNode"+carrierXMLReqNode);
				int carrierId = carrierXMLReqNode.get("carrierId").intValue();
				int productId = carrierXMLReqNode.get("planId").intValue();
				configDocNode = objectMapper.readTree((serverConfig.getDocBYId(carrierXMLReqNode.get("requestType").asText()+"-"+carrierId+"-"+productId).content().toString()));
				log.info("configDocNode:     "+configDocNode);
			} 
			String schemaLocation =configDocNode.get("schemaLocation").asText();
			String soapRequest = extService.prepareSoapRequest(carrierXMLReq, schemaLocation);
			log.info("Soap Request : "+soapRequest);
			
			
			exchange.getIn().setBody(soapRequest);
			
	}catch(Exception e)
		{
		
		log.error("Exception at CarPrepareSoapReqProcessor",e);
		
	}
	

}
}