package com.idep.policy.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;


public class TravlProposalResExtractProcessor implements Processor {
	Logger log = Logger.getLogger(TravlProposalResExtractProcessor.class.getName());
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  ObjectMapper objectMapper = new ObjectMapper();
	
	  SoapConnector  soapService = new SoapConnector();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			String inputReq= exchange.getIn().getBody(String.class);
			
			JsonNode HealthProposalRequest = (JsonNode) exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG);
			log.info("HealthProposalRequest document"+HealthProposalRequest);
			String startTag = HealthProposalRequest.get("carrierSOAPConfig").get("reqConfig").get("resConfig").get("startTagName").asText();
			
			String Response = soapService.getSoapResult(inputReq, startTag);
			
			String ResponseConcat="<xmlRes>"+Response+"</xmlRes>";
			log.info("Extracted Carrier proposall resposne : "+ResponseConcat);
		
			exchange.getIn().setBody(ResponseConcat);
			
		}catch(Exception e){
			log.error("unable to extract proposal resposone : ",e);
		}
		
	}

}
