package com.idep.travelquote.service.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.util.TravelQuoteConstants;

public class ProductPicker {
	Logger log = Logger.getLogger(ProductPicker.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	
	public List<JsonObject> fetchProductsFromDB(JsonNode reqNode, JsonNode TravelProductQueryConfig, CBService service)throws Exception{
		this.log.info("Fetch All TravelProducts from DB using query");
		JsonNode quoteParamNode = reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
		List<JsonObject> productList = null;
		String statement = null;

		JsonNode productPickingQuery = TravelProductQueryConfig.get(TravelQuoteConstants.PRODUCT_PICKING_QUERY);
		JsonNode productPickingQueryParams = TravelProductQueryConfig.get(TravelQuoteConstants.PRODUCT_PICKING_QUERY_PARAMS);
		
		String planType = "I";
		if(quoteParamNode.has("travellers")){
			/**
			 * if only one travellers present then it consider as Individual plan 
			 * and more than 1 then travellers details present then Family plan query we are executing  
			 * */
			ArrayNode travllerList = (ArrayNode)quoteParamNode.get("travellers");
			if(travllerList.size()==1 && travllerList.size()!=0){
				planType="I";
			}else if (travllerList.size()>1 ){
				planType="F";
			}
		}else{
			log.debug("travellers node not found  Plan type by default as I ");
			planType="I";
		}
		
		
		
		((ObjectNode)quoteParamNode).put("planType",planType);
		JsonArray prodArray = JsonArray.create();
		for(JsonNode param : productPickingQueryParams){
			if(param.get(TravelQuoteConstants.DEFAULT_VALUE_STATUS).asBoolean()){
				if(param.get(TravelQuoteConstants.DATA_TYPE).asText().equalsIgnoreCase(TravelQuoteConstants.DATA_AS_TEXT)){
					prodArray.add(param.get(TravelQuoteConstants.DEFAULT_VALUE).asText());
				}else{
					prodArray.add(param.get(TravelQuoteConstants.DEFAULT_VALUE).asInt());
				}
			}else{
				if(param.get(TravelQuoteConstants.DATA_TYPE).asText().equalsIgnoreCase(TravelQuoteConstants.DATA_AS_TEXT)){
					log.debug("key getting : "+reqNode.get(param.get("reqNode").asText()).get(param.get(TravelQuoteConstants.KEY).asText()).asText());
					prodArray.add(reqNode.get(param.get("reqNode").asText()).get(param.get(TravelQuoteConstants.KEY).asText()).asText());
				}else{
					prodArray.add(quoteParamNode.get(param.get(TravelQuoteConstants.KEY).asText()).asInt());
				}
			}
		}
		statement = productPickingQuery.get(TravelQuoteConstants.TRAVEL_PRODUCT).asText();
	/*	if(planType.equals(TravelQuoteConstants.INDIVIDUAL_I) || planType.equals(TravelQuoteConstants.INDIVIDUAL)){
			statement = productPickingQuery.get(TravelQuoteConstants.INDIVIDUAL).asText();
		}else if(planType.equals(TravelQuoteConstants.FAMILY_F) || planType.equals(TravelQuoteConstants.FAMILY)){
			statement = productPickingQuery.get(TravelQuoteConstants.FAMILY).asText();
		}*/
		
		/*if(reqNode.get("travelDetails").get("destinations") != null){
			int totalRidersSelected = reqNode.get("travelDetails").get("destinations").size();
			if(totalRidersSelected == 0){
				statement += " and b.defaultProduct='Y';";
			}else{
				statement += " and b.destinationsApplicable='Y' ORDER BY b.importance;";
				log.info("statement in ProductPicker for destinations: "+statement);
			}
		}else{
			statement += " and b.isDefaultProduct='Y';";
		}
	*/
		/**
		 * Find all continent list from input request for product picking from database
		 */
				
		this.log.info("All TravelProducts DB query : " + statement);
		this.log.info("All TravelProducts DB query params : " + prodArray);
		productList = service.executeConfigParamArrQuery(statement, prodArray);
		return productList;
	}
}
