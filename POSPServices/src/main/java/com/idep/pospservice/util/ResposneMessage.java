package com.idep.pospservice.util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ResposneMessage implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(ResposneMessage.class.getName());
	public void process(Exchange exchange) throws Exception {
		try{

			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			ObjectNode objectNode = objectMapper.createObjectNode();
			if(reqNode.has(POSPServiceConstant.RES_CODE)){
				objectNode.put(POSPServiceConstant.RES_CODE,reqNode.get(POSPServiceConstant.RES_CODE).asText());
				objectNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.SUCC_CONFIG_MSG);
				objectNode.put(POSPServiceConstant.RES_DATA,reqNode.get("body"));
			}else{
				objectNode.put(POSPServiceConstant.RES_CODE,"1000");
				objectNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.SUCC_CONFIG_MSG);
				objectNode.put(POSPServiceConstant.RES_DATA,reqNode);
			}
				exchange.getIn().setBody(objectNode);
		}catch(Exception e){
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put(POSPServiceConstant.RES_CODE,"1002");
			objectNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.FAILURE_MESSAGES);
			objectNode.put(POSPServiceConstant.RES_DATA,"");
			exchange.getIn().setBody(objectNode);

		}
		
	}

	
	
	
	
	
}
