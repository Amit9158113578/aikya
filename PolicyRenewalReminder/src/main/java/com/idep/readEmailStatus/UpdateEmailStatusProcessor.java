package com.idep.readEmailStatus;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class UpdateEmailStatusProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UpdateRecordReqProcessor.class.getName());
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();

	@SuppressWarnings("unused")
	public void process(Exchange exchange) throws Exception {
		String leadId = null;
		String emailId = null;
		JsonNode recordNode = null;
		// For update email status as read
		ObjectNode updateNode = objectMapper.createObjectNode(); 
		updateNode.put("status", "read");
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("Update Email Status Req :"+reqNode);

		if(reqNode.has("smsId") && reqNode.get("smsId").asText().contains("MAILID")){
			log.info("altered value");
			((ObjectNode) reqNode).put("mailId",reqNode.get("smsId"));
			((ObjectNode) reqNode).remove("smsId");
		}

		// Get lead id to check parent id in email for update email
		if(reqNode.has("mailId") && reqNode.get("mailId") != null){
			recordNode = crmService.getModuleData("mailId = '"+reqNode.get("mailId").asText()+"'", "Emails", "id,parent_id");
			log.info("email id got :"+recordNode);
			if(recordNode != null && recordNode.has("id") && recordNode.get("id") != null){
				//update email status as read
				emailId = crmService.updateModuleRecord(updateNode, "Emails", recordNode.get("id").asText());	
			}
			if(recordNode.has("parent_id") && recordNode.get("parent_id") != null){
				((ObjectNode)reqNode).put("parent_id",recordNode.get("parent_id").asText());
			}
			log.info("Updated email with id :"+emailId);

		}else if(reqNode.has("smsId") && reqNode.get("smsId") != null){

			recordNode = crmService.getModuleData("smsId = '"+reqNode.get("smsId").asText()+"'", "Notes", "id,parent_id");

			log.info("sms id got :"+recordNode);
			if(recordNode != null && recordNode.has("id") && recordNode.get("id") != null){
				//update sms status as read
				emailId = crmService.updateModuleRecord(updateNode, "Notes", recordNode.get("id").asText());	
			}
			if(recordNode.has("parent_id") && recordNode.get("parent_id") != null){
				((ObjectNode)reqNode).put("parent_id",recordNode.get("parent_id").asText());
			}
			log.info("Updated sms with id :"+emailId);
			// Backward compatible
		}else if(reqNode.has("messageId") && reqNode.get("messageId") != null){
			log.info("messageId  :"+reqNode.get("messageId"));
			leadId = crmService.findLead("leads_cstm.messageid_c = '"+reqNode.get("messageId").asText()+"'", " ");
			// Get email id for update
			if(leadId != null || leadId != "" ){
				emailId = crmService.findModuleRecord("parent_id = '"+leadId+"' AND name ='Renewal Email'", "Emails", "");
			}else{
				log.info("Error While updating email");
				throw new Exception();
			}
			log.info("emailId  :"+emailId);
			//update email status as read
			emailId = crmService.updateModuleRecord(updateNode, "Emails", emailId);
			log.info("Updated email with id :"+emailId);

		}
		exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
	}
}
