package com.idep.icrmlead.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class UpdateLeadRequest implements Processor {
	Logger log = Logger.getLogger(UpdateLeadRequest.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode leadConfigNode = null;

	public void process(Exchange exchange)   throws Exception
	{
		try
		{
			  String request = exchange.getIn().getBody().toString();
		      JsonNode reqNode = this.objectMapper.readTree(request);
		      JsonNode headerReqNode = reqNode.get("header");
		      JsonNode bodyReqNode = reqNode.get("body");
		      this.log.info("PreparedLeadRequest BODY : REQ NODE :" + headerReqNode);
		      this.log.info("PreparedLeadRequest Header : REQ NODE :" + bodyReqNode);
		      this.log.info("PreparedLeadRequest REQUEST: REQ NODE :" + reqNode);
		      this.log.info("Ressulltss..." + reqNode.findValue("results"));
		      exchange.getIn().setHeader("Request", reqNode.findValue("results"));
		}
		catch(Exception e)
		{
			log.error("Error in UpdateLeadRequest Processor");
		}	    
	}
}