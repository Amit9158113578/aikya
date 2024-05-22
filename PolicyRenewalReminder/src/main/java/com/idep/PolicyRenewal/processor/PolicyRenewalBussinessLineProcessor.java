package com.idep.PolicyRenewal.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PolicyRenewalBussinessLineProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	
	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(PolicyRenewalBussinessLineProcessor.class.getName());
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = this.objectMapper.readTree(request);
		log.info("Processing Recieved Proposal Id "+reqNode);
		if(reqNode.has("proposalId") && reqNode.get("proposalId").asText() != null )
		{
			if(reqNode.get("proposalId").asText().contains("PROP000L"))
			{
				log.info("Its Life");
				exchange.getIn().setHeader("LOB", "Life");
			}
			else if(reqNode.get("proposalId").asText().contains("PROP000B"))
			{
				log.info("Its Bike");
				exchange.getIn().setHeader("LOB", "Bike");
			}
			else if(reqNode.get("proposalId").asText().contains("PROP000C"))
			{
				log.info("Its Car");
				exchange.getIn().setHeader("LOB", "Car");
			}
			else if(reqNode.get("proposalId").asText().contains("PROP000H"))
			{
				log.info("Its Health");
				exchange.getIn().setHeader("LOB", "Health");
			}
			
		}
		
	}

}
