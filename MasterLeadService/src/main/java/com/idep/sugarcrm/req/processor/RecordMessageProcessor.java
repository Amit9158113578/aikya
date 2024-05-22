package com.idep.sugarcrm.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;


public class RecordMessageProcessor implements Processor
{
	/*SugarCRMModuleServices crmService = new SugarCRMModuleServices();*/

	Logger log=Logger.getLogger(RecordMessageProcessor.class.getName());
	ObjectMapper objectMapper= new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode ticketConfigNode = null;
	JsonNode leadConfigNode = null;


	public void process(Exchange exchange) throws Exception {

		try {

			String request=exchange.getIn().getBody().toString();
			request=request.substring(1, request.length()-1);
			JsonNode reqNode=objectMapper.readTree(request);
			// get Insurance type to lead validate
			exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));


		} catch (Exception e) {
			log.error("Error while preparing a message for Ticket request Q : TicketQ ",e);
		}	
	}

}
