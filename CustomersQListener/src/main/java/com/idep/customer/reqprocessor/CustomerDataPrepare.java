package com.idep.customer.reqprocessor;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.sugarcrm.service.impl.SugarCRMModuleServices;

public class CustomerDataPrepare {
	Logger log = Logger.getLogger(CustomerDataPrepare.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SugarCRMModuleServices crmService = new SugarCRMModuleServices();
	
	public String isCustomerExist(JsonNode customerDataNode, JsonNode customerConfigNode)
	{
		String customerId = "";
		try
		{
			@SuppressWarnings("unchecked")
			Map<String, String> configDataNodeMap = objectMapper.readValue(customerConfigNode.toString(), Map.class);
			String query = prepareCustomerQuery(customerDataNode,configDataNodeMap);
			if(! query.equalsIgnoreCase("ERROR")){
				customerId = crmService.findCustomer(query, customerConfigNode.get("select_fields").asText());
				log.info("Length Of Customer Id :"+customerId.length());
			}
			else
			{
				customerId = "ERROR";
			}
			return customerId;
		}
		catch(Exception e)
		{
			log.error("Exception while fetching Customer by query ",e);
			customerId = "ERROR";
			return customerId;
		}
	}

	@SuppressWarnings("unchecked")
	public ObjectNode prepareCustomerDataSet(JsonNode reqNode,JsonNode custConfigNode) throws JsonParseException, JsonMappingException, IOException
	{
		log.info("prepareCustomerDataSet "+reqNode);
		ObjectNode dataNode = this.objectMapper.createObjectNode(); 
		try
		{
			JsonNode custNode = custConfigNode;
			Map<String, String> configDataNodeMap = objectMapper.readValue(custNode.toString(), Map.class);
			dataNode = filterMapData(dataNode,reqNode,configDataNodeMap);
		}catch (Exception e){
			log.error("Exception while preparing customer data",e);
		}
		return dataNode;
	}

	private ObjectNode filterMapData(ObjectNode dataNode,JsonNode reqNode,Map<String, String> configDataNodeMap)
	{
		for (Map.Entry<String, String> field : configDataNodeMap.entrySet())
		{
			try
			{
				if(reqNode.findValue(field.getKey()).isTextual()) {
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
				log.error("Null Pointer Exception for key :"+field.getValue());
				//Ignore null pointer as requested field may be missing in request
			}
		}
		log.info(" After Filter :"+dataNode.toString());
		return dataNode;
	}

	public String prepareCustomerQuery(JsonNode dataNode,Map<String, String> configDataNodeMap)
	{
		log.info("prepare Customer Query :"+dataNode);
		log.info("prepare Customer Query :"+configDataNodeMap);

		String query = "";
		try
		{
			for (Map.Entry<String, String> field : configDataNodeMap.entrySet())
			{
				String key = (String)field.getKey();
				if (key.contains(".")) {
					key = key.substring(key.indexOf(".") + 1, key.length());
				}
				if (((String)field.getValue()).equalsIgnoreCase("int")) {
					query = query + (String)field.getKey() + "=" + dataNode.findValue(key).intValue() + " OR ";
				}
				else if (((String)field.getValue()).equalsIgnoreCase("float")) {
					query = query + (String)field.getKey() + "=" + dataNode.findValue(key).floatValue() + " OR ";
				}
				else if (((String)field.getValue()).equalsIgnoreCase("long")) {
					query = query + (String)field.getKey() + "=" + dataNode.findValue(key).longValue() + " OR ";
				} else if (((String)field.getValue()).equalsIgnoreCase("String")) {
					query = query + (String)field.getKey() + "='" + dataNode.findValue(key).textValue() + "'" + " OR ";
				} 
			}
			query = query.substring(0, query.length() - 3);
			this.log.info("search Customer by query : " + query);
		}
		catch(Exception e)
		{
			log.error("Exception while preparing customer query : ",e);
			e.printStackTrace();
			query = "ERROR";
		}
		return query;
	}
	
	public String convertDate(String dateString) {
		DateFormat inputDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String changed_date = null;
		try {
			Date date = inputDateFormat.parse(dateString);
			changed_date = outputDateFormat.format(date);

		} catch (ParseException e) {
			log.info("Exception in changing end date format");
			e.printStackTrace();
		}
		return changed_date;
	}
	
	
}
