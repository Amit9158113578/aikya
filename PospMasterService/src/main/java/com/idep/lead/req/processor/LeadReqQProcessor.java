package com.idep.lead.req.processor;

import org.apache.camel.CamelContext;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.master.util.MasterConstants;

public class LeadReqQProcessor implements Processor {
	
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(LeadReqQProcessor.class.getName());
	static CBService service = null;
	static JsonNode sourceConfigNode = null;
	
	static
	{
		try
		{
			service = CBInstanceProvider.getServerConfigInstance(); 
			sourceConfigNode =objectMapper.readTree(service.getDocBYId(MasterConstants.SOURCE_CONFIG_DOC).content().toString());
		} 
		catch(Exception e)
		{
			log.error("Master Service not able to load SourceConfig document ",e);
		}
	}
	
	public void process(Exchange exchange) {
		 
		 try
		 {		 
			/**
			 *  create producer template to send messages to LeadRequestQ
			 */
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String request = exchange.getIn().getBody().toString();
	   		JsonNode masterReqNode =  objectMapper.readTree(request);
	   		JsonNode headerNode = masterReqNode.get(MasterConstants.REQUEST_HEADER);
	   		if(sourceConfigNode.has(headerNode.get("deviceId").asText()))
	   		{
				String uri = "activemq:queue:LeadRequestQ";//?mapJmsMessage=false
				exchange.getIn().setBody(request);
				exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
				//template.send(uri, exchange); // put request in carrier quote request queue
				
				template.sendBody(uri, request);
	   		}
	   		else{
   				log.info("Request does not have valid DeviceId or not a lead request");
   			}
		 }
		 catch(Exception e)
		 {
			 log.error("unable to send message to LeadRequestQ : ",e);
		 }
	}
}
