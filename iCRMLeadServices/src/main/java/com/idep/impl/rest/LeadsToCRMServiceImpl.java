package com.idep.impl.rest;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class LeadsToCRMServiceImpl
{
	Logger log = Logger.getLogger(LeadsToCRMServiceImpl.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	public String updateLeads(String recordData)
	{
		log.info("Record  request received in leadsToCRMServiceImpl class");
		return recordData;
	}
}
