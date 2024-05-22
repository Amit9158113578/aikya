package com.idep.lifequote.service.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.lifequote.util.LifeQuoteConstants;

public class ProductPicker {
	Logger log = Logger.getLogger(ProductPicker.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	
	public List<JsonObject> fetchProductsFromDB(JsonNode quoteParamNode, JsonNode LifeProductQueryConfig, CBService service)throws Exception{
		this.log.debug("Fetch All LifeProducts from DB using query");
		List<JsonObject> productList = null;
		String statement = null;

		JsonNode productPickingQuery = LifeProductQueryConfig.get(LifeQuoteConstants.PRODUCT_PICKING_QUERY);
		JsonNode productPickingQueryParams = LifeProductQueryConfig.get(LifeQuoteConstants.PRODUCT_PICKING_QUERY_PARAMS);

		this.log.debug("productPickingQuery : " + productPickingQuery);
		this.log.debug("productPickingQueryParams : " + productPickingQueryParams);
		
		//this.log.info("productPickingQuery : " + productPickingQuery);
		//this.log.info("productPickingQueryParams : " + productPickingQueryParams);
		this.log.debug("quoteParamNode : " + quoteParamNode);
		
		String gender = quoteParamNode.get("gender").textValue();
		String smokerStatus = quoteParamNode.get("tobacoAdicted").textValue();
		int payoutId = quoteParamNode.get(LifeQuoteConstants.PAYOUT_ID).intValue();

		JsonArray payoutIdArray = JsonArray.create();
		payoutIdArray.add(payoutId);
		payoutIdArray.add(LifeQuoteConstants.COMMON_PAYOUT_ID);

		JsonArray prodArray = JsonArray.create();
		for(JsonNode param : productPickingQueryParams){
			if(param.get("defaultValStatus").asBoolean()){
				if(param.get("dataType").asText().equalsIgnoreCase("text")){
					prodArray.add(param.get("defaultValue").asText());
				}else{
					prodArray.add(param.get("defaultValue").asInt());
				}
			}else{
				if(param.get("dataType").asText().equalsIgnoreCase("text")){
					prodArray.add(quoteParamNode.get(param.get("key").asText()).asText());
				}else{
					prodArray.add(quoteParamNode.get(param.get("key").asText()).intValue());
				}
			}
		}
		prodArray.add(payoutIdArray);

		if((gender.equals("M") || gender.equals("Male") )&& (smokerStatus.equals("Y"))){
			statement = productPickingQuery.get("smokerMale").asText();
		}else if((gender.equals("M") || gender.equals("Male")) && (smokerStatus.equals("N"))){
			statement = productPickingQuery.get("nonSmokerMale").asText();
		}else if((gender.equals("F") || gender.equals("Female")) && (smokerStatus.equals("Y"))){
			statement = productPickingQuery.get("smokerFemale").asText();
		}else if((gender.equals("F") || gender.equals("Female")) && (smokerStatus.equals("N"))){
			statement = productPickingQuery.get("nonSmokerFemale").asText();
		}
		
		if(quoteParamNode.get(LifeQuoteConstants.RIDERS) != null){
			int totalRidersSelected = quoteParamNode.get(LifeQuoteConstants.RIDERS).size();
			if(totalRidersSelected == 0){
				statement += " and b.defaultProduct='Y'";
			}else{
				statement += " and b.riderApplicable='Y' ORDER BY b.importance";
			}
		}else{
			statement += " and b.defaultProduct='Y'";
		}

		this.log.debug("All LifeProducts DB query : " + statement);
		this.log.debug("All LifeProducts DB query params : " + prodArray);
		productList = service.executeConfigParamArrQuery(statement, prodArray);
		return productList;
	}
}
