package com.idep.sugarcrm.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RecordResponseProcessor implements Processor
{
	
	  Logger log = Logger.getLogger(RecordResponseProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  
	  public void process(Exchange exchange) throws Exception
	  {
		    try
		    {
		      
		      /**
		       * read input request
		       */
		      String request = exchange.getIn().getBody().toString();
		      JsonNode reqNode = objectMapper.readTree(request);
		      ObjectNode responseNode = objectMapper.createObjectNode();
		      responseNode.put("responseCode", 1000);
		      responseNode.put("message", "success");
		      responseNode.put("data", reqNode);
		      exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
		      
		    }
		    catch(Exception e)
		    {
		    		log.error("Exception at RecordResponseProcessor : ",e);
		    		ObjectNode responseNode = objectMapper.createObjectNode();
		    		responseNode.put("responseCode", 1002);
		    		responseNode.put("message", "failure");
		    		responseNode.put("data", "");
		    		exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
		    }
		    
	  }

}
