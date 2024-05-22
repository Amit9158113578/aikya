package com.idep.healthquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * 
 * @author sandeep.jadhav
 * Quote Response Processor
 */
public class HealthQuoteResponseProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthQuoteResponseProcessor.class.getName());
	JsonNode errorNode;
	
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try
		{
			String quoteResponse = exchange.getIn().getBody().toString();
		    JsonNode quoteResponseNode = this.objectMapper.readTree(quoteResponse);
		    
		    
		    ArrayNode finalQuoteResult = this.objectMapper.createArrayNode();
		    finalQuoteResult.add(quoteResponseNode);
		    ObjectNode quoteResultNode = this.objectMapper.createObjectNode();
		    quoteResultNode.put(HealthQuoteConstants.QUOTES, finalQuoteResult);
		    
		    /**
		     * set quote response in body
		     */
		    ObjectNode objectNode = this.objectMapper.createObjectNode();
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, quoteResultNode);
		    
		    exchange.getIn().setBody(objectNode);
		   // log.info("HealthQuoteResponseProcessor objectNode"+objectNode);
		    
		}
		catch(Exception e)
		{
			log.error("Exception at HealthQuoteResponseProcessor : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.ERROR_CONFIG_CODE).get(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.ERROR_CONFIG_MSG).get(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, errorNode);
		    exchange.getIn().setBody(objectNode);
		}
		
		
	}
	
	
	

}
