package com.idep.leadutility.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class LeadRequestPrepare implements Processor {

	Logger log = Logger.getLogger(LeadRequestPrepare.class.getName());
	LeadFormDataProcessor dataProvider = new LeadFormDataProcessor();
	ObjectMapper objectMapper = new ObjectMapper();

	public void process(Exchange exchange) throws Exception {
		String request = exchange.getIn().getBody().toString();
		JsonNode leadResponseNode = null;
		String quoteType = "2";
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("req of import lead : "+reqNode);

		if(reqNode.has("businessId") && reqNode.get("businessId").asText()!= null){
			if(reqNode.get("businessId").asText().equalsIgnoreCase("Car")){
				quoteType = "3";
			}else if(reqNode.get("businessId").asText().equalsIgnoreCase("Bike")){
				quoteType = "2";
			}else if(reqNode.get("businessId").asText().equalsIgnoreCase("Health")){
				quoteType = "4";
			}else if(reqNode.get("businessId").asText().equalsIgnoreCase("Life")){
				quoteType = "1";
			}else if(reqNode.get("businessId").asText().equalsIgnoreCase("Travel")){
				quoteType = "5";
			}else if(reqNode.get("businessId").asText().equalsIgnoreCase("Other")){
				quoteType = "Other";
			}

			for (JsonNode lead : reqNode.get("leadList")) {
				ObjectNode bodyNode = objectMapper.createObjectNode();
				ObjectNode quoteParamNode = objectMapper.createObjectNode();
				ObjectNode contactInfoNode = objectMapper.createObjectNode();
				if(lead.has("description") && lead.get("description")!=null){
					quoteParamNode.put("description_c", lead.get("description"));
				}
				if(lead.has("source") && lead.get("source")!=null){
					quoteParamNode.put("source", lead.get("source"));
				}
				quoteParamNode.put("quoteType", quoteType);
				contactInfoNode.put("messageId", "");
				contactInfoNode.put("termsCondition", true);
				contactInfoNode.put("createLeadStatus", false);
				if(lead.has("first_name") && lead.get("first_name")!= null){
					contactInfoNode.put("firstName", lead.get("first_name"));
				}
				if(lead.has("last_name") && lead.get("last_name")!= null){
					contactInfoNode.put("lastName", lead.get("last_name"));
				}
				if(lead.has("email") && lead.get("email")!= null){
					contactInfoNode.put("emailId", lead.get("email"));
				}
				if(lead.has("mobile") && lead.get("mobile")!= null){
					contactInfoNode.put("mobileNumber", lead.get("mobile"));
				}
				if(lead.has("alternative_mobile") && lead.get("alternative_mobile")!= null){
					contactInfoNode.put("alternativeNumber", lead.get("alternative_mobile"));
				}
				bodyNode.put("msgIdStatus", "new");
				bodyNode.put("requestSource", lead.get("lead_source"));
				bodyNode.put("campaign_id", reqNode.get("campaignId"));
				bodyNode.put("contactInfo", contactInfoNode);
				bodyNode.put("quoteParam", quoteParamNode);
				log.info("bodyNode lead :"+bodyNode);
				
				if((bodyNode.get("contactInfo").has("mobileNumber") && bodyNode.get("contactInfo").get("mobileNumber")!=null) ||
						(bodyNode.get("contactInfo").has("emailId") && bodyNode.get("contactInfo").get("emailId")!=null)){
					ObjectNode headerNode = objectMapper.createObjectNode();
					ObjectNode finalLeadNode = objectMapper.createObjectNode();
					headerNode.put("deviceId", lead.get("deviceId"));
					headerNode.put("source", "web");
					headerNode.put("transactionName", "createLead");
					finalLeadNode.put("header", headerNode);
					finalLeadNode.put("body", bodyNode);
					leadResponseNode = dataProvider.createLead(finalLeadNode);
				}
				contactInfoNode = null;
				quoteParamNode = null;
				bodyNode = null;
			}
		}
	}
	
	
	
}