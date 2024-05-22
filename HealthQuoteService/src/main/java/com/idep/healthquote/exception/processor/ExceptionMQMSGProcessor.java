package com.idep.healthquote.exception.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ExceptionMQMSGProcessor implements Processor {
	
	  Logger log = Logger.getLogger(ExceptionMQMSGProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service = null;
	  JsonNode responseConfigNode;
	  JsonNode errorNode;
	
	 @Override
	public void process(Exchange exchange) throws JsonProcessingException {
		 
		 exchange.getIn().setHeader(HealthQuoteConstants.JMSCORRELATION_ID, exchange.getProperty(HealthQuoteConstants.CORRELATION_ID));
	
		 try {
			 
		  if (this.service == null)
		  {
			      this.service = CBInstanceProvider.getServerConfigInstance();
			      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
			        
		  }
	     ObjectNode objectNode = this.objectMapper.createObjectNode();
	     objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
	     objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
	     objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
	     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
	     
		}
		 catch(Exception e)
		 {
			  ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			 
		 }
	 }
	 
}	 
