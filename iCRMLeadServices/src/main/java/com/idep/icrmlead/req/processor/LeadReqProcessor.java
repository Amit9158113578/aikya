package com.idep.icrmlead.req.processor;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class LeadReqProcessor implements Processor {
	static Logger log = Logger.getLogger(UpdateLeadRequest.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static JsonNode leadConfigNode = null;
	static JsonNode leadQueryConfigNode = null;
	CreateLeadProcessor createupdateLead = new CreateLeadProcessor();
	QueryProcessor queryProcessor = new QueryProcessor();
	static
	{
		if (serverConfig != null) {
			try
			{
				leadConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("iMATToCRMConfig").content()).toString());
				leadQueryConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("iMATToCRMQueryConfig").content()).toString());
			}
			catch (Exception e)
			{
				log.error("iMATToCRMConfig Configuration Document Not Found", e);
				e.printStackTrace();
			}
		}
	}
	public void process(Exchange exchange) throws Exception
	{
		try
		{
			String request = exchange.getIn().getBody().toString();
			JsonNode inputRequest = this.objectMapper.readTree(request);
			log.info("log in LeadReqProcessor"+inputRequest.findValue("results"));

			String methodOutput = prepareLeadRequest(inputRequest.findValue("results"));
			log.info("Method output.."+methodOutput);
			ObjectNode leadDataNode = this.objectMapper.createObjectNode();
			leadDataNode = (ObjectNode) objectMapper.readTree(methodOutput);

			log.info("QueryDocument"+leadQueryConfigNode);
			String leadId = isLeadExist(leadDataNode, inputRequest, this.leadConfigNode);
			log.info("leadId.."+leadId);
			log.info("Lead Id Present !! " + leadId + " and messageID : " + inputRequest.get("leadmsgid"));
			if (!leadId.equalsIgnoreCase("ERROR"))
			{
				if (leadId.length() > 0)
				{
					leadId = this.createupdateLead.updateLead(leadDataNode, leadId);
					log.info("updatelead response"+leadId);
					exchange.getIn().setHeader("updateLeadId",leadId);
				}
				else
				{
					leadId = this.createupdateLead.createLead(leadDataNode);
					log.info("craetelead response"+leadId);
					exchange.getIn().setHeader("createLeadId",leadId);
				}
			}
			else
			{
				this.log.error("msgIdStatus is missing in lead create request");
			}
		}
		catch(Exception e)
		{
			log.error("Error in LeadReqProcessor Processor");
		}	    
	}
	public String prepareLeadRequest(JsonNode reqNode)throws JsonParseException, JsonMappingException, IOException
	{
		log.info("in iMATToCRMConfig PrepareLeadRequest Processor" + reqNode);

		ObjectNode dataNode = objectMapper.createObjectNode();

		// JsonNode quoteTypeNode = leadConfigNode.get("iMATLeadConfig").get("QuoteType" + reqNode.get("quoteParam").findValue("quoteType").asText());

		if (leadConfigNode.has("defaultParam"))
		{
			Map<String, String> defaultDataNodeMap = (Map)objectMapper.readValue(leadConfigNode.get("defaultParam").toString(), Map.class);
			for (Map.Entry<String, String> field1 : defaultDataNodeMap.entrySet()) {
				dataNode.put((String)field1.getKey(), (String)field1.getValue());
			}
		}
		log.info("DataNode before default param..." + dataNode);

		Map<String, String> configDataNodeMap = (Map)objectMapper.readValue(leadConfigNode.get("MappingConfig").toString(), Map.class);
		log.info("configDataNodeMap in processor" + configDataNodeMap);
		configDataNodeMap.remove("defaultParam");

		log.info("dataNode" + dataNode);
		log.info("reqNode" + reqNode);
		log.info("configDataNodeMap1" + configDataNodeMap);


		for (Map.Entry<String, String> field : configDataNodeMap.entrySet()) {
			try
			{ if( !reqNode.findValue((String)field.getKey()).isNull() || reqNode.findValue((String)field.getKey())!= null ){
				if (reqNode.findValue((String)field.getKey()).isTextual() ) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).textValue());
				} else if (reqNode.findValue((String)field.getKey()).isInt()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).intValue());
				} else if (reqNode.findValue((String)field.getKey()).isLong()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).longValue());
				} else if (reqNode.findValue((String)field.getKey()).isDouble()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).doubleValue());
				} else if (reqNode.findValue((String)field.getKey()).isBoolean()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).booleanValue());
				} else if (reqNode.findValue((String)field.getKey()).isFloat()) {
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).floatValue());
				}
			}
			else {
				log.info("Value has null or empty value");
			}
			}
			catch (NullPointerException localNullPointerException) {}
		}
		log.info("DataNode..." + dataNode);
		return dataNode.toString();
	}

	private String isLeadExist(JsonNode leadDataNode, JsonNode reqNode, JsonNode leadConfigNode)
	{
		String leadId = "";
		try
		{
			JsonNode configDataNode = leadQueryConfigNode.get("LeadValidate");
			Map<String, String> configDataNodeMap = (Map)this.objectMapper.readValue(configDataNode.get("searchParamConfig").toString(), Map.class);
			String query = this.queryProcessor.prepareQuery(leadDataNode, configDataNodeMap, configDataNode, configDataNode.get("interval").asInt());
			if (query.equalsIgnoreCase("ERROR")) {
				leadId = "ERROR";
			}
			return this.createupdateLead.findLead(query, configDataNode.get("select_fields").asText());
		}
		catch (Exception e)
		{
			this.log.error("Exception while fetching leads by query ", e);
			leadId = "ERROR";
		}
		return leadId;
	}	
}