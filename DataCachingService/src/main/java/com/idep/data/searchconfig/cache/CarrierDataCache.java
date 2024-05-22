package com.idep.data.searchconfig.cache;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.util.CacheConstants;

public class CarrierDataCache {
	
	static ObjectNode carrierconfigDataList = null;
	
	static
	
	{
		
		Logger log = Logger.getLogger(CarrierDataCache.class.getName());
		
		try
		{
			
			
			/**
			 * get carrier logo list
			 */
			
			ObjectMapper objectMapper = new ObjectMapper();
			carrierconfigDataList = objectMapper.createObjectNode();
			CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
			JsonNode docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("SearchCacheQueries").content().toString());
			
			/**
			 *  get carrier list (id, name)
			 */
			carrierconfigDataList.put("CarCarrierList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.CARCARRIERLIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			carrierconfigDataList.put("BikeCarrierList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.BIKECARRIERLIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			carrierconfigDataList.put("LifeCarrierList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.LIFECARRIERLIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			carrierconfigDataList.put("HealthCarrierList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.HEALTHCARRIERLIST).get(CacheConstants.QUERY_STRING).asText()))));
		
			//log.info("get carrier logo list : "+docConfigNode.get(CacheConstants.CARRIERLOGOLIST).get(CacheConstants.QUERY_STRING).asText());
			//log.info("carrier logo : "+serverConfigService.executeQuery("SELECT carrierId,logo FROM `ServerConfig`  WHERE documentType = 'CarrierLogo' ORDER BY carrierId"));
			//carrierconfigDataList.put("CarrierLogoList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(DocumentDataConfig.getcacheDocList().get(CacheConstants.SEARCHCACHEQUERIES).get(CacheConstants.CARRIERLOGOLIST).get(CacheConstants.QUERY_STRING).asText()))));

			carrierconfigDataList.put("CarrierLogoList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery("SELECT carrierId,logo FROM `ServerConfig`  WHERE documentType = 'CarrierLogo' ORDER BY carrierId"))));
			log.info("carrier info completed");
		}
		catch(Exception e)
		{
			log.error("failed to load carrier logo and list",e);
		}
		
	}
	
	

}
