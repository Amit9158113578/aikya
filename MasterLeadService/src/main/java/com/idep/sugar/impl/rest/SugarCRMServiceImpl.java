package com.idep.sugar.impl.rest;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class SugarCRMServiceImpl
{
	Logger log = Logger.getLogger(SugarCRMServiceImpl.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	public String collectLeadData(String leadData) throws JsonProcessingException, IOException, ExecutionTerminator
	{   

		ObjectNode leadDataJson = (ObjectNode) objectMapper.readTree(leadData);
		this.log.info("lead json node" + leadDataJson);

		try {
			if(leadDataJson.has("isChat") && leadDataJson.get("isChat").asBoolean() == true){
				log.info("Its Chat lead");
				return leadDataJson.toString();
			} else if (leadDataJson.findValue("contactInfo").has("mobileNumber") && leadDataJson.findValue("contactInfo").get("mobileNumber") != null) 
			{
				this.log.info("mobile number found");
				return leadDataJson.toString();
			}
			else if(leadDataJson.findValue("contactInfo").has("emailId") && leadDataJson.findValue("contactInfo").get("emailId") != null ) 
			{
				this.log.info("email found");
				return leadDataJson.toString();
			}
		}
		catch(Exception e)
		{
			log.error("Exception : ",e);
			throw new ExecutionTerminator();
		}
		
		if(leadDataJson.toString() != null && ! leadDataJson.has("isChat") ){
			log.error("Mobile or Email or Chat is not present in request ");
			throw new ExecutionTerminator();
		}
		return "";

	}//end of collectLeadData

	public String collectTicketData(String ticketData)
	{  
		return ticketData;
	}
	
	public String collectURL(String urlData)
	{  
		return urlData;
	}

	public String collectRecordData(String recordData)
	{
		log.info("Record  request received");
		return recordData;
	}
}
