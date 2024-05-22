
package com.idep.policydoc.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.policydoc.util.PolicyDocViewConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
/**
 * 
 * @author sandeep.jadhav
 * 
 */
public class PolicyDocViewerReqHandler implements Processor
{
	
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(PolicyDocViewerReqHandler.class.getName());
  
  public void process(Exchange exchange)
    
  {
	  try {
		  
		  	String docViewRequest = exchange.getIn().getBody(String.class);
		    JsonNode docViewRequestNode = this.objectMapper.readTree(docViewRequest);
		    exchange.getIn().setBody(this.objectMapper.writeValueAsString(docViewRequestNode.get(PolicyDocViewConstants.CARRIER_MAPPER_REQ)));
	 
	  }
	  
	  catch(Exception e)
	  {
		  log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICYDOCVIEWMAPPER+"|ERROR|"+"Exception at PolicyDocViewerReqHandler:");
	  }
    
  }
}
