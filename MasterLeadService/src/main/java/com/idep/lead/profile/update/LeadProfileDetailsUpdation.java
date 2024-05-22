package com.idep.lead.profile.update;

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

public class LeadProfileDetailsUpdation  implements Processor{

	CBService transService  = CBInstanceProvider.getPolicyTransInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LeadProfileDetailsUpdation.class.getName());
	
	public void process(Exchange exchange) {
		try {
			String request = exchange.getIn().getBody().toString();
			JsonNode inputRequest = objectMapper.readTree(request);
			JsonObject documentContent;
			log.info("inputRequest :"+inputRequest);
			
			//updating VehicleDetails doc
			if(inputRequest.has("carInfo") || inputRequest.has("bikeInfo")){
				log.info("inputRequest has carInfo or bikeInfo"+inputRequest);
				String messageId = exchange.getProperty("messageId").toString();
				log.info("messageId property :"+messageId);
				String docId = "VehicleDetails-"+messageId;
				log.info("docId :"+docId);
				JsonDocument vehicleDetailsDoc = transService.getDocBYId(docId);
				log.info("vehicleDetailsDoc :"+vehicleDetailsDoc);
				if(vehicleDetailsDoc != null){
					JsonNode vehicleDetails = objectMapper.readTree(vehicleDetailsDoc.content().toString());
					log.info("vehicleDetails content :"+vehicleDetails);
					if(inputRequest.has("carInfo") && inputRequest.get("carInfo").get("variantId") != null && !inputRequest.get("carInfo").get("variantId").asText().isEmpty()){
						log.info("inputRequest has carInfo :"+inputRequest);
						((ObjectNode)vehicleDetails).put("carInfo",inputRequest.get("carInfo"));
					}
					if(inputRequest.has("bikeInfo") && inputRequest.get("bikeInfo").get("variantId") != null  && !inputRequest.get("bikeInfo").get("variantId").asText().isEmpty()){
						log.info("inputRequest has bikeInfo :"+inputRequest);
						((ObjectNode)vehicleDetails).put("bikeInfo",inputRequest.get("bikeInfo"));
					}					
					documentContent = JsonObject.fromJson(vehicleDetails.toString());
					log.info("documentContent :"+documentContent);
					String doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
					
				}
				else{
					log.error("VehcileDetails doc not created, creating one :"+docId);
					((ObjectNode)inputRequest).put("requestSource","web");
					((ObjectNode)inputRequest).put("messageId",messageId);
					((ObjectNode)inputRequest).put("documentType","vehicleDetails");
					documentContent = JsonObject.fromJson(inputRequest.toString());
					String doc_status = transService.createDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
			}
			
			//updating FamilyDetails doc
			if(inputRequest.has("familyComp")){
				log.info("inputRequest has familyComp"+inputRequest);
				String messageId = exchange.getProperty("messageId").toString();
				log.info("messageId property :"+messageId);
				String docId = "FamilyDetails-"+messageId;
				log.info("docId :"+docId);
				JsonDocument familyDetailsDoc = transService.getDocBYId(docId);
				log.info("FamilyDetailsDoc :"+familyDetailsDoc);
				if(familyDetailsDoc != null){
					JsonNode familyDetails = objectMapper.readTree(familyDetailsDoc.content().toString());
					log.info("familyDetails content :"+familyDetails);
					((ObjectNode)familyDetails).put("familyComp",inputRequest.get("familyComp"));
					documentContent = JsonObject.fromJson(familyDetails.toString());
					log.info("documentContent :"+documentContent);
					String doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
					
				}
				else{
					log.error("FamilyDetails doc not created, creating one :"+docId);
					((ObjectNode)inputRequest).put("requestSource","web");
					((ObjectNode)inputRequest).put("messageId",messageId);
					((ObjectNode)inputRequest).put("documentType","familyDetails");
					documentContent = JsonObject.fromJson(inputRequest.toString());
					String doc_status = transService.createDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
			}
			if(inputRequest.has("commonInfo")){
				log.info("inputRequest has commonInfo :"+inputRequest);
				String messageId = exchange.getProperty("messageId").toString();
				log.info("messageId property :"+messageId);
				String docId = "PersonalDetails-"+messageId;
				log.info("docId :"+docId);
				JsonDocument personalDetailsDoc = transService.getDocBYId(docId);
				log.info("PersonalDetailsDoc :"+personalDetailsDoc);
				if(personalDetailsDoc != null){
					JsonNode persoanlDetails = objectMapper.readTree(personalDetailsDoc.content().toString());
					log.info("persoanlDetails content :"+persoanlDetails);
					((ObjectNode)persoanlDetails).put("commonInfo",inputRequest.get("commonInfo"));
					documentContent = JsonObject.fromJson(persoanlDetails.toString());
					log.info("documentContent :"+documentContent);
					String doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
					
				}
				else{
					log.error("PersonalDetails doc not created, creating one :"+docId);
					((ObjectNode)inputRequest).put("requestSource","web");
					((ObjectNode)inputRequest).put("messageId",messageId);
					((ObjectNode)inputRequest).put("documentType","personalDetails");
					documentContent = JsonObject.fromJson(inputRequest.toString());
					String doc_status = transService.createDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
			}

		} catch (Exception e) {
			log.error("docId updation message error : ",e);
		}
	}
}
