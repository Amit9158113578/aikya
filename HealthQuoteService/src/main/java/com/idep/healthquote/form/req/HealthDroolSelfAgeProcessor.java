package com.idep.healthquote.form.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.camel.Exchange;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
public class HealthDroolSelfAgeProcessor implements Processor {

	Logger log = Logger.getLogger(HealthDroolReqServiceTaxProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			
			String input = exchange.getIn().getBody(String.class);
			JsonNode DroolReq = objectMapper.readTree(input);
			
			 JsonNode productInfoNode = DroolReq.get(HealthQuoteConstants.PRODUCT_INFO);
		      JsonNode quoteParamNode = DroolReq.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM);
			
		      ArrayNode dependant = (ArrayNode)quoteParamNode.get("dependent");
		      log.debug(" HealthDroolSelfAgeProcessor quoteParamNode Before Any Changed : "+quoteParamNode);
		      int age[] = new int[dependant.size()];
		      int index=0;
		      List<Integer> Age = new ArrayList<>();
		      for(JsonNode member : dependant ){
		    	  Age.add(member.get("age").asInt());		    	  
		      }
		     Collections.sort(Age);
		     ((ObjectNode)quoteParamNode).put("selfAge",(Age.get(Age.size()-1)));
		     
		      log.debug(" HealthDroolSelfAgeProcessor quoteParamNode After Self Age Changed : "+quoteParamNode);
		      exchange.getIn().setBody(DroolReq); 
		}catch(Exception e ){
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthDroolSelfAgeProcessor : ",e);
			throw new ExecutionTerminator();
		}
	}
}
