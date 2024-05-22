package com.idep.lifequote.carrier.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.lifequote.res.processor.LifeQuoteResponseProcessor;
import com.idep.lifequote.util.LifeQuoteConstants;

public class KotakQuoteNumSetter  implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeQuoteResponseProcessor.class.getName());
	//JsonObject responseConfigNode = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.RESPONSE_CONFIG_DOC);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
	
		String quoteResdata = exchange.getIn().getBody().toString();
		
		JsonNode carrierResNode = this.objectMapper.readTree(quoteResdata);
		
		
		//exchange.getProperty(LifeQuoteConstants.LOG_REQ).toString()
		
		String QuoteNum=exchange.getProperty(LifeQuoteConstants.QuotationNumber).toString().replaceAll("\"", "");		
		log.debug("QuoteNum is: "+QuoteNum);
		
		((ObjectNode)carrierResNode).put("QuotationNumber",QuoteNum );

		//String quoteResdata1=quoteResdata+QuoteNum;
		
		exchange.getIn().setBody(carrierResNode);			

	
	}
	}