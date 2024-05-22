package com.idep.proposal.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;

public class StarHealthInsuredMemberProcessor implements Processor
{
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(StarHealthInsuredMemberProcessor.class.getName());
	  
	  
	  public void process(Exchange exchange)  throws Exception {
		  
	    try
	    {
	    	String mapperReq = (String)exchange.getIn().getBody(String.class);
	        JsonNode mapperReqNode = this.objectMapper.readTree(mapperReq);
	        
	        /**
	         * process insured members
	         */
	        
	        if(mapperReqNode.has("insureds"))
	        {
	        	 for(int i=0;i<mapperReqNode.get("insureds").size();i++)
	 	        {
	 	        	((ObjectNode)mapperReqNode).put("insureds["+i+"]",mapperReqNode.get("insureds").get(i));
	 	        }
	        }
	        else
	        {
	        	log.error("insured node is missing, cannot process without insured members");
	        	throw new ExecutionTerminator();
	        }
	        
	       /**
	        * remove insureds node from request
	        */
	        ((ObjectNode)mapperReqNode).remove("insureds");
	        
	    	log.info("insureds after modifications : "+mapperReqNode);
	        exchange.getIn().setBody(mapperReqNode);
	    	
	    }
	    
	    catch(Exception e)
	    {
	    	
	    	log.error(e);
	    }
}


}
