package com.idep.healthquote.req.processor;

import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.util.HealthQuoteConstants;
import com.idep.healthquote.exception.processor.ExecutionTerminator;

public class MaxBupaGetPlanCodeProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(MaxBupaGetPlanCodeProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static CBService productService = CBInstanceProvider.getProductConfigInstance();
	static List<Map<String, Object>> planDetails;
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
		
		String inputReq = exchange.getIn().getBody(String.class);	
		JsonNode inputReqNode = objectMapper.readTree(inputReq);
		 JsonNode productInfoNode = inputReqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		 log.info("Max Bupa productInfoNode : "+productInfoNode);
		JsonNode healthReqConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.CARRIER_HEALTH_QUOTE_REQ_CONF+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()).content().toString());
		   // log.info("healthReqConfigNode: "+healthReqConfigNode);
		    /**
		     * set request configuration document id HealthQuoteRequest
		     */
		if(healthReqConfigNode!=null){
		    exchange.setProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,healthReqConfigNode);
		}else{
			log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|Quote Request Document not fuond|"+HealthQuoteConstants.CARRIER_HEALTH_QUOTE_REQ_CONF+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue());
		}
	String whereClause=" and sumInsured = "+productInfoNode.get(HealthQuoteConstants.DROOLS_SUM_INSURED);
	ArrayNode riderConfig = (ArrayNode)healthReqConfigNode.get(HealthQuoteConstants.RIDER_LIST).get(HealthQuoteConstants.RIDERS);
		if(inputReqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM).has(HealthQuoteConstants.RIDERS)){
			ArrayNode riders = (ArrayNode)inputReqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM).get(HealthQuoteConstants.RIDERS);
				if(healthReqConfigNode.has(HealthQuoteConstants.RIDER_LIST)){
					for( JsonNode rider: riderConfig){
						int riderId= rider.get(HealthQuoteConstants.RIDER_ID).asInt();
						boolean flag=false;
						for(JsonNode UIRider : riders){
							
							if(UIRider.get(HealthQuoteConstants.RIDER_ID).asInt()==riderId){
								whereClause = whereClause + " and "+rider.get(HealthQuoteConstants.RIDER_NAME).asText()+" = "+rider.get("riderAmount").asText();
								log.info("Where clase flag true : "+whereClause);
								flag=true;
							}
						}
						if(!flag){
							whereClause = whereClause + " and "+rider.get(HealthQuoteConstants.RIDER_NAME).asText()+" = "+rider.get("defaultValue").asText();
						}
						
						/*if(riders.has(riderId)){
						whereClause = whereClause + " and "+rider.get("riderName").asText()+" = "+rider.get("riderAmount").asText();
						}else{
							whereClause = whereClause + " and "+rider.get("riderName").asText()+" = "+rider.get("defaultValue").asText();
						}*/
					}
				}
		}else{
			for( JsonNode rider: riderConfig){
					whereClause = whereClause + " and "+rider.get(HealthQuoteConstants.RIDER_NAME).asText()+" = "+rider.get("defaultValue").asText();
				}
		}
		log.info("Condition WhereClasue Genrated  : "+whereClause);
		String finalQuery = null;
		if(productInfoNode.has(HealthQuoteConstants.PLANTYPE)){
			if(productInfoNode.get(HealthQuoteConstants.PLANTYPE).asText().equalsIgnoreCase(HealthQuoteConstants.I)){
				finalQuery = HealthQuoteConstants.INDVIDUALPLAN_QUERY;
			}else{
				finalQuery = HealthQuoteConstants.FAMILYPLAN_QUERY;
			}
		}
		finalQuery = finalQuery+" and carrierId="+productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+
				" and planId = "+productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()+whereClause;
		
		log.info("final Query Genrated for MaxBupa : "+finalQuery);
		
		ArrayNode plandetailsNode=null;
		planDetails=serverConfig.executeQuery(finalQuery);
		if(planDetails!=null){
			plandetailsNode = (ArrayNode)objectMapper.readTree(objectMapper.writeValueAsString(planDetails));
		}
		log.info("plandetailsNode maxBupa : "+plandetailsNode);
		if(plandetailsNode.get(0).has("planCode")){
			((ObjectNode)inputReqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM)).put("planCode", plandetailsNode.get(0).get("planCode").asText());
			log.info("MaxBupa Plan COde Picked : "+plandetailsNode.get(0).get("planCode").asText());	
		}else{
			log.info("final Query Genrated for MaxBupa : "+finalQuery);
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|plan Code not found |");
			throw new ExecutionTerminator();
		}
		
		
		
		exchange.getIn().setBody(inputReqNode);
		}catch(Exception e){
			log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|MaxBupaGetPlanCodeProcessor|",e);
			throw new ExecutionTerminator(); 
		}
	}
	
}
