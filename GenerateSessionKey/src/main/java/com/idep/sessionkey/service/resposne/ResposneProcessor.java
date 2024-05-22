package com.idep.sessionkey.service.resposne;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.sessionkey.service.util.GenrateSessionKeyConstant;


public class ResposneProcessor implements Processor {
	Logger log = Logger.getLogger(ResposneProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
	
		
		try{
			
			JsonNode inputReq = objectMapper.readTree(exchange.getIn().getBody(String.class));
			JsonNode response = objectMapper.createObjectNode();
			
			
			((ObjectNode)response).put(GenrateSessionKeyConstant.RES_CODEKEY, GenrateSessionKeyConstant.RES_CODE);
			((ObjectNode)response).put(GenrateSessionKeyConstant.RES_MSGKEY, GenrateSessionKeyConstant.RES_MSG);
			((ObjectNode)response).put(GenrateSessionKeyConstant.RES_DATAKEY, inputReq);
				
			
			exchange.getIn().setBody(objectMapper.writeValueAsString(response));
		}catch(Exception e){
		log.error("unable to send response : ",e);	
		}
		
		
	}

}
