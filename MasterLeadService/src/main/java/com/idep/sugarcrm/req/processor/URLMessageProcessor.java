package com.idep.sugarcrm.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class URLMessageProcessor implements Processor{
	Logger log=Logger.getLogger(URLMessageProcessor.class.getName());
	ObjectMapper objectMapper= new ObjectMapper();
	public void process(Exchange exchange) throws Exception {
		try {

			String request=exchange.getIn().getBody().toString();
			JsonNode reqNode=objectMapper.readTree(request);
			log.info("Udate Image URL reqNode :"+reqNode);
			
			exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
		} catch (Exception e) {
			log.error("Error while preparing a message for URLMessageProcessor : UpdateImageURLCRMQ ",e);
		}	
	}

}
