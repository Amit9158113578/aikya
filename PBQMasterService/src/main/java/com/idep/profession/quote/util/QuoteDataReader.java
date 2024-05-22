package com.idep.profession.quote.util;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class QuoteDataReader {
	
	static ObjectMapper mapper = new ObjectMapper();
	static Logger log = Logger.getLogger(QuoteDataReader.class);
	static CBService QuoteData = CBInstanceProvider.getBucketInstance("QuoteData");

	@SuppressWarnings("unchecked")
	public static JsonNode updateSelection(JsonNode reqNode) throws IOException
	{		
		String quoteID = reqNode.get("QUOTE_ID").textValue();
		log.info("Fetched Quote ID :"+quoteID);
		JsonNode QuoteID = mapper.readTree(QuoteData.getDocBYId(quoteID).content().toString());
		((ObjectNode)QuoteID).put("selectedProduct",reqNode.get("selectedProduct").textValue());
		((ObjectNode)QuoteID).put("selectedCarrier",reqNode.get("selectedCarrier").textValue());
		if(reqNode.has("selectedChildPlanId"))
		{
			((ObjectNode)QuoteID).put("selectedChildPlanId",reqNode.get("selectedChildPlanId").textValue());
		}
		return QuoteID;
	}
}
