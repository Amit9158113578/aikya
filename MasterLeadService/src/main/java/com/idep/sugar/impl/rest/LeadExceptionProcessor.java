package com.idep.sugar.impl.rest;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.sugar.util.SugarCRMConstants;
/*
* send error message as response 
* @author  Gauri bhalerao
* @version 1.0
* @since   9-03-2018
*/
public class LeadExceptionProcessor implements Processor {
	
	  Logger log = Logger.getLogger(LeadExceptionProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  CBService service = null;
	  JsonNode responseConfigNode;
	  JsonNode errorNode=null;
	 public void process(Exchange exchange) throws JsonProcessingException {
		 ObjectNode objectNode = this.objectMapper.createObjectNode();	 
	
	    try {
			 
		 if (this.service == null)
		 {
			      this.service = CBInstanceProvider.getServerConfigInstance();
			      this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(SugarCRMConstants.RESPONSE_CONFIG_DOC).content().toString());
			        
		 }
		 
		 log.info("lead Exception "+exchange.getIn().getBody(String.class));
		
			  objectNode.put(SugarCRMConstants.LEAD_RES_CODE, this.responseConfigNode.get(SugarCRMConstants.ERROR_CONFIG_CODE).intValue());
			     objectNode.put(SugarCRMConstants.LEAD_RES_MSG, this.responseConfigNode.get(SugarCRMConstants.ERROR_CONFIG_MSG).textValue());
			     objectNode.put(SugarCRMConstants.LEAD_RES_DATA, this.errorNode);
		
			     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
	     
		}
		 catch(Exception e)
		 {
			 
	
			 objectNode.put(SugarCRMConstants.LEAD_RES_CODE, this.responseConfigNode.get(SugarCRMConstants.ERROR_CONFIG_CODE).intValue());
		     objectNode.put(SugarCRMConstants.LEAD_RES_MSG, this.responseConfigNode.get(SugarCRMConstants.ERROR_CONFIG_MSG).textValue());
		     objectNode.put(SugarCRMConstants.LEAD_RES_DATA, this.errorNode);
	
		     exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode.toString()));
    	 log.error("LeadExceptionProcessor : ",e);
		 }
	 }
	 
	 
}	 
