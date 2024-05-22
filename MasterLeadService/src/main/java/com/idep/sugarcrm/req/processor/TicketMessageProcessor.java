package com.idep.sugarcrm.req.processor;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.sugar.util.SugarCRMConstants;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;


public class TicketMessageProcessor implements Processor
{
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();

	Logger log=Logger.getLogger(TicketMessageProcessor.class.getName());
	ObjectMapper objectMapper= new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	JsonNode ticketConfigNode = null;
	JsonNode leadConfigNode = null;


	public void process(Exchange exchange) throws Exception {

		try {
			String request=exchange.getIn().getBody().toString();
			JsonNode reqNode=objectMapper.readTree(request);
			log.info("The ticket request"+reqNode);
			// get Insurance type to lead validate
			String type=reqNode.findValue("insuranceType").asText();
			if (reqNode.findValue("insuranceType").asText().equalsIgnoreCase("Register Claim"))
			{
				type=reqNode.findValue("selectedLineOfBusiness").asText();

			}

			if (this.ticketConfigNode == null)
			{
				this.ticketConfigNode = objectMapper.readTree(this.serverConfig.getDocBYId("TicketConfiguration").content().toString());
			}

			if (this.leadConfigNode == null)
			{
				this.leadConfigNode = objectMapper.readTree(this.serverConfig.getDocBYId("LeadConfiguration").content().toString());
			}		

			((ObjectNode)reqNode).put("msgIdStatus", "new");

			// newReqNode - to form lead request
			ObjectNode newReqNode = this.objectMapper.createObjectNode();
			ObjectNode quoteParam = this.objectMapper.createObjectNode();
			@SuppressWarnings("unchecked")

			Map<String, String> leadReqProcNodeMap = objectMapper.readValue(ticketConfigNode.get("leadRequestProcessor").toString(), Map.class);

			newReqNode =filterMapData(reqNode,leadReqProcNodeMap);

			if(reqNode.has("quoteParam")){
				newReqNode.put("quoteParam", reqNode.get("quoteParam"));				
			}

			newReqNode.put("msgIdStatus", reqNode.findValue("msgIdStatus"));
			if (reqNode.has("requestSource"))
			{
				newReqNode.put("requestSource",reqNode.findValue("requestSource").asText() );

			}else
			{
				newReqNode.put("requestSource", "WebPortal");
			}
			if (reqNode.has("campaign_id"))
			{
				newReqNode.put("campaign_id",reqNode.findValue("campaign_id").asText() );
			}

			JsonNode quoteType=ticketConfigNode.get("quoteTypeConfig");

			//putting quote type from request.
			quoteParam.put("quoteType", quoteType.get(type).asInt());
			newReqNode.put("quoteParam", quoteParam);
			//newReqNode.put("requestSource", "web");
			if(reqNode.get("paramMap").has("policyNumber"))
			{
				newReqNode.put("stage_c", "claim");
			}
			JsonNode configDataNode = leadConfigNode.get("leadConfig").get("LeadValidate" + newReqNode.findValue("quoteType").asText());

			// Prepare query to find lead.
			String query=prepareQuery(ticketConfigNode.get("searchParamConfig"),newReqNode);

			String leadId = crmService.findLead(query, configDataNode.get("select_fields").asText());

			if(!leadId.equalsIgnoreCase("ERROR"))
			{
				if(leadId.length()==0){
					CamelContext camelContext = exchange.getContext();
					ProducerTemplate template = camelContext.createProducerTemplate();
					String uri = "activemq:queue:"+SugarCRMConstants.REQ_QUEUE;
					template.sendBody(uri, newReqNode.toString());		
					((ObjectNode) reqNode).put("customer_type","New");
				}else{
					((ObjectNode) reqNode).put("customer_type","Existing Customer");
				}
			}
			exchange.getIn().setBody(objectMapper.writeValueAsString(reqNode));
		} catch (Exception e) {
			log.error("Error while preparing a message for Ticket request Q : TicketQ ",e);
		}	
	}

	private ObjectNode filterMapData(JsonNode reqNode,Map<String, String> leadReqProcNodeMap)
	{
		ObjectNode dataNode= this.objectMapper.createObjectNode();
		for (Map.Entry<String, String> field : leadReqProcNodeMap.entrySet())
		{

			try
			{
				if (reqNode.findValue(field.getKey()).isTextual()) {
					dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).textValue());
				} else if (reqNode.findValue(field.getKey()).isInt()) {
					dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).intValue());
				} else if (reqNode.findValue(field.getKey()).isLong()) {
					dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).longValue());
				} else if (reqNode.findValue(field.getKey()).isDouble()) {
					dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).doubleValue());
				} else if (reqNode.findValue(field.getKey()).isBoolean()) {
					dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).booleanValue());
				} else if (reqNode.findValue(field.getKey()).isFloat()) {
					dataNode.put(field.getValue(), reqNode.findValue(field.getKey()).floatValue());
				} 
			}
			catch(NullPointerException e)
			{
				// ignore null pointer as requested field may be missing in request
			}
		}
		return dataNode;
	}

	private String prepareQuery(JsonNode configDataNode,ObjectNode newReqNode){
		String preparedQuery="";

		try {
			if(configDataNode.has("conditionParameter"))
			{
				preparedQuery=configDataNode.get("searchLeadQuery").asText();
				for(JsonNode fieldName : configDataNode.get("conditionParameter"))
				{		
					preparedQuery = preparedQuery.replace(fieldName.get("destFieldName").asText(), newReqNode.get(fieldName.get("sourceFieldName").asText()).asText());
				}
			}			
		} catch (Exception e) {
			this.log.error("Exception while preparing query : ", e);
			preparedQuery = "ERROR";
		}
		return preparedQuery;
	}
}
