package com.idep.imatLead.req.processor;

import aQute.libg.generics.Create;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

public class iMATLeadRequest {
	static Logger log = Logger.getLogger(PrepareiMATLead.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static JsonNode imatleadConfigNode = null;
	static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static
	{
		if (serverConfig != null)
		{
			try
			{
				imatleadConfigNode = objectMapper.readTree(((JsonObject)serverConfig.getDocBYId("iMATLeadConfiguration").content()).toString());
			}
			catch (Exception e)
			{
				log.error("iMATConfiguration Document Not Found", e);
				e.printStackTrace();
			}
		}
	}
	public String prepareLeadRequest(JsonNode reqNode) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		log.info("in iMATPrepareLeadRequest Processor"+reqNode);

		if (reqNode.has("lob") && reqNode.get("lob") != null){
			if ( reqNode.get("lob").asText().equalsIgnoreCase("life"))
			{
				((ObjectNode) reqNode).with("quoteParam").put("quoteType",1);
			}
			if ( reqNode.get("lob").asText().equalsIgnoreCase("bike"))
			{
				((ObjectNode) reqNode).with("quoteParam").put("quoteType",2);
			}
			if ( reqNode.get("lob").asText().equalsIgnoreCase("car"))
			{
				((ObjectNode) reqNode).with("quoteParam").put("quoteType",3);
			}
			if ( reqNode.get("lob").asText().equalsIgnoreCase("health"))
			{
				((ObjectNode) reqNode).with("quoteParam").put("quoteType",4);
			}

		}


		ObjectNode dataNode = objectMapper.createObjectNode();
		JsonNode quoteTypeNode = imatleadConfigNode.get("iMATLeadConfig").get("QuoteType" + reqNode.findValue("quoteType").asText());
		if (quoteTypeNode.has("defaultParam"))
		{
			Map<String, String> defaultDataNodeMap = (Map)objectMapper.readValue(quoteTypeNode.get("defaultParam").toString(), Map.class);
			for (Map.Entry<String, String> field1 : defaultDataNodeMap.entrySet()) {

				dataNode.put((String)field1.getKey(), (String)field1.getValue());
			}
		}

		log.info("DataNode before default param..."+dataNode);			

		Map<String, String> configDataNodeMap = (Map)objectMapper.readValue(quoteTypeNode.toString(), Map.class);
		log.info("configDataNodeMap in iMAT"+configDataNodeMap);
		configDataNodeMap.remove("defaultParam");

		log.info("dataNode"+dataNode);
		log.info("reqNode"+reqNode);
		log.info("configDataNodeMap1"+configDataNodeMap);

		for (Map.Entry<String, String> field : configDataNodeMap.entrySet()) {
			try
			{

				if (reqNode.findValue((String)field.getKey()).isTextual()) {
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

				/*else if (reqNode.findValue((String)field.getKey()).isTextual())
				{
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).textValue());
				}
				else if (reqNode.findValue((String)field.getKey()).isInt())
				{
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).intValue());
				}
				else if (reqNode.findValue((String)field.getKey()).isLong())
				{
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).longValue());
				}
				else if (reqNode.findValue((String)field.getKey()).isDouble())
				{
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).doubleValue());
				}
				else if (reqNode.findValue((String)field.getKey()).isBoolean())
				{
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).booleanValue());
				}
				else if (reqNode.findValue((String)field.getKey()).isFloat())
				{
					dataNode.put((String)field.getValue(), reqNode.findValue((String)field.getKey()).floatValue());
				}*/

			}
			catch (NullPointerException localNullPointerException) {}

		}
		log.info("DataNode..."+dataNode);
		return dataNode.toString();

	}

}
