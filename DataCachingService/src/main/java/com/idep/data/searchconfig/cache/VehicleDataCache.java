package com.idep.data.searchconfig.cache;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.util.CacheConstants;

public class VehicleDataCache {
	
	static ObjectNode vehicleconfigDataList = null;
	
	static
	
	{
		Logger log = Logger.getLogger(VehicleDataCache.class.getName());
		
		try {
		
		ObjectMapper objectMapper = new ObjectMapper();
		vehicleconfigDataList = objectMapper.createObjectNode();
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		JsonNode docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("SearchCacheQueries").content().toString());
		/**
		 * load Vehicle make model list
		 */
		log.info("Prepare Vehicle Data Cache");
		vehicleconfigDataList.put("CarModels", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.CARMODELS).get(CacheConstants.QUERY_STRING).asText()))));
		Thread.sleep(2000);
		vehicleconfigDataList.put("BikeModels", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.BIKEMODELS).get(CacheConstants.QUERY_STRING).asText()))));
		Thread.sleep(2000);
		}
		catch(Exception e)
		{
			log.error("Failed to prepare vehicle make model list cache",e);
		}
	}

}
