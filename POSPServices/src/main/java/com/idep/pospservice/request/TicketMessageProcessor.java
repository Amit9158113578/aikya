package com.idep.pospservice.request;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.pospservice.util.Functions;
import com.idep.pospservice.util.POSPServiceConstant;


public class TicketMessageProcessor implements Processor
{
	static Logger log = Logger.getLogger(TicketMessageProcessor.class.getName());
	static ObjectMapper objectMapper= new ObjectMapper();
	static CBService pospBucket= CBInstanceProvider.getBucketInstance("PospData");
	static JsonNode ticketConfigNode = null; 
	JsonNode leadConfigNode = null;

	static{
		if(ticketConfigNode == null){
			try {
				ticketConfigNode = objectMapper.readTree(pospBucket.getDocBYId(POSPServiceConstant.POSP_TICKET_CONFIG).content().toString());
			} catch (JsonProcessingException e) {
				log.error("unable to fetch document "+POSPServiceConstant.POSP_TICKET_CONFIG,e);
			} catch (IOException e) {
				log.error("unable to fetch document "+POSPServiceConstant.POSP_TICKET_CONFIG,e);
			}
		} 
	}

	public void process(Exchange exchange) throws Exception {
		
		try {
			ObjectNode ticketData = objectMapper.createObjectNode();
			String request=exchange.getIn().getBody().toString();
			JsonNode reqNode=objectMapper.readTree(request);
			
			if(reqNode.has("paramMap") && reqNode.get("paramMap") != null){
				ticketData = Functions.filterMapData(ticketData,reqNode, ticketConfigNode.get("paramMapConfig"));				
			}
			if(reqNode != null){
				ticketData = Functions.filterMapData(ticketData,reqNode, ticketConfigNode.get("rootNodeMapConfig"));				
			}
			/*Map<String,String> rootNodeMap = objectMapper.readValue(ticketConfigNode.get("rootNodeMapConfig").toString(),Map.class);
			for(Map.Entry<String, String> field : rootNodeMap.entrySet()){
				ticketData.put(field.getValue(), reqNode.get(field.getKey()));
			}*/
			ArrayNode disscussionList = objectMapper.createArrayNode();
			JsonNode msg = objectMapper.createObjectNode();
			((ObjectNode)msg).put("message", ticketData.get("description").asText());
			((ObjectNode)msg).put("seqId", 1);
			((ObjectNode)msg).put("updatedBy", reqNode.get("id").asText());
			disscussionList.add(msg);
			ticketData.put("description", disscussionList);
			
			ticketData.put("isActive", "Yes");
			ticketData.put("documentType", POSPServiceConstant.TICKET_DOC_TYPE);
			ticketData.put("status", POSPServiceConstant.NEW_TICKET_STATUS);
			exchange.getIn().setBody(objectMapper.writeValueAsString(ticketData));
			
		} catch (Exception e) {
			log.error("Error while preparing a message for posp Ticket ",e);
		}	
	}
}
