package com.idep.travelquote.req.form;

import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class BhartiAxaFamilyMemberCountCodeProcesser implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(BhartiAxaFamilyMemberCountCodeProcesser.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String inpReq = exchange.getIn().getBody(String.class);
			JsonNode inpReqNode = objectMapper.readTree(inpReq);
			
			String familyMemberCount="";
			JsonNode productInfoNode = inpReqNode.get(TravelQuoteConstants.PRODUCT_INFO);
			log.info("BhartiAxaFamilyMemberCountCodeProcesser productInfoNode:::::::::::"+productInfoNode);
			ArrayNode travllerMember = (ArrayNode)inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM).get(TravelQuoteConstants.TRAVLLERS);
			if(inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM).has(TravelQuoteConstants.TRAVLLERS)){
					for(JsonNode member : travllerMember){
						if(member.has("relation"))
						{
							
							if((member.get("relation").asText().equalsIgnoreCase("Self"))||(member.get("relation").asText().equalsIgnoreCase("Spouse"))){
								familyMemberCount=familyMemberCount +"S";
							}
							
							else if((member.get("relation").asText().equalsIgnoreCase("Son"))|| (member.get("relation").asText().equalsIgnoreCase("Daughter"))){
								familyMemberCount=familyMemberCount +"C";
							}
							else{
								log.info("Family Member other than self spouse and child are not allowed and relation is : "+member.get("relation").asText());
								throw new ExecutionTerminator();							
							}
								
						}
						
					}
					char[] charArray =familyMemberCount.toCharArray();
					Arrays.sort(charArray);
					familyMemberCount=String.valueOf(charArray);
			}
			 
			((ObjectNode)inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM)).put("familyMemberCountCode", familyMemberCount);

			
			log.info("Family Member Count code is familyMemberCountCode "+familyMemberCount);
			exchange.setProperty(TravelQuoteConstants.UI_QUOTEREQUEST,inpReqNode);
			exchange.getIn().setBody(inpReqNode);
		}catch(Exception e){
			log.error("unable to find familyMemberCountCode : ",e);
			throw new ExecutionTerminator();
		}
	}
}
