package com.idep.lead.profile.update;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugar.util.SugarCRMConstants;

public class LeadProfileQuoteUpdation implements Processor{
	CBService transService  = CBInstanceProvider.getPolicyTransInstance();
	CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
	Logger log = Logger.getLogger(LeadProfileQuoteUpdation.class.getName());
	SimpleDateFormat sysDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	ObjectMapper objectMapper = new ObjectMapper(); 
	public void process(Exchange exchange) {
		try {
			String request = exchange.getIn().getBody().toString();
			JsonNode inputRequest = objectMapper.readTree(request);
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String quoteId = inputRequest.get(SugarCRMConstants.QUOTE_ID).asText();
			log.info("quoteId :"+quoteId);
			JsonDocument quoteIdDoc = quoteData.getDocBYId(quoteId);
			log.info("quoteIdDoc :"+quoteIdDoc);
			JsonNode quoteDataDoc = objectMapper.createObjectNode();
			if(quoteIdDoc == null ){
				log.info("quoteIdDoc in if condition:"+quoteIdDoc);
				Thread.sleep(8000);
				quoteIdDoc = quoteData.getDocBYId(quoteId);
				for(int i=0;i<20;i++){
					if(quoteIdDoc!=null && quoteIdDoc.content() != null){
						quoteDataDoc = objectMapper.readTree(quoteIdDoc.content().toString());
						if(quoteDataDoc !=null && quoteDataDoc.findValue("messageId") == null){
							Thread.sleep(5000);
							quoteIdDoc = quoteData.getDocBYId(quoteId);
							if(quoteIdDoc.content()!=null){
								quoteDataDoc = objectMapper.readTree(quoteIdDoc.content().toString());
								if(quoteDataDoc !=null && quoteDataDoc.findValue("messageId") != null){
									break;
								}
							}
						}
						else{
							break;
						}
					}
					else{
						Thread.sleep(5000);
						quoteIdDoc = quoteData.getDocBYId(quoteId);
					}
				}
			}
			else{
				log.info("quoteIdDoc in else condition:"+quoteIdDoc);
				for(int i=0;i<20;i++){
					if(quoteIdDoc!=null && quoteIdDoc.content() != null){
						quoteDataDoc = objectMapper.readTree(quoteIdDoc.content().toString());
						if(quoteDataDoc !=null && quoteDataDoc.findValue("messageId") == null){
							Thread.sleep(5000);
							quoteIdDoc = quoteData.getDocBYId(quoteId);
							if(quoteIdDoc.content()!=null){
								quoteDataDoc = objectMapper.readTree(quoteIdDoc.content().toString());
								if(quoteDataDoc !=null && quoteDataDoc.findValue("messageId") != null){
									break;
								}
							}
						}
						else{
							break;
						}
					}
					else{
						Thread.sleep(5000);
						quoteIdDoc = quoteData.getDocBYId(quoteId);
					}
				}
			}
			log.info("quoteDataDoc condition:"+quoteDataDoc);
			if(quoteDataDoc != null && quoteDataDoc.findValue("messageId") != null && !quoteDataDoc.findValue("messageId").asText().isEmpty()){
				String messageId = null;
				messageId = quoteDataDoc.findValue("messageId").asText();
				exchange.setProperty("messageId", messageId);
				String docId = "LeadProfile-"+messageId;
				log.info(" LeadProfile doc : :"+docId);
				JsonNode leadProfileDoc = objectMapper.readTree((transService.getDocBYId(docId).content()).toString());
				log.info("leadProfileDoc :"+leadProfileDoc);
				log.info("inputRequest ::"+inputRequest);
				if(inputRequest.get("businessLineId").asInt() == 1){
					log.info("inputRequest :"+inputRequest);
					log.info("LOB is 1 ");
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.QUOTE_ID).get(0)).put("Life",inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_ID, inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_LOB,"1");
					((ObjectNode)inputRequest).put("quoteRequest", quoteDataDoc.get("lifeQuoteRequest"));
					exchange.getIn().setHeader("documentId", "Policies365-PersonalDetails");
					((ObjectNode)inputRequest).put("requestType", "PersonalDetails");
					exchange.getIn().setBody(inputRequest);
				}
				else if(inputRequest.get("businessLineId").asInt() == 2){
					log.info("LOB is 2 ");
					log.info("inputRequest :"+inputRequest);
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.QUOTE_ID).get(1)).put("Bike",inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_ID, inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_LOB,"2");
					((ObjectNode)inputRequest).put("bikeQuoteRequest", quoteDataDoc.get("bikeQuoteRequest"));
					((ObjectNode)inputRequest.get("bikeQuoteRequest")).remove("requestType");
					((ObjectNode)inputRequest).put("requestType", "VehicleDetails");
					exchange.getIn().setHeader("documentId", "Policies365-VehicleDetails");
					exchange.getIn().setBody(inputRequest);
					log.info("inputRequest value:"+inputRequest);
				}
				else if(inputRequest.get("businessLineId").asInt() == 3){
					log.info("LOB is 3 ");
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.QUOTE_ID).get(2)).put("Car",inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_ID, inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_LOB,"3");
					((ObjectNode)inputRequest).put("carQuoteRequest", quoteDataDoc.get("carQuoteRequest"));
					((ObjectNode)inputRequest.get("carQuoteRequest")).remove("requestType");
					((ObjectNode)inputRequest).put("requestType", "VehicleDetails");
					exchange.getIn().setHeader("documentId", "Policies365-VehicleDetails");
					exchange.getIn().setBody(inputRequest);
					log.info("inputRequest value:"+inputRequest);

				}
				else if(inputRequest.get("businessLineId").asInt() == 4){
					log.info("LOB is 4 "+leadProfileDoc);
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.QUOTE_ID).get(3)).put("Health",inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_ID, inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_LOB,"4");
					exchange.getIn().setHeader("documentId", "Policies365-FamilyDetails");
					((ObjectNode)inputRequest).put("quoteRequest", quoteDataDoc.get("quoteRequest"));
					((ObjectNode)inputRequest.get("quoteRequest")).remove("requestType");
					((ObjectNode)inputRequest).put("requestType", "FamilyDetails");
					exchange.getIn().setBody(inputRequest);
					log.info("inputRequest value:"+inputRequest);

				}
				else if(inputRequest.get("businessLineId").asInt() == 5){
					((ObjectNode)leadProfileDoc.get(SugarCRMConstants.QUOTE_ID).get(4)).put("Travel",inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_ID, inputRequest.get(SugarCRMConstants.QUOTE_ID).asText());
					((ObjectNode)leadProfileDoc).put(SugarCRMConstants.LATEST_QUOTE_LOB,"5");
				}
				((ObjectNode)leadProfileDoc).put("lastUpdatedDate", sysDateFormat.format(new Date()));
				if(!leadProfileDoc.has("leadStage")){
					((ObjectNode)leadProfileDoc).put("leadStage", "QUOTE");
				}
				JsonObject documentContent  = JsonObject.fromJson(leadProfileDoc.toString());
				log.info("documentContent :"+documentContent);
				String uri = "activemq:queue:POSPRequestQ";
				template.sendBody(uri, documentContent.toString());
				log.info("Request String sent to POSPRequestQ from Quote");
				String doc_status = transService.replaceDocument(docId, documentContent);
				log.info("docId updation message :"+doc_status+", docId :"+docId);
				exchange.getIn().setHeader("reqFlag", "True");
			}
			else{
				log.info("quoteId Doc does not have messageId :"+quoteId);
			}
		} catch (Exception e) {
			log.error("docId updation message error : ",e);
		}
	}
}
