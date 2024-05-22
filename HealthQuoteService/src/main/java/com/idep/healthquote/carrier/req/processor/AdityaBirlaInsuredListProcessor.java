package com.idep.healthquote.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.form.req.CignaSumInsuredReqProcessor;
import com.idep.healthquote.util.HealthQuoteConstants;

/**
 * @author pravin.jakhi
 *
 */
public class AdityaBirlaInsuredListProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(AdityaBirlaInsuredListProcessor.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
	try{
		
		String inputReq = exchange.getIn().getBody(String.class);
		JsonNode inputReqNode = objectMapper.readTree(inputReq);
		JsonNode personalInfo = inputReqNode.get("personalInfo");
		
		ArrayNode InsuredNode = (ArrayNode)personalInfo.get("selectedFamilyMembers");
		
		((ObjectNode)personalInfo).put("InsuredDetails",InsuredNode);
		
		((ObjectNode)inputReqNode).put("personalInfo", personalInfo);
		log.debug("BODY IN personalInfo Added : "+inputReqNode);
		exchange.getIn().setBody(inputReqNode);
		
	}catch(Exception e){
		log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"AdityaBirlaInsuredListProcessor : ",e);
		throw new ExecutionTerminator();
	}
	}

}
