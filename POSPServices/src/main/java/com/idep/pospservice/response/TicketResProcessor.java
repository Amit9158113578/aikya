package com.idep.pospservice.response;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.pospservice.util.POSPServiceConstant;

public class TicketResProcessor implements Processor {
	static Logger log = Logger.getLogger(TicketResProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
/*
	public ObjectNode createTicketResponse(String request){
		ObjectNode responseNode = objectMapper.createObjectNode();
		try{
			
			if(request.has("status")){
				responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_SUCCESS_RES_CODE);
				responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.SUCCESS_MSG_VAL);
				responseNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.TICKET_CREATED);
			}else{
				responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
				responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
				responseNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.TICKET_NOT_CREATED);
			}
		}catch(Exception e){
			responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
			responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
			responseNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.TICKET_NOT_CREATED);
		}
		return responseNode;
	}

	public JsonNode getTicketResponse(String request){
		JsonNode reqNode = null;
		ObjectNode responseNode = objectMapper.createObjectNode();
		
		try{
			
			reqNode = objectMapper.readTree(request);
			
			if(reqNode.get("ticketList").size() > 0){
				((ObjectNode) responseNode).put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_SUCCESS_RES_CODE);
				((ObjectNode) responseNode).put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.SUCCESS_MSG_VAL);
				((ObjectNode) responseNode).put(POSPServiceConstant.RES_DATA,reqNode);
			}else{
				((ObjectNode) responseNode).put(POSPServiceConstant.RES_CODE,POSPServiceConstant.NO_RECORD_RES_CODE);
				((ObjectNode) responseNode).put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.NO_RECORD_FOUND);
				((ObjectNode) responseNode).put(POSPServiceConstant.RES_DATA,reqNode);
			}
		}catch(Exception e){
			log.error("error while fetching get tickets :",e);
			responseNode = objectMapper.createObjectNode();
			((ObjectNode) responseNode).put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
			((ObjectNode) responseNode).put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
			((ObjectNode) responseNode).put(POSPServiceConstant.RES_DATA,POSPServiceConstant.TICKET_NOT_CREATED);
		}
		return responseNode;
	}
*/
	@Override
	public void process(Exchange exchange) throws Exception {
		ObjectNode responseNode = objectMapper.createObjectNode();
		try{
			JsonNode response = objectMapper.readTree(exchange.getIn().getBody(String.class));
		if(response.has("stage")){
			if(response.get("stage").asText().equalsIgnoreCase("created") || response.get("stage").asText().equalsIgnoreCase("updated")){
				responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_SUCCESS_RES_CODE);
				responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.SUCCESS_MSG_VAL);
				responseNode.put(POSPServiceConstant.RES_DATA,response);
			}else if(response.get("stage").asText().equalsIgnoreCase("read")){
				if(response.get("ticketList").size() > 0){
					responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_SUCCESS_RES_CODE);
					responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.SUCCESS_MSG_VAL);
					responseNode.put(POSPServiceConstant.RES_DATA,response);
					exchange.getIn().setBody(responseNode);
				}else{
					responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
					responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
					responseNode.put(POSPServiceConstant.RES_DATA,"");
					exchange.getIn().setBody(responseNode);
				}
			}else{
				responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
				responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
				responseNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.TICKET_NOT_CREATED);
			}
			((ObjectNode)response).remove("stage");
			exchange.getIn().setBody(responseNode);
		}else{
			responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
			responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
			
			responseNode.put(POSPServiceConstant.RES_DATA,"");
			exchange.getIn().setBody(responseNode);
		}
	}catch(Exception e){
		log.info("unable to send ticket procecss response : ",e);
		responseNode.put(POSPServiceConstant.RES_CODE,POSPServiceConstant.TICKET_FAILURE_RES_CODE);
		responseNode.put(POSPServiceConstant.RES_MSG ,POSPServiceConstant.ERROR_MSG_VAL);
		responseNode.put(POSPServiceConstant.RES_DATA,POSPServiceConstant.TICKET_NOT_CREATED);
		exchange.getIn().setBody(responseNode);
	}
	}
}
