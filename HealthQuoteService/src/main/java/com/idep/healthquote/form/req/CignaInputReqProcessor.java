package com.idep.healthquote.form.req;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;

public class CignaInputReqProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CignaInputReqProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = null;
	JsonNode responseConfigNode = null;
	JsonNode errorNode;
	JsonNode healthSumCalc = null;
	
	 @Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		 
		    try
		    {	        
		      String quotedata = exchange.getIn().getBody().toString();
		      log.info("quotedata in CignaInputReqProcessor:" +quotedata);
		      JsonNode reqNode = objectMapper.readTree(quotedata);
		      
		      ArrayNode inputRidersArray = objectMapper.createArrayNode();
		      
		      ObjectNode objNode = objectMapper.createObjectNode();
		      objNode.put("riderId","32");
		      //objNode.put("riderType","R");
		      inputRidersArray.add(objNode);
		   		     
		      ObjectNode objNode1 = objectMapper.createObjectNode();
		      objNode1.put("riderId","38");
		      //objNode1.put("riderType","R");
		      inputRidersArray.add(objNode1);
		      
		      
		      ObjectNode objNode2 = objectMapper.createObjectNode();
		      objNode2.put("riderId","16");
		      inputRidersArray.add(objNode2);
		      
		      ((ObjectNode)reqNode).put("riders", inputRidersArray);
		      exchange.getIn().setBody(reqNode);
		      
		      
		    }
		    catch (NullPointerException e)
		    {
		      this.log.error("NullPointerException at ExternalServiceReqProcessor : ", e);
		     
		      throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		      this.log.error("Exception at ExternalServiceReqProcessor : ", e);
		    
		      throw new ExecutionTerminator();
		    }
	  }
	 
}

		    