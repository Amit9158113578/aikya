package com.idep.lead.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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
	static JsonNode leadStageConfigNode = null;
	static
	{
		try
		{
			service = CBInstanceProvider.getServerConfigInstance(); 
		  	sourceConfigNode =objectMapper.readTree(service.getDocBYId(MasterConstants.SOURCE_CONFIG_DOC).content().toString());
		  	leadStageConfigNode =objectMapper.readTree(service.getDocBYId(MasterConstants.LEAD_STAGES).content().toString());
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
			String request = exchange.getIn().getBody().toString();
	   		JsonNode masterReqNode =  objectMapper.readTree(request);
	   		JsonNode headerNode = masterReqNode.get(MasterConstants.REQUEST_HEADER);
	   		if(sourceConfigNode.has(headerNode.get("deviceId").asText()))
	   			{
	   				if((leadStageConfigNode.get("servicesList").has(headerNode.get("transactionName").asText()))) 
	   				{
	   					log.info("Master request has deviceId and transationname is macthed ");
	   					exchange.getIn().setHeader("applicableLeadRequest", "Yes");
	   				}
	   				else
	   				{
	   					log.info("Master request has deviceId and transationname is not macthed ");
	   				}
	   			}
	   		else
	   		{
   				log.info("Request does not have valid DeviceId or not a lead request");
   			}
		 }
		 catch(Exception e)
		 {
			 log.error("unable to send message to LeadRequestQ : ",e);
		 }
	}
}
