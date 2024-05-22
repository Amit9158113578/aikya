package com.idep.productconfig.data.cache;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.util.Map;

public class ProductDataConfigCache {
	
	static ObjectNode productDataConfigList = null;
	
	
	static
	{
		Logger log = Logger.getLogger(ProductDataConfigCache.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		productDataConfigList = objectMapper.createObjectNode();
		CBService productConfigService = CBInstanceProvider.getProductConfigInstance();
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		
		log.info("Product Config Data loading initiated");
		try
		{
			JsonNode docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("ProductDataCacheList").content().toString());
			
			@SuppressWarnings("unchecked")
			Map<String,Object>configMap =  objectMapper.readValue(docConfigNode.get("documentList").toString(), Map.class);
			
			for(Map.Entry<String, Object> entry : configMap.entrySet())
			{
				@SuppressWarnings("unchecked")
				Map<String,String> confMap =  objectMapper.readValue(objectMapper.writeValueAsString(entry.getValue()),Map.class);
				
				ObjectNode docListNode = objectMapper.createObjectNode();
				for(Map.Entry<String, String> docConfig : confMap.entrySet())
				{
					if(docConfig.getValue().equalsIgnoreCase("Y"))
					{
						docListNode.put(docConfig.getKey(), objectMapper.readTree(productConfigService.getDocBYId(docConfig.getKey()).content().toString()));
					}
					
				}
				
				productDataConfigList.put(entry.getKey(), docListNode);
				
			}
			
			log.info("Product Config Data loading completed");
			
		}
		catch(Exception e)
		{
			log.error("failed to setup product configuration documents : ",e);
		}
		
	}
	
	public static JsonNode getProductDataCache()
	{
		
		return 	productDataConfigList;
			
	}
	
	public static void main(String[] args) {
		
		System.out.println("data : "+ProductDataConfigCache.getProductDataCache());
		
	}

}
