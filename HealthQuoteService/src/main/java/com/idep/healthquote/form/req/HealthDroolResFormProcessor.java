package com.idep.healthquote.form.req;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.eclipse.jetty.util.log.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * 
 * @author sandeep.jadhav
 * format response received from quote engine
 */

public class HealthDroolResFormProcessor implements Processor {
	
	  Logger log = Logger.getLogger(HealthDroolResFormProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service = null;
	  JsonNode errorNode;
	  
	  
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator, JsonProcessingException, IOException {
		
	    try
	    {
	    	log.info("HealthDroolResFormProcessor is invoked.");
		      String message = exchange.getIn().getBody().toString();
		      JsonNode root = this.objectMapper.readTree(message);
		      String results = root.get(HealthQuoteConstants.DROOLS_RESULT_NODE).textValue();
		      
		      JsonNode resultsNode = this.objectMapper.readTree(results);
		    //  log.info("HealthDroolResFormProcessor resultsNode."+resultsNode);
		      JsonNode quoteResult = resultsNode.get(HealthQuoteConstants.DROOLS_RESULTS_NODE)
		        .get(1).get(HealthQuoteConstants.DROOLS_VALUE_NODE)
		        .get(HealthQuoteConstants.DROOLS_ELEMENT_NODE);
		   
		      //JsonNode prodRatingNode = this.objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.RATINGS).toString());
		   
		      JsonNode featureNode = this.objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.FEATURES).toString());
		   
		   /*   JsonNode productRating = null;
		      JsonNode productRisksNode = null;
		   
			  productRisksNode = prodRatingNode.get(HealthQuoteConstants.DEFAULT_RISK_TYPE);
			  productRating = prodRatingNode.get(HealthQuoteConstants.RATINGS_LIST);
		   */
			  JsonNode quoteResultNode =  quoteResult.get(0);
			  JsonNode quoteNode = quoteResultNode.get(HealthQuoteConstants.QUOTE_RESPONSE);
			  ObjectNode quoteResNode = objectMapper.createObjectNode();
	    	  if(quoteNode!=null)
	    	  {
	    		  log.info("QuoteNode is not null");
	    		  quoteResNode.putAll((ObjectNode)quoteNode);
	    		  quoteResNode.put("childPlanId", exchange.getProperty(HealthQuoteConstants.PRODUCT_CHILDPLANID).toString());
	    		  quoteResNode.put(HealthQuoteConstants.DISCOUNT_DETAILS, quoteNode.get(HealthQuoteConstants.DISCOUNT_DETAILS).get(HealthQuoteConstants.ARRAY_LIST_NODE));
			    	
			    	  /**
			    	   * process discount
			    	   */
			    	  
			    	/*  if(quoteNode.has(HealthQuoteConstants.DISCOUNT_DETAILS))
			    	  {
			    		  
			    		  
			    		  if(quoteNode.get(HealthQuoteConstants.DISCOUNT_DETAILS).has(HealthQuoteConstants.ARRAY_LIST_NODE))
			    		  {
			    			  if(quoteNode.get(HealthQuoteConstants.DISCOUNT_DETAILS).get(HealthQuoteConstants.ARRAY_LIST_NODE).size()>0)
			    			  {
			    				  ArrayNode discountListNode = objectMapper.createArrayNode();
			    				  
			    				  for(JsonNode rider : quoteNode.get(HealthQuoteConstants.DISCOUNT_DETAILS).get(HealthQuoteConstants.ARRAY_LIST_NODE))
			    				  {
			    					  discountListNode.add(rider.get("com.sutrr.quote.healthquotecalc.DiscountDetails"));
			    				  }
			    				  
			    				  
			    				  quoteResNode.put(HealthQuoteConstants.DISCOUNT_DETAILS, discountListNode);
			    			  }
			    		  }
			    		  
			    		 
			          }
			    	  
			    	  //add ratings
			    	   
				        if (productRating != null)
				        {
				        	quoteResNode.put(HealthQuoteConstants.RATINGS_LIST,productRating);
				        }
				        else
				        {
				        	quoteResNode.put(HealthQuoteConstants.RATINGS_LIST,"");
				        }
				        
				        if(productRisksNode != null )
				        {
				        	quoteResNode.put(HealthQuoteConstants.RISKS, productRisksNode);
				        }
				        else
				        {
				        	quoteResNode.put(HealthQuoteConstants.RISKS, "");
				        }
				        */
				     //  log.info("quoteResNode is:::"+quoteResNode);
				        quoteResNode.put(HealthQuoteConstants.FEATURES_LIST, featureNode);
				       // log.info("quoteResNode after update is:::"+quoteResNode);
	    	  }else{
	    		  log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthDroolResFormProcessor : "+message);
	  			throw new ExecutionTerminator();
	    		  /*log.error("Expected response not recived from Quote engnie : "+message);
	    		  throw new ExecutionTerminator();*/
	    	  }
	    	  log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTERES|SUCCESS|"+"quote calculated successfully");
		    /**
		     * save quote engine response in exchange body	  
		     */
		    exchange.getIn().setBody(quoteResNode);
		      
	    }
	    
	    catch (NullPointerException e)
	    {
//	    	  this.log.error("NullPointerException at HealthDroolResFormProcessor : ", e);
	    	  log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthDroolResFormProcessor :",e);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);
		      throw new ExecutionTerminator();
	    }
	    catch (JsonProcessingException e)
	    {
	    	  //this.log.error("JsonProcessingException at HealthDroolResFormProcessor : ", e);
	    	 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthDroolResFormProcessor :",e);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);
		      throw new ExecutionTerminator();
	    }
	    catch (IOException e)
	    {
	    	 // this.log.error("IOException at HealthDroolResFormProcessor : ", e);
	    	 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthDroolResFormProcessor :",e);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);
		      throw new ExecutionTerminator();
	    }
	    catch (Exception e)
	    {
	    	  //this.log.error("Exception at HealthDroolResFormProcessor : ", e);
	    	 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthDroolResFormProcessor :",e);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);
		      throw new ExecutionTerminator();
	    }
	    
	    
	  }
}