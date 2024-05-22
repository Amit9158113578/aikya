package com.idep.icrmlead.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UpdateLeadResponse implements Processor {	
	static Logger log = Logger.getLogger(UpdateLeadResponse.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	
	public void process(Exchange exchange) throws Exception{
	
	ObjectNode responseNode = objectMapper.createObjectNode();
	log.info("resssponsse with exchange output"+exchange.getIn().getHeaders().get("updateLeadId"));
	
	String leadid = (String) exchange.getIn().getHeaders().get("updateLeadId");
	log.info("lead id "+leadid);
    if (leadid != null ) 
    {
      responseNode.put("responseCode", "1000");
      responseNode.put("message", "success");
      log.info("iCRM LEAD response generated for : " + leadid);
      responseNode.put("data", leadid);
    }
    else
    {
      responseNode.put("responseCode", "1001");
      responseNode.put("message", "failure");
      responseNode.put("data", "");
      log.error("iCRM LEAD response not found : " + leadid);
    }
    log.info("in get iMAT LEAD responsssseeee" + responseNode);
    exchange.getIn().setBody(responseNode);
  }
}
