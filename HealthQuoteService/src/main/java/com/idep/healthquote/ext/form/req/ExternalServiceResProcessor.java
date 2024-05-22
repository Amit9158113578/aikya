package com.idep.healthquote.ext.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ExternalServiceResProcessor implements Processor {
	
	  Logger log = Logger.getLogger(ExternalServiceResProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service = null;
	  JsonNode responseConfigNode;
	  JsonNode errorNode;

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {

		try {
			
		      if (this.service == null)
		      {
		        this.service = CBInstanceProvider.getServerConfigInstance();
		        this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
		        this.log.info("ResponseMessages configuration loaded");
		        
		      }
		      
		      String response  = exchange.getIn().getBody(String.class);
		      JsonNode finalResultNode = this.objectMapper.readTree(response);
		      finalResultNode = finalResultNode.get(HealthQuoteConstants.CARRIER_REQUEST_FORM); // get response from sutrrMapper
		      ArrayNode finalQuoteResult = this.objectMapper.createArrayNode();
		      /*JsonNode prodRatingNode = this.objectMapper.readTree(exchange.getProperty(HealthQuoteConstants.RATINGS).toString());
		      JsonNode productRisksNode = prodRatingNode.get(HealthQuoteConstants.DEFAULT_RISK_TYPE);
		      JsonNode productRating = prodRatingNode.get(HealthQuoteConstants.RATINGS_LIST);
	      
		       if(productRisksNode != null )
		       {
		    	   ((ObjectNode)finalResultNode).put(HealthQuoteConstants.RISKS, productRisksNode);
		       }
		       else
		       {
		    	   ((ObjectNode)finalResultNode).put(HealthQuoteConstants.RISKS, "");
		       }
	       
		       if (productRating != null)
		        {
			          for (JsonNode ratingNode : productRating) {
			            if ((ratingNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue() == finalResultNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()) && (ratingNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue() == finalResultNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()))
			            {
			            	 ((ObjectNode)finalResultNode).put(HealthQuoteConstants.RATINGS_LIST, ratingNode.get(HealthQuoteConstants.CATEGORY_MAP));
			            }
			          }
			          
		    	   ((ObjectNode)finalResultNode).put(HealthQuoteConstants.RATINGS_LIST, productRating);
		        }
		       else
		       {
		    	   ((ObjectNode)finalResultNode).put(HealthQuoteConstants.RATINGS_LIST, "");
		       }
		*/
	       	  finalQuoteResult.add(finalResultNode);
	       	  ObjectNode quoteResultNode = this.objectMapper.createObjectNode();
	       	  quoteResultNode.put(HealthQuoteConstants.QUOTES, finalQuoteResult);	
	       	  
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, quoteResultNode);
		      exchange.getIn().setBody(objectNode);
		      
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"ExternalServiceResProcessor : ",e);
		      /*ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);*/
		      throw new ExecutionTerminator();
		}
	}

}
