package com.idep.readEmailStatus;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class UpdateLeadProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(UpdateLeadProcessor.class.getName());
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();

	@SuppressWarnings("unused")
	public void process(Exchange exchange) throws Exception {
		String leadId = null;
		// For update lead 
		ObjectNode updateNode = objectMapper.createObjectNode(); 
		updateNode.put("stage_c", "quote");
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("UpdateLeadProcessor Req :"+reqNode);
		// Get lead id to check parent id in email for update email
		if(reqNode.has("parent_id") && reqNode.get("parent_id") != null){
			log.info("parent_id  :"+reqNode.get("parent_id"));
			leadId = reqNode.get("parent_id").asText();
		}else if(reqNode.has("messageId") && reqNode.get("messageId") != null){
			log.info("messageId  :"+reqNode.get("messageId"));
			leadId = crmService.findLead("leads_cstm.messageid_c = '"+reqNode.get("messageId").asText()+"'", " ");
		}
		// update lead 
		if(leadId != null || leadId != "" ){
			crmService.updateLead(updateNode, leadId);
		}else{
			log.info("Error While updating Lead");
			throw new Exception();
		}
		log.info("Updated Lead with id :"+leadId);
		exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
	}
}
