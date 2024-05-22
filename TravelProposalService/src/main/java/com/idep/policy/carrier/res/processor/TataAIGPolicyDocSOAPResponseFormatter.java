package com.idep.policy.carrier.res.processor;


import java.util.concurrent.ExecutionException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TataAIGPolicyDocSOAPResponseFormatter implements Processor {
	
	  Logger log = Logger.getLogger(TataAIGPolicyDocSOAPResponseFormatter.class.getName());
	  SoapConnector soapService = new SoapConnector();
	  ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {

		try {
		
			String carrierResponse  = exchange.getIn().getBody(String.class);

			  ObjectNode obj = objectMapper.createObjectNode();
			  JsonNode policyConfigNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.TRAVPOLICYDOC_CONFIG).toString());

				if(carrierResponse.contains("&gt;") || carrierResponse.contains("&lt;"))
				{
				carrierResponse = carrierResponse.replaceAll("&gt;", ">");
				carrierResponse = carrierResponse.replaceAll("&lt;", "<");
				}
				log.debug("Carrier Rsponse after : "+carrierResponse);
				//log.debug("Result Tag Name : "+configDocNode.get("resultTagName").asText());
				String formattedSooapRes="";
				if(policyConfigNode.has("responseResultTag"))
				 formattedSooapRes = soapService.getSoapResult(carrierResponse,policyConfigNode.get("responseResultTag").get("resultTag").asText());
				log.info("formattedSooapRes Tag :"+formattedSooapRes);
				//String response = formattedSooapRes.substring(formattedSooapRes.indexOf("<PolicyDoc>"),formattedSooapRes.indexOf("</PolicyDoc>"));
				log.debug("final response : "+formattedSooapRes);
				//String formattedSoapRes = soapService.retriveSoapResult(carrierResponse,"int-get-policy-status-iO");
				
				obj.put("pdfString", formattedSooapRes);
				exchange.getIn().setBody(obj);
		  
			
		}
		catch(Exception e)
		{
			log.error("unable to genrate policy document : ",e);	
			throw new ExecutionTerminator();
		}
		  
	}
	

}
