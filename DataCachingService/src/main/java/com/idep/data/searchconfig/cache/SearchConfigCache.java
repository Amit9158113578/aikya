package com.idep.data.searchconfig.cache;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.util.CacheConstants;

/**
 * 
 * @author sandeep.jadhav
 * cache all configured data.
 * carrier list,riders,disease,occupation,vehicle make and models etc
 */

public class SearchConfigCache {
	
	static ObjectNode configDataList = null;
	
	static
	{
		Logger log = Logger.getLogger(SearchConfigCache.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		configDataList = objectMapper.createObjectNode();
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		
		
		try
		{
			JsonNode docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("SearchCacheQueries").content().toString());
			
			//log.info("car riders : "+objectMapper.readTree(objectMapper.writeValueAsString(productDataService.executeQuery(DocumentDataConfig.getcacheDocList().get(CacheConstants.SEARCHCACHEQUERIES).get(CacheConstants.CARRIDERS).get(CacheConstants.QUERY_STRING).asText()))));
			//Thread.sleep(4000);
			
			//log.info("Bike riders : "+ objectMapper.readTree(productDataService.executeQuery(DocumentDataConfig.getcacheDocList().get(CacheConstants.SEARCHCACHEQUERIES).get(CacheConstants.BIKERIDERS).get(CacheConstants.QUERY_STRING).asText(),DocumentDataConfig.getcacheDocList().get(CacheConstants.SEARCHCACHEQUERIES).get(CacheConstants.BIKERIDERS).get(CacheConstants.SELECT_FIELDS).asText()).toString()));
			//Thread.sleep(4000);
			
			
			/**
			 * get occupation details
			 */
			log.info("prepare occupation data cache");
			configDataList.put("OccupationList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.OCCUPATION_LIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			/**
			 * get Vehicle deductibles configuration
			 */
			log.info("prepare deductible data cache");
			configDataList.put("CarDeductibles", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.CAR_DEDUCTIBLE).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			configDataList.put("BikeDeductibles", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.BIKE_DEDUCTIBLE).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			/**
			 * get Disease List
			 */
			log.info("prepare Disease data cache");
			configDataList.put("DiseaseList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.DISEASELIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			
			/**
			 * get Hospitalization Limit configuration
			 */
			log.info("prepare Hospitalization Limit data cache");
			configDataList.put("HospitalizationLimitList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.HOSPLIMITLIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			
			log.info("prepare masterdata Limit data cache");
			configDataList.put("MasterDataList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.MASTERDATALIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			
			/**
			 * get Bike Mobile Variants
			 */
			log.info("prepare Bike Variant details data cache");
			configDataList.put("BikeVariantsList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.BIKEVARIANTSLIST).get(CacheConstants.QUERY_STRING).asText()))));
			Thread.sleep(1000);
			
		}
		catch(Exception e)
		{
			log.error("failed to cache config data : ",e);
		}
		
	}
	
	public static JsonNode getConfigDataCache()
	{
		configDataList.putAll(RidersDataCache.ridersconfigDataList);
		configDataList.putAll(VehicleDataCache.vehicleconfigDataList);
		configDataList.putAll(CarrierDataCache.carrierconfigDataList);
		configDataList.putAll(RTODataCache.rtoDataList);
		
		return 	configDataList;
			
	}
	
	public static void main(String[] args) {
		
		System.out.println("data : "+SearchConfigCache.getConfigDataCache());
		
	}
		
	

}
