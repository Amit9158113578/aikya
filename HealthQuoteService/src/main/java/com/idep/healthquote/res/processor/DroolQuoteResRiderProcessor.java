package com.idep.healthquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * 
 * @author sandeep.jadhav
 *
 */
public class DroolQuoteResRiderProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthQuoteResponseProcessor.class.getName());
	JsonNode errorNode;
	
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try
		{
			String quoteResponse = exchange.getIn().getBody().toString();
		    JsonNode quoteResponseNode = this.objectMapper.readTree(quoteResponse);
		  //  log.info("RIDERREQPROCESS quoteResponseNode:"+quoteResponseNode);
		    log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"RIDERREQPROCESS|INIT|"+"riders processing initiated");
		    log.info("input to DroolQuoteResRiderProcessor :"+quoteResponseNode);
		    /**
	    	   * process riders
	    	   */
	    	  if(quoteResponseNode.has(HealthQuoteConstants.RIDER_LIST))
	    	  {
	    		  
	    		  
	    		  if(quoteResponseNode.get(HealthQuoteConstants.RIDER_LIST).has(HealthQuoteConstants.ARRAY_LIST_NODE))
	    		  {
	    			  if(quoteResponseNode.get(HealthQuoteConstants.RIDER_LIST).get(HealthQuoteConstants.ARRAY_LIST_NODE).size()>0)
	    			  {
	    				  ArrayNode riderListNode = objectMapper.createArrayNode();
	    				  Long basicPremium= quoteResponseNode.get("basicPremium").asLong();
	    				  for(JsonNode rider : quoteResponseNode.get(HealthQuoteConstants.RIDER_LIST).get(HealthQuoteConstants.ARRAY_LIST_NODE))
	    				  {
	    					  riderListNode.add(rider.get("com.sutrr.quote.healthquotecalc.AddOnCover"));
	    					  if(rider.get("com.sutrr.quote.healthquotecalc.AddOnCover").has("riderPremiumAmount")){
	    						  basicPremium = basicPremium + rider.get("com.sutrr.quote.healthquotecalc.AddOnCover").get("riderPremiumAmount").asLong(0);
	    					  }
	    				  }
	    				  
	    				  /**
	    				   * modify riders list and basic premium
	    				   */
	    				  ((ObjectNode)quoteResponseNode).put("basicPremium", basicPremium);
	    				  log.info("basic Premium modified to : "+basicPremium);
	    				  ((ObjectNode)quoteResponseNode).put(HealthQuoteConstants.RIDER_LIST, riderListNode);
	    			  }
	    		  }
	    		  
	    		 
	    	  }
	    	  
	    	  log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"RIDERREQPROCESS|SUCCESS|"+"riders processing completed");
	    	  exchange.getIn().setBody(quoteResponseNode);
		    
		    
		}
		catch(Exception e)
		{
			//log.error("Exception at DroolQuoteResRiderProcessor : ",e);
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"DroolQuoteResRiderProcessor : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
		    objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, errorNode);
		    exchange.getIn().setBody(objectNode);
		    throw new ExecutionTerminator();
		    
		}
		
		
	}
	
	
	

}
