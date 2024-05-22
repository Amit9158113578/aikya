package com.idep.lifequote.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.fasterxml.jackson.databind.JsonNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.lifequote.util.LifeQuoteConstants;

public class ProductRatingPicker {
	Logger log = Logger.getLogger(ProductRatingPicker.class.getName());
	
	public List<Map<String, Object>> getProductRatingDetails(JsonNode quoteParamNode, CBService productService) throws Exception{
		this.log.debug("Fetching Product Rating details from DB using query");
		List<Map<String, Object>> ratingList = null;
		JsonArray ratingParam = JsonArray.create();
		ratingParam.add(25);//quoteParamNode.get(LifeQuoteConstants.POLICY_TERM).intValue()
		ratingParam.add(quoteParamNode.get("gender").textValue());
		ratingParam.add(quoteParamNode.get("healthCondition").textValue());
		ratingParam.add(quoteParamNode.get("tobacoAdicted").textValue());
		ratingParam.add(quoteParamNode.get("annualIncome").longValue());
		ratingParam.add(quoteParamNode.get("age").intValue());
		ratingParam.add(quoteParamNode.get("sumInsured").longValue());

		this.log.debug("Product Rating details DB query :" + LifeQuoteConstants.PRODUCT_RATINGS_QUERY + ratingParam);
		ratingList = productService.executeParamArrQuery(LifeQuoteConstants.PRODUCT_RATINGS_QUERY,ratingParam);
		return ratingList;
	}
}
