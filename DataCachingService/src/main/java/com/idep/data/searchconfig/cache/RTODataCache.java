package com.idep.data.searchconfig.cache;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.util.CacheConstants;

public class RTODataCache {
	
	
static ObjectNode rtoDataList = null;
	
	static
	
	{
		Logger log = Logger.getLogger(RTODataCache.class.getName());
		
		try {
		
		ObjectMapper objectMapper = new ObjectMapper();
		rtoDataList = objectMapper.createObjectNode();
		CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
		JsonNode docConfigNode = objectMapper.readTree(serverConfigService.getDocBYId("SearchCacheQueries").content().toString());
		
		/**
		 * load Vehicle make model list
		 */
		log.info("Prepare RTO Data Cache");
		rtoDataList.put("RTOList", objectMapper.readTree(objectMapper.writeValueAsString(serverConfigService.executeQuery(docConfigNode.get(CacheConstants.RTOLIST).get(CacheConstants.QUERY_STRING).asText()))));
		
		}
		catch(Exception e)
		{
			log.error("failed to Prepare RTO Data Cache",e);
		}
	}
	
	public static void main(String[] args) {
		
	long startTime = System.currentTimeMillis();	
	System.out.println("rtoDataList : "+rtoDataList);
	ObjectMapper objectMapper = new ObjectMapper();
	ArrayNode rtoList = objectMapper.createArrayNode();
	String searchRegisCode = "rj";
	for(JsonNode node : rtoDataList.get("RTOList"))
	{
		if(node.get("regisCode").asText().startsWith(searchRegisCode.toUpperCase()))
		{
			rtoList.add(node);
		}
		else if(node.get("commonCityName").asText().startsWith(searchRegisCode.toUpperCase()))
		{
			rtoList.add(node);
		}
	}
	
	System.out.println("filtered data : "+rtoList);
	long endTime = System.currentTimeMillis();
	System.out.println("processing time : "+(endTime-startTime));
	
	
	
	
	
	}

}
