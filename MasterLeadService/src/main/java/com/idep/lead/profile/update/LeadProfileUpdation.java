package com.idep.lead.profile.update;


import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;



public class LeadProfileUpdation  implements Processor{

	CBService service  = CBInstanceProvider.getServerConfigInstance();
	CBService transService  = CBInstanceProvider.getPolicyTransInstance();
	Logger log = Logger.getLogger(LeadProfileUpdation.class.getName());
	ObjectMapper objectMapper = new ObjectMapper(); 

	public void process(Exchange exchange) {
		try{
			String request = exchange.getIn().getBody().toString();
			JsonObject documentContent = null;
			JsonNode inputRequest = objectMapper.readTree(request);

			//to get the document LeadProfile-9565653244
			JsonNode headerNode = inputRequest.get("header");
			JsonNode leadMaintenanceStagesNode = objectMapper.readTree((service.getDocBYId("LeadMaintenanceStages").content()).toString());
			log.info("leadMaintenanceStagesNode :"+leadMaintenanceStagesNode);
			SimpleDateFormat sysDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");			
			if (leadMaintenanceStagesNode.get("servicesList").has(headerNode.get("transactionName").asText())){
				String proposalId = inputRequest.findValue("proposalId").asText();
				log.info("proposalId in request :"+proposalId);
				ArrayNode fieldsToStore = (ArrayNode)leadMaintenanceStagesNode.get("servicesList").get(headerNode.get("transactionName").asText()).get("fieldsToStore");
				for (int i = 0; i < fieldsToStore.size(); i++){
					String field = fieldsToStore.get(i).asText();
					JsonNode proposalIdDoc = objectMapper.readTree((transService.getDocBYId(proposalId).content()).toString());
					String mobileNo = proposalIdDoc.get("mobile").asText();
					String docId = "LeadProfile-"+mobileNo;
					JsonNode leadProfileDoc = objectMapper.readTree((transService.getDocBYId(docId).content()).toString());
					((ObjectNode)leadProfileDoc).put(field, proposalIdDoc.get(field).asText());
					JsonNode fields = objectMapper.createObjectNode();
					log.info("field :::"+field);
					if(proposalIdDoc.get("businessLineId").asInt() == 1){
						((ObjectNode)fields).put("Life",proposalIdDoc.get(field).asText());
						((ObjectNode)leadProfileDoc).put("latest"+field, proposalIdDoc.get(field).asText());
					}
					if(proposalIdDoc.get("businessLineId").asInt() == 2){
						((ObjectNode)fields).put("Bike",proposalIdDoc.get(field).asText());
						((ObjectNode)leadProfileDoc).put("latest"+field, proposalIdDoc.get(field).asText());
					}
					if(proposalIdDoc.get("businessLineId").asInt() == 3){
						((ObjectNode)fields).put("Car",proposalIdDoc.get(field).asText());
						((ObjectNode)leadProfileDoc).put("latest"+field, proposalIdDoc.get(field).asText());
					}
					if(proposalIdDoc.get("businessLineId").asInt() == 4){
						((ObjectNode)fields).put("Health",proposalIdDoc.get(field).asText());
						((ObjectNode)leadProfileDoc).put("latest"+field, proposalIdDoc.get(field).asText());
					}
					if(proposalIdDoc.get("businessLineId").asInt() == 5){
						((ObjectNode)fields).put("Travel",proposalIdDoc.get(field).asText());
						((ObjectNode)leadProfileDoc).put("latest"+field, proposalIdDoc.get(field).asText());
					}
					
					/*ArrayNode fieldNode  = (ArrayNode)defaultLeadProfile.get("field");
					for (int j = 0; j < fieldNode.size(); j++){
						String lob = fieldsToStore.get(j).asText();
						if( proposalIdDoc.get(field).asText().contains(lob)){
						}
					}*/
					
					((ObjectNode)leadProfileDoc).put(field, fields);
					((ObjectNode)leadProfileDoc).put("lastUpdatedDate", sysDateFormat.format(new Date()));
					
					documentContent  = JsonObject.fromJson(leadProfileDoc.toString());
					log.info("documentContent :"+documentContent);
					String doc_status = transService.replaceDocument(docId, documentContent);
					log.info("docId updation message :"+doc_status+", docId :"+docId);
				}
			}
		}
		catch(Exception e){
			log.error("Exception in LeadProfileUpdation :"+e);
		}
	}
}

