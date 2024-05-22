package com.idep.lead.profile.update;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.idep.sugar.util.SugarCRMConstants;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class LeadProfileProposalUpdation implements Processor{

	CBService transService  = CBInstanceProvider.getPolicyTransInstance();
	CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	Logger log = Logger.getLogger(LeadProfileProposalUpdation.class.getName());
	SimpleDateFormat sysDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	ObjectMapper objectMapper = new ObjectMapper(); 
	String quoteId=null;
	public void process(Exchange exchange) {
		try {
			String request = exchange.getIn().getBody().toString();
			JsonNode inputRequest = objectMapper.readTree(request);
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String proposalId = inputRequest.findValue("proposalId").asText();
			String messageId = null;
			if(inputRequest.has("requestSource") && inputRequest.get("requestSource").asText().equalsIgnoreCase("agency")){
				String uri = "activemq:queue:POSPRequestQ";
				exchange.setPattern(ExchangePattern.InOnly);
		        template.send(uri, exchange);
		        log.info("Request sent to POSPRequestQ from Proposal");
			}
			if(inputRequest.has("messageId")){
				messageId = inputRequest.get("messageId").asText();
			}
			else{
				quoteId = inputRequest.get("QUOTE_ID").asText();
				JsonNode quoteIdDoc = objectMapper.readTree((quoteData.getDocBYId(quoteId).content()).toString());
				if(quoteIdDoc.findValue("messageId") != null){
					messageId = quoteIdDoc.findValue("messageId").asText();
				}
			}
			//setting documentId and requestType for mapper
			if(messageId != null && !messageId.isEmpty()){
				exchange.setProperty("messageId", messageId);
				String docId = "LeadProfile-"+messageId;
				log.info("LeadProfile doc present :"+docId);
				JsonNode leadProfileDoc = objectMapper.readTree((transService.getDocBYId(docId).content()).toString());
				log.info("leadProfileDoc :"+leadProfileDoc);
				if(inputRequest.has("businessLineId") && inputRequest.get("businessLineId").asInt() == 1){
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.PROPOSAL_ID).get(0)).put("Life",proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_ID, proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_LOB,"1");
					((ObjectNode)inputRequest).remove("requestType");
					((ObjectNode)inputRequest).put("address", inputRequest.get("vehicleDetails").get("registrationAddress"));
					((ObjectNode)inputRequest).put("requestType", "AddressDetails");
					exchange.getIn().setHeader("documentId", "Policies365-AddressDetails");
					exchange.getIn().setBody(inputRequest);
				}
				else if(inputRequest.has("businessLineId") && inputRequest.get("businessLineId").asInt() == 2){
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.PROPOSAL_ID).get(1)).put("Bike",proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_ID, proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_LOB,"2");
					if(inputRequest.has("vehicleDetails") && inputRequest.get("vehicleDetails") != null && inputRequest.get("vehicleDetails").has("registrationAddress")){
						((ObjectNode)inputRequest).remove("requestType");
						((ObjectNode)inputRequest).put("address", inputRequest.get("vehicleDetails").get("registrationAddress"));
						((ObjectNode)inputRequest).put("requestType", "AddressDetails");
					}
					
					exchange.getIn().setHeader("documentId", "Policies365-AddressDetails");
					exchange.getIn().setBody(inputRequest);
				}
				else if(inputRequest.has("businessLineId") && inputRequest.get("businessLineId").asInt() == 3){
					log.info("LOB is 3 :"+inputRequest);
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.PROPOSAL_ID).get(2)).put("Car",proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_ID, proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_LOB,"3");
					if(inputRequest.has("vehicleDetails") && inputRequest.get("vehicleDetails") != null && inputRequest.get("vehicleDetails").has("permanentAddress")){
						((ObjectNode)inputRequest).remove("requestType");
						((ObjectNode)inputRequest).put("address", inputRequest.get("vehicleDetails").get("permanentAddress"));
						((ObjectNode)inputRequest).put("requestType", "AddressDetails");
						exchange.getIn().setHeader("documentId", "Policies365-AddressDetails");
					}
					exchange.getIn().setHeader("documentId", "Policies365-AddressDetails");
					exchange.getIn().setBody(inputRequest);
				}
				else if(inputRequest.has("businessLineId") && inputRequest.get("businessLineId").asInt() == 4){
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.PROPOSAL_ID).get(3)).put("Health",proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_ID, proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_LOB,"4");
					((ObjectNode)inputRequest).remove("requestType");
					if(inputRequest.has("proposerInfo") && inputRequest.get("proposerInfo") != null && inputRequest.get("proposerInfo").has("permanentAddress")){
						((ObjectNode)inputRequest).put("address", inputRequest.get("proposerInfo").get("permanentAddress"));
					}
					((ObjectNode)inputRequest).put("requestType", "AddressDetails");
					exchange.getIn().setHeader("documentId", "Policies365-AddressDetails");
					exchange.getIn().setBody(inputRequest);
				}
				else if(inputRequest.has("businessLineId") && inputRequest.get("businessLineId").asInt() == 5){
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.PROPOSAL_ID).get(4)).put("Travel",proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_ID, proposalId);
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_PROPOSAL_LOB,"5");
					((ObjectNode)inputRequest).remove("requestType");
					((ObjectNode)inputRequest).put("requestType", "AddressDetails");
					exchange.getIn().setHeader("documentId", "Policies365-AddressDetails");
					exchange.getIn().setBody(inputRequest);
				}

				((ObjectNode)leadProfileDoc).put("lastUpdatedDate", sysDateFormat.format(new Date()));
				((ObjectNode)leadProfileDoc).put("leadStage", "PAYINIT");
				JsonObject documentContent  = JsonObject.fromJson(leadProfileDoc.toString());
				log.info("documentContent :"+documentContent);
				if(inputRequest.has("requestSource") && inputRequest.get("requestSource").asText().equalsIgnoreCase("posp")){
					String uri = "activemq:queue:POSPRequestQ";
					exchange.setPattern(ExchangePattern.InOnly);
			        template.sendBody(uri, documentContent.toString());
			        log.info("Request sent to POSPRequestQ from Proposal");
				}
				String doc_status = transService.replaceDocument(docId, documentContent);
				log.info("docId updation message :"+doc_status+", docId :"+docId);
				exchange.getIn().setHeader("reqFlag", "True");
			}
			else{
				log.info("quoteId Doc does not have messageId in updating proposalId:"+quoteId);
			}
		} catch (Exception e) {
			log.error("docId updation message error : ",e);
		}
	}
}
