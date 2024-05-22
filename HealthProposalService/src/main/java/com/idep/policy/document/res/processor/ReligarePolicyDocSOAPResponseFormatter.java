package com.idep.policy.document.res.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ReligarePolicyDocSOAPResponseFormatter implements Processor {
	
	  Logger log = Logger.getLogger(ReligarePolicyDocSOAPResponseFormatter.class.getName());
	  SoapConnector soapService = new SoapConnector();
	  ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
		
		  String carrierResponse  = exchange.getIn().getBody(String.class);

		  ObjectNode obj = objectMapper.createObjectNode();

			if(carrierResponse.contains("&gt;") || carrierResponse.contains("&lt;"))
			{
			carrierResponse = carrierResponse.replaceAll("&gt;", ">");
			carrierResponse = carrierResponse.replaceAll("&lt;", "<");
			}
			log.debug("Carrier Rsponse after : "+carrierResponse);
			//log.debug("Result Tag Name : "+configDocNode.get("resultTagName").asText());
			String formattedSooapRes = soapService.getSoapResult(carrierResponse,"return");
			
			String response = formattedSooapRes.substring(formattedSooapRes.indexOf("<StreamData>")+12,formattedSooapRes.indexOf("</StreamData>"));
			log.debug("final response : "+response);
			//String formattedSoapRes = soapService.retriveSoapResult(carrierResponse,"int-get-policy-status-iO");
			
			obj.put("pdfString", response);
			exchange.getIn().setBody(obj);
		  
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ReligarePolicySOAPResponseFormatter|",e);
			throw new ExecutionTerminator();
		}
		  
	}
	

}
