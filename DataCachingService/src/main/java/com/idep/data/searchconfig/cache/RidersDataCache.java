package com.idep.data.searchconfig.cache;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.util.CacheConstants;

public class RidersDataCache {
	
	static ObjectNode ridersconfigDataList = null;
	
	static
	{
		Logger log = Logger.getLogger(RidersDataCache.class.getName());
		
		try {
			
		ObjectMapper objectMapper = new ObjectMapper();
		ridersconfigDataList = objectMapper.createObjectNode();
		CBService productDataService = CBInstanceProvider.getProductConfigInstance();
		CBService serverDataService = CBInstanceProvider.getServerConfigInstance();
		JsonNode docConfigNode = objectMapper.readTree(serverDataService.getDocBYId("SearchCacheQueries").content().toString());
		
		/**
		 * load all business line riders
		 */
		
		log.info("Prepare Product Riders Cache");
		ridersconfigDataList.put("CarRiders", objectMapper.readTree(objectMapper.writeValueAsString(productDataService.executeQuery(docConfigNode.get(CacheConstants.CARRIDERS).get(CacheConstants.QUERY_STRING).asText()))));
		Thread.sleep(1000);
		ridersconfigDataList.put("BikeRiders", objectMapper.readTree(objectMapper.writeValueAsString(productDataService.executeQuery(docConfigNode.get(CacheConstants.BIKERIDERS).get(CacheConstants.QUERY_STRING).asText()))));
		Thread.sleep(1000);
		ridersconfigDataList.put("LifeRiders", objectMapper.readTree(objectMapper.writeValueAsString(productDataService.executeQuery(docConfigNode.get(CacheConstants.LIFERIDERS).get(CacheConstants.QUERY_STRING).asText()))));
		Thread.sleep(1000);
		ridersconfigDataList.put("HealthRiders", objectMapper.readTree(objectMapper.writeValueAsString(productDataService.executeQuery(docConfigNode.get(CacheConstants.HEALTHRIDERS).get(CacheConstants.QUERY_STRING).asText()))));
		Thread.sleep(1000);
		}
		catch(Exception e)
		{
			log.error("unable to load product riders",e);
		}
		
	}

}
