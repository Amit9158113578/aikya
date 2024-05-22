package com.idep.proposal.log.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

public class HealthPolicyLogProcessor implements Processor {

	Logger log = Logger.getLogger(HealthPolicyLogProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		String data = new String();
		 
		 if(exchange.getProperty(ProposalConstants.DEFAULT_LOG)!=null)
		{
			data = data + exchange.getProperty(ProposalConstants.DEFAULT_LOG);
		}
		 
			if(exchange.getProperty(ProposalConstants.STAGE)!=null){
				data = data + exchange.getProperty(ProposalConstants.STAGE).toString()+"|";
			}
			if(exchange.getProperty(ProposalConstants.STATUS)!=null){
				data = data + exchange.getProperty(ProposalConstants.STATUS).toString()+"|";
			}
			log.info(data);
	 }
	}


