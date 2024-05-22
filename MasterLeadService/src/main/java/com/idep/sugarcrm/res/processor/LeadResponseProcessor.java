package com.idep.sugarcrm.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.sugar.util.SugarCRMConstants;
import com.idep.data.searchconfig.cache.DocumentDataConfig;

public class LeadResponseProcessor implements Processor
{

	Logger log = Logger.getLogger(LeadResponseProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public void process(Exchange exchange) throws Exception
	{
		try
		{

			/**
			 * read input request
			 */
			String messageId = null;
			String request = exchange.getIn().getBody().toString();
			JsonNode reqNode = objectMapper.readTree(request);
		/*	if(!reqNode.has("messageId"))
			{
				messageId=exchange.getProperty(SugarCRMConstants.MESSAGE_ID).toString();
				((ObjectNode)reqNode).put("messageId",messageId);
			}*/
			log.info("SugarCRM Service: "+reqNode);
			ObjectNode responseNode = objectMapper.createObjectNode();
			ObjectNode msgIdNode = objectMapper.createObjectNode();
			messageId = reqNode.get("messageId").asText();
			msgIdNode.put("messageId", messageId);


			/*if( (reqNode.has(SugarCRMConstants.QUOTE_PARAM) && (reqNode.get(SugarCRMConstants.QUOTE_PARAM) != null) && 
					(reqNode.get(SugarCRMConstants.QUOTE_PARAM).has("quoteType")) && (reqNode.get(SugarCRMConstants.QUOTE_PARAM).get("quoteType") !=null) &&
					(reqNode.has(SugarCRMConstants.CONTACT_INFO)) && (reqNode.get(SugarCRMConstants.CONTACT_INFO) != null) &&
					((reqNode.get(SugarCRMConstants.CONTACT_INFO).has("mobileNumber") ) || (reqNode.get(SugarCRMConstants.CONTACT_INFO).has("emailId"))) &&
					(reqNode.get(SugarCRMConstants.CONTACT_INFO).has("termsCondition")) && (reqNode.get(SugarCRMConstants.CONTACT_INFO).get("termsCondition").asBoolean()) &&
					(reqNode.get(SugarCRMConstants.CONTACT_INFO).has("createLeadStatus")) && (!reqNode.get(SugarCRMConstants.CONTACT_INFO).get("createLeadStatus").asBoolean()) &&
					(reqNode.has("requestSource")) && (reqNode.get("requestSource") !=null)&&
					(reqNode.has("campaign_id")) && (reqNode.get("campaign_id") !=null)) || 
					(reqNode.has("isChat") && reqNode.get("isChat").asBoolean()) || 
					(reqNode.has("isProfessionalJourney") && reqNode.get("isProfessionalJourney").asBoolean())) */ 
			if(messageId!=null)
			{
				log.info("Success Response");
				responseNode.put("responseCode", 1000);
				responseNode.put("message", "success");
				responseNode.put("data", msgIdNode);
				if(reqNode.has("requestSource") && reqNode.get("requestSource").asText().equalsIgnoreCase("posp") &&  reqNode.has("leadExist") && reqNode.get("leadExist").asText().equalsIgnoreCase("Y")){
					((ObjectNode)responseNode.get("data")).put("message", DocumentDataConfig.getConfigDocList().get(SugarCRMConstants.RESPONSE_CONFIG_DOC).get("leadExistMessage").asText());
				}

			}else{
				log.info("Failure Response");
				responseNode.put("responseCode", 1002);
				responseNode.put("message", "failure");
				responseNode.put("data","");
			}		      
			exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
		}
		catch(Exception e)
		{
			log.error("Exception at LeadResponseProcessor : ",e);
			ObjectNode responseNode = objectMapper.createObjectNode();
			responseNode.put("responseCode", 1002);
			responseNode.put("message", "failure");
			responseNode.put("data", "");
			exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
		}

	}

}
