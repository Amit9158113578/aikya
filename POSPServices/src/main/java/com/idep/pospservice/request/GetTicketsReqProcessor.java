package com.idep.pospservice.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.pospservice.util.POSPServiceConstant;

public class GetTicketsReqProcessor implements Processor{
	static Logger log = Logger.getLogger(GetTicketsReqProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService pospBucket = CBInstanceProvider.getBucketInstance(POSPServiceConstant.POSP_BUCKET);
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static JsonNode ticketConfig = null;

	static {
		if(ticketConfig == null){
			try {
				ticketConfig = objectMapper.readTree(pospBucket.getDocBYId(POSPServiceConstant.POSP_TICKET_CONFIG).content().toString());
			} catch (IOException e) {
				log.error("error while fetching POSPTicketConfig",e);
			}
		}
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		JsonNode reqNode = objectMapper.readTree(exchange.getIn().getBody().toString());
		List<Map<String, Object>> ticketList = new ArrayList<Map<String,Object>>();
		String query = null;
		if(reqNode.get("paramMap").has("isAdmin") && reqNode.get("paramMap").get("isAdmin").asBoolean()){
			query = ticketConfig.get("GetTicketQueryConfig").get("query").get("byAdmin").asText();
		}else if(reqNode.findValue("dateWise")!=null && reqNode.findValue("dateWise").asText().equalsIgnoreCase("Yes")){
			
			query = ticketConfig.get("GetTicketQueryConfig").get("query").get("dateWise").asText();
			query = query.replace("<startDate>", reqNode.get("paramMap").get("startDate").asText());
			query = query.replace("<endDate>", reqNode.get("paramMap").get("endDate").asText());
			
			if(reqNode.findValue("userId")!=null){
				/*
				 * if request has userId then userId created ticket list sending in response
				 * **/
			String userId = 	reqNode.findValue("userId").asText();
				if(userId.length()>0){
					query = query+" and userId =  '"+userId+"'";
				}
			}
			
			
			log.info("query DateWise Ticket fetch : "+query);
		}else{
			query = ticketConfig.get("GetTicketQueryConfig").get("query").get("byUserId").asText();
			query = query.replace("<userId>", reqNode.get("paramMap").get("userId").asText());
		}
		log.info("prepared query :"+query);
		ticketList = pospBucket.executeQuery(query);		
		JsonNode jsonNode = objectMapper.convertValue(ticketList, JsonNode.class);
		ObjectNode node = objectMapper.createObjectNode();
		node.put("ticketList", jsonNode);
		node.put("stage", "read");
		exchange.getIn().setBody(node.toString());
	}

}
