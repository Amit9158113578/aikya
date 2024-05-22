package com.idep.proposal.req.processor;


import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LeadProfileRequest {

static ObjectMapper objectMapper = new ObjectMapper();
static Logger log = Logger.getLogger(LeadProfileRequest.class.getName());
	
	public  static void sendLeadProfileRequest(JsonNode request , Exchange exchange){
		
		try{
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String uri = "activemqSecondary:queue:P365LeadProposalUpdationQ";
			log.info("sending to LeadProposalProposalUpdationQ :"+exchange);
			exchange.getIn().setBody(objectMapper.writeValueAsString(request));
			exchange.setPattern(ExchangePattern.InOnly); 
			template.send(uri, exchange);
			
		}catch(Exception e){
			log.error("unable to send request to activemqSecondary:queue:P365LeadProposalUpdationQ ",e);
		}
	}
}
