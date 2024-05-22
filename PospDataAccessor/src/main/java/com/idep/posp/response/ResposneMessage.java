package com.idep.posp.response;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.posp.service.POSPDataAccessorConstant;
public class ResposneMessage implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ResposneMessage.class.getName());
	public void process(Exchange exchange) throws Exception {
		try{

			JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			if(reqNode.has(POSPDataAccessorConstant.RES_CODE)){
				objectNode.put(POSPDataAccessorConstant.RES_CODE,reqNode.get(POSPDataAccessorConstant.RES_CODE).asText());
				if(reqNode.has(POSPDataAccessorConstant.RES_MSG)){
					objectNode.put(POSPDataAccessorConstant.RES_MSG ,reqNode.get(POSPDataAccessorConstant.RES_MSG).asText());
				}else{
					objectNode.put(POSPDataAccessorConstant.RES_MSG ,POSPDataAccessorConstant.SUCCESS);		
				}
				if(reqNode.has(POSPDataAccessorConstant.RES_DATA)){
					objectNode.put(POSPDataAccessorConstant.RES_DATA,reqNode.get("body"));
				}else{
					objectNode.put(POSPDataAccessorConstant.RES_DATA,reqNode);
				}
			}else{
				objectNode.put(POSPDataAccessorConstant.RES_CODE,"1000");
				objectNode.put(POSPDataAccessorConstant.RES_MSG ,POSPDataAccessorConstant.SUCCESS);
				objectNode.put(POSPDataAccessorConstant.RES_DATA,"");
			}
				exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
		}catch(Exception e){
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(POSPDataAccessorConstant.RES_CODE,"1002");
			objectNode.put(POSPDataAccessorConstant.RES_MSG ,POSPDataAccessorConstant.FAILURE);
			objectNode.put(POSPDataAccessorConstant.RES_DATA,"");
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));

		}
		
	}

	

	
	
}
