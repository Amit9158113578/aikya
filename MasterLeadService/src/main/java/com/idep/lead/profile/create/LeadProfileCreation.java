package com.idep.lead.profile.create;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LeadProfileCreation implements Processor{
	CBService transService  = CBInstanceProvider.getPolicyTransInstance();
	CBService quoteData  = CBInstanceProvider.getBucketInstance("QuoteData");
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	Logger log = Logger.getLogger(LeadProfileCreation.class.getName());
	SimpleDateFormat sysDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	ObjectMapper objectMapper = new ObjectMapper(); 
	String mobile;
	String quoteId;
	public void process(Exchange exchange)
	{
		try{
			
			String request = exchange.getIn().getBody().toString();
			JsonObject documentContent = null;
			JsonNode inputRequest = objectMapper.readTree(request);
			((ObjectNode)inputRequest).put("docCreatedDate", sysDateFormat.format(new Date()));
			String messageId = inputRequest.get("messageId").asText();
			String docId = "LeadProfile-"+messageId;
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put("firstActivity", inputRequest);
			objectNode.put("lastActivity", inputRequest);
			ObjectNode objectNode2 = this.objectMapper.createObjectNode();
			objectNode2.put("LeadDetails",objectNode);
			objectNode2.put("documentType", "leadProfileRequest");
			objectNode2.put("requestSource", inputRequest.get("requestSource"));
			/*if(inputRequest.get("requestSource").asText().equalsIgnoreCase("agency")){
				objectNode2.put("agencyId", inputRequest.get("agencyId"));
				objectNode2.put("userName", inputRequest.get("userName"));
			}
			*/
			if(inputRequest.has("userName"))
			{
				objectNode2.put("userName", inputRequest.get("userName"));
			}
			if(inputRequest.has("agencyId"))
			{
				objectNode2.put("agencyId", inputRequest.get("agencyId"));
			}
			if(inputRequest.has("leadStatus") && inputRequest.get("leadStatus") != null)
			 {
				objectNode2.put("leadStatus", inputRequest.get("leadStatus"));
			 }
			objectNode2.put("mobileNumber",inputRequest.get("contactInfo").get("mobileNumber").asText());
			objectNode2.put("lastUpdatedDate", sysDateFormat.format(new Date()));
			JsonNode defaultLeadProfileDoc = objectMapper.readTree((serverConfig.getDocBYId("DefaultLeadProfile").content()).toString());			
			objectNode2.put("QUOTE_ID", defaultLeadProfileDoc.get("QUOTE_ID")); 
			objectNode2.put("proposalId", defaultLeadProfileDoc.get("proposalId")); 
			if(inputRequest.has("messageId")){
				 log.info("inputRequest has messageId");
				 objectNode2.put("messageId", inputRequest.get("messageId")); 
			 }
			if(inputRequest.has("utm_medium")){
				 log.info("inputRequest has utm_medium");
				 objectNode2.put("affiliateId", inputRequest.get("utm_medium")); 
			 }
			if(inputRequest.has("utm_source")){
				 log.info("inputRequest has utm_source");
				 objectNode2.put("utm_source", inputRequest.get("utm_source")); 
			 }
			if(inputRequest.has("businessLineId")){
				 log.info("inputRequest has businessLineId");
				 objectNode2.put("latestQuoteBusinessLineId", inputRequest.get("businessLineId")); 
			 }
			 else if(inputRequest.has("contactInfo")&& inputRequest.get("contactInfo").has("messageId")){
				 log.info("inputRequest has contactInfo which has messageId");
				 objectNode2.put("messageId", inputRequest.get("contactInfo").get("messageId")); 
			 }
			documentContent = JsonObject.fromJson(objectNode2.toString());
			log.info("documentContent to store in first activity :"+documentContent);
			String doc_status = transService.createDocument(docId, documentContent);
			log.info("docId creation message :"+doc_status+" for docId :"+docId); 
			if(doc_status.equalsIgnoreCase("doc_exist")){
				log.info("Replacing document.");
				JsonNode leadProfileDoc = objectMapper.readTree((transService.getDocBYId(docId).content()).toString());
				((ObjectNode)leadProfileDoc.get("LeadDetails")).put("lastActivity", inputRequest);
				((ObjectNode)leadProfileDoc).put("lastUpdatedDate", sysDateFormat.format(new Date()));
				((ObjectNode)leadProfileDoc).put("documentType", "leadProfileRequest");
				((ObjectNode)leadProfileDoc).put("mobileNumber",inputRequest.get("contactInfo").get("mobileNumber").asText());
				if(!leadProfileDoc.has("QUOTE_ID")){
					((ObjectNode)leadProfileDoc).put("QUOTE_ID", defaultLeadProfileDoc.get("QUOTE_ID"));
				}
				if(!leadProfileDoc.has("proposalId")){
					((ObjectNode)leadProfileDoc).put("proposalId", defaultLeadProfileDoc.get("proposalId"));
				}
				if(inputRequest.has("utm_medium")){
					 log.info("inputRequest has utm_medium");
					 ((ObjectNode)leadProfileDoc).put("affiliateId", inputRequest.get("utm_medium")); 
				 }
				if(inputRequest.has("utm_source")){
					 log.info("inputRequest has utm_source");
					 ((ObjectNode)leadProfileDoc).put("utm_source", inputRequest.get("utm_source")); 
				 }
				 if(inputRequest.has("messageId")){
					 log.info("inputRequest has messageId");
					 ((ObjectNode)leadProfileDoc).put("messageId", inputRequest.get("messageId")); 
				 }
				 else if(inputRequest.has("contactInfo")&& inputRequest.get("contactInfo").has("messageId")){
					 log.info("inputRequest has contactInfo which has messageId");
					 ((ObjectNode)leadProfileDoc).put("messageId", inputRequest.get("contactInfo").get("messageId")); 
				 }
				 if(inputRequest.has("userName"))
				 {
					 ((ObjectNode)leadProfileDoc).put("userName", inputRequest.get("userName"));
				 }
				 if(inputRequest.has("agencyId"))
				 {
					 ((ObjectNode)leadProfileDoc).put("agencyId", inputRequest.get("agencyId"));
				 }
				 if(inputRequest.has("leadStatus") && inputRequest.get("leadStatus") != null)
				 {
					 ((ObjectNode)leadProfileDoc).put("leadStatus", inputRequest.get("leadStatus"));
				 }
				documentContent  = JsonObject.fromJson(leadProfileDoc.toString());
				log.info("documentContent to store in last activity :"+documentContent);
				doc_status = transService.replaceDocument(docId, documentContent);
				log.info("docId updation message :"+doc_status+", docId :"+docId);
				
			}
			if(inputRequest.has("isProfessionalJourney") && inputRequest.get("isProfessionalJourney").asText().equalsIgnoreCase("true")){
				
				log.info("isProfessionalJourney is true :"+inputRequest);
				//creating VehicleDetails doc
				docId = "VehicleDetails-"+messageId;
				JsonNode vehicleDetails=objectMapper.createObjectNode();
				
				//adding carrierVariantDisplayName in response for and bike
				if(inputRequest.has("carInfo") && inputRequest.get("carInfo") != null  && !inputRequest.get("carInfo").toString().isEmpty()){
				((ObjectNode)vehicleDetails).put("carInfo",inputRequest.get("carInfo") );
				String mobileCarVarientId = serverConfig.getDocBYId("Mobile"+inputRequest.get("carInfo").get("variantId").asText()).content().toString();
				JsonNode mobileCarVarientIdDoc = objectMapper.readTree(mobileCarVarientId);
				String carCarrierVariantDisplayName = mobileCarVarientIdDoc.findValue("displayVehicle").asText();
				((ObjectNode)vehicleDetails.get("carInfo")).put("carrierVariantDisplayName", carCarrierVariantDisplayName);
				}
				if(inputRequest.has("bikeInfo") && inputRequest.get("bikeInfo") != null && !inputRequest.get("bikeInfo").toString().isEmpty()){
					((ObjectNode)vehicleDetails ).put("bikeInfo",inputRequest.get("bikeInfo"));
					String mobileBikeVarientId = serverConfig.getDocBYId("Mobile"+inputRequest.get("bikeInfo").get("variantId").asText()).content().toString();
					JsonNode mobileBikeVarientIdDoc = objectMapper.readTree(mobileBikeVarientId);
					String bikeCarrierVariantDisplayName = mobileBikeVarientIdDoc.findValue("displayVehicle").asText();				
					((ObjectNode)vehicleDetails.get("bikeInfo")).put("carrierVariantDisplayName", bikeCarrierVariantDisplayName);
				}
				
				((ObjectNode)vehicleDetails).put("requestSource","web");
				((ObjectNode)vehicleDetails ).put("messageId",messageId);
				((ObjectNode)vehicleDetails ).put("documentType","vehicleDetails");
				documentContent  = JsonObject.fromJson(vehicleDetails.toString());
				log.info("documentContent to store in VehicleDetails :"+documentContent);
				doc_status = transService.createDocument(docId, documentContent);
				log.info("docId creation message :"+doc_status+", docId :"+docId);
				if(doc_status.equalsIgnoreCase("doc_exist")){
					doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
				
				//creating AddressDetails doc
				docId = "AddressDetails-"+messageId;
				JsonNode addressDetails=objectMapper.createObjectNode();
				((ObjectNode)addressDetails).put("address",inputRequest.get("commonInfo").get("address"));
				((ObjectNode)addressDetails).put("requestSource","web");
				((ObjectNode)addressDetails).put("messageId",messageId);
				((ObjectNode)addressDetails).put("documentType","addressDetails");
				documentContent  = JsonObject.fromJson(addressDetails.toString());
				log.info("documentContent to store in AddressDetails :"+documentContent);
				doc_status = transService.createDocument(docId, documentContent);
				log.info("docId creation message :"+doc_status+", docId :"+docId);
				if(doc_status.equalsIgnoreCase("doc_exist")){
					doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
				
				//creating FamilyDetails doc
				docId = "FamilyDetails-"+messageId;
				JsonNode familyDetails=objectMapper.createObjectNode();
				((ObjectNode)familyDetails).put("familyComp",inputRequest.get("commonInfo").get("familyComp"));
				((ObjectNode)familyDetails).put("requestSource","web");
				((ObjectNode)familyDetails).put("messageId",messageId);
				((ObjectNode)familyDetails).put("documentType","familyDetails");
				documentContent  = JsonObject.fromJson(familyDetails.toString());
				log.info("documentContent to store in familyDetails :"+documentContent);
				doc_status = transService.createDocument(docId, documentContent);
				log.info("docId creation message :"+doc_status+", docId :"+docId);
				if(doc_status.equalsIgnoreCase("doc_exist")){
					doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
				log.info("inputRequest before removing details :"+inputRequest);
				((ObjectNode)inputRequest).remove("carInfo");
				((ObjectNode)inputRequest).remove("bikeInfo");
				((ObjectNode)inputRequest.get("commonInfo")).remove("familyComp");
				((ObjectNode)inputRequest.get("commonInfo")).remove("address");
				log.info("inputRequest after removing details :"+inputRequest);
				
				//creating PersonalDetails doc
				docId = "PersonalDetails-"+messageId;
				JsonNode personalDetails=objectMapper.createObjectNode();
				((ObjectNode)personalDetails).put("commonInfo",inputRequest.get("commonInfo"));
				((ObjectNode)personalDetails).put("requestSource","web");
				
				JsonNode profession=objectMapper.createObjectNode();
				((ObjectNode)profession).put("professionId",inputRequest.get("professionId"));
				((ObjectNode)profession).put("professionName",inputRequest.get("profession"));
				((ObjectNode)profession).put("professionCode",inputRequest.get("professionCode"));
				((ObjectNode)personalDetails).put("profession",profession);
				((ObjectNode)personalDetails).put("messageId",messageId);
				((ObjectNode)personalDetails).put("documentType","personalDetails");
				documentContent  = JsonObject.fromJson(personalDetails.toString());
				log.info("documentContent to store in personalDetails :"+documentContent);
				doc_status = transService.createDocument(docId, documentContent);
				log.info("docId creation message :"+doc_status+", docId :"+docId);
				if(doc_status.equalsIgnoreCase("doc_exist")){
					doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
			}
			
		}
		catch(Exception e){
			log.error("Exception while LeadProfileCreation : ", e);
		}
	}
}