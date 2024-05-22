package com.idep.sugarcrm.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TicketResponseProcessor implements Processor{

	
	 Logger log = Logger.getLogger(TicketResponseProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  
	
	public void process(Exchange exchange) throws Exception {
	
		  
	      ObjectNode responseNode = objectMapper.createObjectNode();
	      responseNode.put("responseCode", 1000);
	      responseNode.put("message", "success");
	      	      
	      exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
	      
		
	}
	
	
	
	

}
