package com.idep.travelquote.req.form;

/**
 * 
 * @author pravin.jakhi
 * Calculate adult and child count basis on selected travller array 
 * 
 * */
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

public class CalculateMemberCountProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CalculateMemberCountProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String inpReq = exchange.getIn().getBody(String.class);
			JsonNode inpReqNode = objectMapper.readTree(inpReq);
			int childCount=0;
			int adultCount=0;
			int totalCount=0;
			int childMaxAgeLimit=18;
			
			// if childMaxAgeLimit is not Present in ProductData Then default childMaxAgeLimit is 18

			
			JsonNode productInfoNode = inpReqNode.get(TravelQuoteConstants.PRODUCT_INFO);
			log.debug("productInfoNode:::::::::::"+productInfoNode);
			ArrayNode travllerMember = (ArrayNode)inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM).get(TravelQuoteConstants.TRAVLLERS);

			if(productInfoNode.has("childMaxAgeLimit")){
				childMaxAgeLimit=productInfoNode.get("childMaxAgeLimit").asInt();
			}

			if(inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM).has(TravelQuoteConstants.TRAVLLERS)){
					for(JsonNode member : travllerMember){
						
						if(member.get("age").asInt() >= childMaxAgeLimit){
							adultCount++;
						}else{
							childCount++;
						}
					}
					
			}
			 totalCount= childCount+adultCount;
			((ObjectNode)inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM)).put("adultCount", adultCount);
			((ObjectNode)inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM)).put("childCount", childCount);
			((ObjectNode)inpReqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM)).put("totalCount", totalCount);
			

			log.info("adult and child count calculated :"+adultCount+"\tChildCount : "+childCount);
			exchange.setProperty(TravelQuoteConstants.UI_QUOTEREQUEST,inpReqNode);
			exchange.getIn().setBody(inpReqNode);
		}catch(Exception e){
			log.error("unable to calculate member Count : ",e);
			throw new ExecutionTerminator();
		}
	}
}
