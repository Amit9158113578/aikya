package com.idep.popsp.ticket.dbstore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.pospservice.util.ExecutionTerminator;


public class UpdatePospTicket implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UpdatePospTicket.class.getName());
	static CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class)); 
			
			
			if(request.has("ticketId")){
				
				JsonDocument tickectDoc = PospData.getDocBYId(request.get("ticketId").asText());
				if(tickectDoc!=null){
					JsonNode ticketNode = objectMapper.readTree(tickectDoc.content().toString());
					ArrayNode disscussion = (ArrayNode) ticketNode.get("description");
					JsonNode msg = objectMapper.createObjectNode();
					((ObjectNode)msg).put("message", request.get("description").asText());
					((ObjectNode)msg).put("seqId", (disscussion.size()+1));
					((ObjectNode)msg).put("updatedBy", request.get("id").asText());
					disscussion.add(msg);
					
					((ObjectNode)ticketNode).put("description", disscussion);
					/**
					 * userId is always agentId 
					 * **/
					if(request.findValue("status")!=null){
						((ObjectNode)ticketNode).put("status", request.findValue("status").asText());	
						if(request.findValue("status").asText().equalsIgnoreCase("closed")){
							((ObjectNode)ticketNode).put("isActive", "No");	
						}else{
							((ObjectNode)ticketNode).put("isActive", "Yes");
						}
						
					}
					
					((ObjectNode)ticketNode).put("userId", ticketNode.get("userId").asText());
					((ObjectNode)ticketNode).put("updatedBy", request.get("id").asText());
					((ObjectNode)ticketNode).put("lastUpdated", dateFormat.format(new Date()));
					
					String doc_status=PospData.replaceDocument(request.get("ticketId").asText(), JsonObject.fromJson(objectMapper.writeValueAsString(ticketNode)));
					ObjectNode resNode = objectMapper.createObjectNode();
					  resNode.put("status", doc_status);
				      resNode.put("stage", "updated");
				      resNode.put("ticketId", request.get("ticketId").asText());
				      exchange.getIn().setBody(resNode);
				      
				}else{
					log.error("Unable to load Posp Ticket document from DB :  "+request);	
					throw new ExecutionTerminator();
				}
			}else{
				log.error("Unable to update Posp Ticket Id not found in request : "+request);	
				throw new ExecutionTerminator();
			}
		}catch(Exception e){
			log.error("Unable to update Posp Ticket : ",e);
			throw new ExecutionTerminator();
		}
	}

}
