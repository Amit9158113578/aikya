package com.idep.proposal.carrier.req.processor;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.json.JSONArray;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class HDFCErgoInsuredmemberProcessor implements Processor{

	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HDFCErgoInsuredmemberProcessor.class.getName());
	CBService quoteDataService = CBInstanceProvider.getBucketInstance("QuoteData");


	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			
			
			 String input = exchange.getIn().getBody(String.class);
			 JsonNode request =  this.objectMapper.readTree(input);
			 JsonNode membersList = request.get("insuredMembers");
			 int count = 1;
			 String isProposersame="N";
			 for(JsonNode members : membersList){
				 ((ObjectNode)members).put("srNo", count);
				 ((ObjectNode)members).put("insuredId", count);
				 count++;
				 if(members.has("relationship")){
					 if(members.get("relationship").asText().equalsIgnoreCase("Self")){
						 isProposersame="Y";
					 }
					 if(members.get("relationship").asText().equalsIgnoreCase("Spouse") && members.get("gender").asText().equalsIgnoreCase("F")){
						 ((ObjectNode)members).put("relationship","Wife");
					 }else if( members.get("relationship").asText().equalsIgnoreCase("Spouse") && members.get("gender").asText().equalsIgnoreCase("M")){
						 ((ObjectNode)members).put("relationship","Husband");
					 }
				 }
			 }
			 
			 if(request.has("QUOTE_ID")){
		    		JsonDocument quoteId = quoteDataService.getDocBYId(request.get("QUOTE_ID").asText());
		    		JsonNode quoteDoc = objectMapper.readTree(quoteId.content().toString());
		    		ArrayNode quoteResponse = (ArrayNode)quoteDoc.get("quoteResponse");
		    		log.info("arrayNode :"+quoteResponse);
		    		for(JsonNode node : quoteResponse){
			    		log.info("node :"+node);
			    		if(node.get("carrierId").asText().equalsIgnoreCase("28") && node.get("planId").asText().equalsIgnoreCase("81")){
				    		log.info("carrierId is 28");
			   			 ((ObjectNode)request).put("carrierResponse", node);
			    		}
		    		}
		    		
		    		JsonNode carrierTransaform = quoteDoc.get("carrierTransformedReq");
		    		JsonNode reqNode = carrierTransaform.get("28");
		   			 ((ObjectNode)request).put("carrierTransformedReq", reqNode);
			 }
			 ((ObjectNode)request).put("insuredMembers", membersList);
			 ((ObjectNode)request).put("isProposersame", isProposersame);
			 log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.MAPPERREQ+"|INIT|Mapper request transform started"); 
			 exchange.getIn().setBody(request);
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|HDFCErgoInsuredmemberProcessor|",e);
			throw new ExecutionTerminator();
		}
		
		
		
	}

}
