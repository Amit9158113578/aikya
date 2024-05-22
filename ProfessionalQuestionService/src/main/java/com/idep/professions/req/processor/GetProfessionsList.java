package com.idep.professions.req.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.professions.constant.ProfessionalConstant;
import com.idep.professions.exception.ExecutionTerminator;

/**
 * 
 * @author kuldeep.patil
 * @date 28-12-2018
 *
 */

public class GetProfessionsList implements Processor {

	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(GetProfessionsList.class);
	static CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	static JsonNode professionalList=null;

	static{
		
		try {
			professionalList = objectMapper.readTree(serverConfigService.getDocBYId(ProfessionalConstant.PROFESSIONSLIST).content().get("Professions").toString());
		
		} catch (Exception e) {
			log.error("Unable to cache  ProfessionsList document : ",e);
			new ExecutionTerminator();
		}
	}
	
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			String request = exchange.getIn().getBody(String.class);
			log.info("recived reqest professionalList : "+request);
			if (professionalList == null) {
				professionalList = objectMapper.readTree(serverConfigService
						.getDocBYId(ProfessionalConstant.PROFESSIONSLIST)
						.content().get("Professions").toString());
			}
			log.info("professionalList : "+professionalList);
			exchange.getIn().setBody(professionalList);

		} catch (Exception e) {
			log.error("Failed to load profession list  Config Document",e);
			new ExecutionTerminator();
		}

	}

}
