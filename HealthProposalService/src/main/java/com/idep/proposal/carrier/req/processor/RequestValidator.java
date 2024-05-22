package com.idep.proposal.carrier.req.processor;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.proposal.util.ProposalConstants;

public class RequestValidator implements Processor {
	Logger log = Logger.getLogger(ConfigurationLoaderProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;
	int carrierId;

	public void process(Exchange exchange) {
		try {   
			Object joltResponse = exchange.getIn().getBody();
			JsonNode joltResNode = SoapUtils.objectMapper.readTree(joltResponse.toString());
			if (joltResNode.get("responseCode").asText().equalsIgnoreCase("P365RES100")) {
				JsonNode responseNode = SoapUtils.objectMapper.readTree(joltResNode.get("data").toString());
				log.info("response :" + responseNode);
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(responseNode));
			} else {
				this.log.info("failure response received ");
				exchange.getIn().setHeader("successRes", "False");
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put(ProposalConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_CODE).intValue());
				objectNode.put(ProposalConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_MSG).textValue());
				objectNode.put(ProposalConstants.QUOTE_RES_DATA, this.errorNode);
				exchange.getIn().setBody(objectNode);   							} 
		} 
		catch (Exception e) {
			this.log.error("Exception at ConfigurationLoaderProcessor : " + e.getMessage());
			exchange.getIn().setHeader("documentFound", "False");
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put(ProposalConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_CODE).intValue());
			objectNode.put(ProposalConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_MSG).textValue());
			objectNode.put(ProposalConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);    
		} 
	}

}