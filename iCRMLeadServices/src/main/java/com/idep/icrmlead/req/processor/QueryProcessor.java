package com.idep.icrmlead.req.processor;

import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

public class QueryProcessor {
	static Logger log = Logger.getLogger(QueryProcessor.class.getName());

	public String prepareQuery(JsonNode dataNode, Map<String, String> configDataNodeMap, JsonNode configDataNode, int interval)
	{
		String query = "";
		JsonNode mobileNo = null;
		JsonNode email1 = null;
		log.info("<Prepare Query> P365 dataNode  Node :: " + dataNode);
		log.info("<Prepare Query> P365 configDataNodeMap  Node :: " + configDataNodeMap);
		log.info("<Prepare Query> P365 configDataNode  Node :: " + configDataNode);
		log.info("<Prepare Query> P365 interval : " + interval);
		try
		{
			if (configDataNode.has("searchParamConfig"))
			{
				if (configDataNode.get("searchParamConfig").has("conditionParameter"))
				{
					if ((dataNode.has("email1")) || (dataNode.has("phone_mobile")))
					{
						mobileNo = dataNode.get("phone_mobile");
						email1 = dataNode.get("email1");
						log.info("update request has email or mobile number");
					}
					if (((mobileNo != null) && (mobileNo.asText().length() > 0)) || ((email1 != null) && (email1.asText().length() > 0) && (email1.toString().contains("@")))) {
						if ((dataNode.has("email1")) && (email1 != null) && (email1.asText().length() > 0) && (email1.toString().contains("@")) && (mobileNo != null) && (mobileNo.asText().length() > 0) && (dataNode.has("phone_mobile")))
						{
							log.info("update request has email or mobile number hence valid request"+dataNode);
							query = configDataNode.get("searchParamConfig").get("searchLeadQuery").asText();
							for (JsonNode fieldName : configDataNode.get("searchParamConfig").get("conditionParameter")) {
								query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
								log.info("forming query"+query);
							}
							log.info("query after replacing value12" + query);
						}
						else if ((dataNode.has("email1")) && (email1 != null) && (email1.asText().length() > 0) && (email1.toString().contains("@")))
						{
							log.info("update request has email or mobile number hence valid request"+dataNode);
							query = configDataNode.get("searchParamConfig").get("searchLeadEmailQuery").asText();
							for (JsonNode fieldName : configDataNode.get("searchParamConfig").get("conditionEmailParameter")) {
								query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
								log.info("forming query"+query);
							}
							log.info("query after replacing value34" + query);
						}
						else if (dataNode.has("phone_mobile"))
						{
							log.info("update request has email or mobile number hence valid request"+dataNode);
							query = configDataNode.get("searchParamConfig").get("searchLeadMobileQuery").asText();
							for (JsonNode fieldName : configDataNode.get("searchParamConfig").get("conditionParameterMobile")) {
								query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
								log.info("forming query"+query);
							}
							log.info("query after replacing value45" + query);
						}
					}
				}
			}
			else if (configDataNode.has("searchLeadQueryMsg"))
			{
				if (configDataNode.has("conditionParameter"))
				{
					log.info("update lead request has condition paramaetr"+configDataNode);
					query = configDataNode.get("searchLeadQueryMsg").asText();
					for (JsonNode fieldName : configDataNode.get("conditionParameter")) {
						query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
						log.info("forming query2 condition"+query);
					}
					log.info("query after replacing value56" + query);
				}
			}
			else if (configDataNode.has("searchLeadQueryProStatus"))
			{
				log.info("else if part");
				if ((dataNode.has("email1")) && (dataNode.has("phone_mobile")))
				{
					query = configDataNode.get("searchLeadQueryProStatus").asText();
					log.info("query after replacing value78" + query);

					query = configDataNode.get("searchParamLeadStatus").get("searchLeadQueryProStatus").asText();
					for (JsonNode fieldName : configDataNode.get("conditionParameter")) {
						query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
					}
					log.info("query after replacing value89" + query);
				}
				if (dataNode.has("email1"))
				{
					query = configDataNode.get("searchLeadQueryProStatusEmail").asText();
					log.info("query after replacing value09" + query);

					query = configDataNode.get("searchParamLeadStatus").get("searchLeadQueryProStatusEmail").asText();
					for (JsonNode fieldName : configDataNode.get("conditionParameter")) {
						query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
					}
					log.info("query after replacing value64" + query);
				}
				if (dataNode.has("phone_mobile"))
				{
					query = configDataNode.get("searchLeadQueryProStatusphoneMobile").asText();
					log.info("query after replacing value83" + query);

					query = configDataNode.get("searchParamLeadStatus").get("searchLeadQueryProStatusMobile").asText();
					for (JsonNode fieldName : configDataNode.get("conditionParameter")) {
						query = query.replace(fieldName.get("destFieldName").asText(), dataNode.get(fieldName.get("sourceFieldName").asText()).asText());
					}
					log.info("query after replacing value365" + query);
				}
			}
			else
			{
				for (Map.Entry<String, String> field : configDataNodeMap.entrySet())
				{
					String key = (String)field.getKey();
					if (key.contains(".")) {
						key = key.substring(key.indexOf(".") + 1, key.length());
					}
					if (((String)field.getValue()).equalsIgnoreCase("int")) {
						query = query + (String)field.getKey() + "=" + dataNode.findValue(key).intValue() + " and ";
					} else if (((String)field.getValue()).equalsIgnoreCase("float")) {
						query = query + (String)field.getKey() + "=" + dataNode.findValue(key).floatValue() + " and ";
					} else if (((String)field.getValue()).equalsIgnoreCase("long")) {
						query = query + (String)field.getKey() + "=" + dataNode.findValue(key).longValue() + " and ";
					} else if (((String)field.getValue()).equalsIgnoreCase("String")) {
						query = query + (String)field.getKey() + "='" + dataNode.findValue(key).textValue() + "'" + " and ";
					} else if (((String)field.getValue()).equalsIgnoreCase("date")) {
						query = query + (String)field.getKey() + ">=(CURDATE() - INTERVAL " + interval + " DAY)" + " and ";
					}
				}
				query = query.substring(0, query.length() - 4);

				log.info("search lead by query : " + query);
			}
		}
		catch (Exception e)
		{
			log.error("Exception while preparing query : ", e);
			query = "ERROR";
		}
		return query;
	}

}
