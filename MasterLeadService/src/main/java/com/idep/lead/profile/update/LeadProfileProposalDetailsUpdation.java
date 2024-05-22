package com.idep.lead.profile.update;

import java.text.SimpleDateFormat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class LeadProfileProposalDetailsUpdation implements Processor{

	CBService transService  = CBInstanceProvider.getPolicyTransInstance();
	CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	Logger log = Logger.getLogger(LeadProfileProposalDetailsUpdation.class.getName());
	SimpleDateFormat sysDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	ObjectMapper objectMapper = new ObjectMapper(); 
	JsonObject documentContent;
	public void process(Exchange exchange) {
		try {
			String request = exchange.getIn().getBody().toString();
			JsonNode inputRequest = objectMapper.readTree(request);

			log.info("storing address request"+inputRequest);
			String messageId = exchange.getProperty("messageId").toString();
			log.info("messageId property :"+messageId);
			String docId = "AddressDetails-"+messageId;
			log.info("docId :"+docId);
			JsonDocument addressDetailsDoc = transService.getDocBYId(docId);
			log.info("addressDetailsDoc :"+addressDetailsDoc);
			if(addressDetailsDoc != null){
				JsonNode addressDetails = objectMapper.readTree(addressDetailsDoc.content().toString());
				log.info("addressDetails content :"+addressDetails);
				if(inputRequest.has("address") && inputRequest.get("address") != null){
					log.info("inputRequest has inputRequest :"+inputRequest);
					((ObjectNode)addressDetails).put("address",inputRequest.get("address"));
				}

				documentContent = JsonObject.fromJson(addressDetails.toString());
				log.info("documentContent :"+documentContent);
				String doc_status = transService.replaceDocument(docId, documentContent);
				log.info("docId updation message :"+doc_status+", docId :"+docId);

			}
			else{
				log.error("AddressDetails doc not created, creating one :"+docId);
				((ObjectNode)inputRequest).put("requestSource","web");
				((ObjectNode)inputRequest).put("messageId",messageId);
				((ObjectNode)inputRequest).put("documentType","addressDetails");
				documentContent = JsonObject.fromJson(inputRequest.toString());
				String doc_status = transService.createDocument(docId, documentContent);
				log.info("docId updation message :"+doc_status+", docId :"+docId);
			}
		}		
		catch (Exception e) {
			log.error("docId updation message error : ",e);
		}
	}
}
