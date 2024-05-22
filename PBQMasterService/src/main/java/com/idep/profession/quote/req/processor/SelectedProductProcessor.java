package com.idep.profession.quote.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.profession.quote.util.QuoteDataReader;

public class SelectedProductProcessor implements Processor{
	
	ObjectMapper mapper = new ObjectMapper();
	Logger log = Logger.getLogger(SelectedProductProcessor.class);
	@Override
	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode requestNode = mapper.readTree(request);
		JsonNode errorNode = null;
		try
		{		  
		  JsonNode quoteResponse = QuoteDataReader.updateSelection(requestNode);
		  ObjectNode finalresultNode = this.mapper.createObjectNode();
	      finalresultNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").asInt());
	      finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
	      finalresultNode.put("data", quoteResponse);
	      log.info("Final Result Node with status:"+quoteResponse);
	      exchange.getIn().setBody(finalresultNode);
		}
	    catch (Exception e)
	    {
	      this.log.error("Exception at ProfessionalQuoteDataProcessor ", e);
	      ObjectNode finalresultNode = this.mapper.createObjectNode();
	      finalresultNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
	      finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
	      finalresultNode.put("data", errorNode);
	      exchange.getIn().setBody(finalresultNode);
	    }	
	}
}
