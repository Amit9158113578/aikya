package com.idep.pospservice.response;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.services.impl.UploadDocument;

public class GeneratePospDocDownloadUrl implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(GeneratePospDocDownloadUrl.class.getName());
	CBService PospData = CBInstanceProvider.getBucketInstance("PospData");
	
	@Override
	public void process(Exchange exchange) throws Exception {
		JsonNode request  = objectMapper.readTree(exchange.getIn().getBody(String.class));
		try{
			JsonNode ConfigDoc =null;
			String ticketId=null;
			
			if(PospData!=null){
				ConfigDoc= objectMapper.readTree(PospData.getDocBYId("PospContentManagementConfig").content().toString());
			}else{
				PospData = CBInstanceProvider.getBucketInstance("PospData");
				ConfigDoc= objectMapper.readTree(PospData.getDocBYId("PospContentManagementConfig").content().toString());
			}
			
			if(ConfigDoc.has("loginURL")){
				ticketId=generatePOSPDocAuthTicket(ConfigDoc.get("loginURL").asText());
			}
			if(ticketId!=null){
				if(request.has("agentId") && request.has("mobileNumber")){
					if(request.has("docDownloadUrl")){
						String downloadUrl = request.get("docDownloadUrl").asText().concat(ticketId);
						((ObjectNode)request).put("docDownloadUrl",downloadUrl );
					}else if(request.has("documentDownloadUrl")){
						String downloadUrl = request.get("documentDownloadUrl").asText().concat(ticketId);
						((ObjectNode)request).put("documentDownloadUrl",downloadUrl );
					}else{
						log.error("Document url not found in request "+request);
					}
				}else{
					log.error("Request in mobileNumber and agentId not found : "+request);
				}
			}else{
				log.error("ticket id not generted for  POSP document url : "+request);
			}
		}catch(Exception e){
			log.error("ticket id not generted for  POSP document url : ",e);
		}
		exchange.getIn().setBody(request);
	}

	
	public String generatePOSPDocAuthTicket(String url) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		GetMethod getAuth = new GetMethod(url);
		client.executeMethod(getAuth);
		String ticketId = getAuth.getResponseBodyAsString();
		ticketId = ticketId.substring(ticketId.indexOf("<ticket>")+8, ticketId.indexOf("</ticket>"));
		log.info("TicketID : "+ticketId);
		if(!ticketId.equals(null))
		{
			return ticketId;
		}
		else
		{
			return ticketId;
		}
	}
	
	
	
}
