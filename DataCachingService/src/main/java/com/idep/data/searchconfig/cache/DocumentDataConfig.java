package com.idep.data.searchconfig.cache;

import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.util.CacheConstants;

/**
 * 
 * @author sandeep.jadhav
 * create configuration documents cache
 */
public class DocumentDataConfig {
	
	static ObjectNode cacheDocumentIdList = null;
	static ObjectNode documentIdList = null;
	static CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(DocumentDataConfig.class.getName());
	
	static
	{
		try {
			
		ObjectMapper objectMapperr = new ObjectMapper();
		cacheDocumentIdList = objectMapperr.createObjectNode();
		documentIdList = objectMapperr.createObjectNode();
		log.info("Preparing document data configuration cache");
		JsonObject SearchCacheQueries = serverConfigService.getDocBYId("SearchCacheQueries").content();
		
		JsonObject documentIdConfig = serverConfigService.getDocBYId("DocumentIDConfig").content();
		JsonObject searchCacheConfig = serverConfigService.getDocBYId("SearchCacheDataConfig").content();
		JsonObject searchConfig = serverConfigService.getDocBYId("SearchConfiguration").content();
		JsonObject responseMessages = serverConfigService.getDocBYId("ResponseMessages").content();
		cacheDocumentIdList.put(CacheConstants.SEARCHCACHEQUERIES, objectMapperr.readTree(SearchCacheQueries.toString()));
		//documentIdList.put(CacheConstants.SEARCHCACHEQUERIES, objectMapperr.readTree(SearchCacheQueries.toString()));
		documentIdList.put(CacheConstants.DOCUMETIDCONFIG, objectMapperr.readTree(documentIdConfig.toString()));
		documentIdList.put(CacheConstants.SEARCHCACHEDATACONF, objectMapperr.readTree(searchCacheConfig.toString()));
		documentIdList.put(CacheConstants.SEARCHCONFIG, objectMapperr.readTree(searchConfig.toString()));
		documentIdList.put(CacheConstants.RESPONSEMESSAGES, objectMapperr.readTree(responseMessages.toString()));
		}
		catch(Exception e)
		{
			log.error("unable to prepare document data configuration cache");
		}
	}
	/*static
	{
		
		
		try
		{
			log.info("DocumentDataConfig initiated");
			
			documentIdList = objectMapper.createObjectNode();
			cacheDocumentIdList = objectMapper.createObjectNode();
			Thread.sleep(1000);
			log.info("fetch configuration documents");
			//log.info("document : "+serverConfigService.getDocBYId("SearchConfigCacheData").content().toString());
			
			
			Thread.sleep(1000);
			try
			{
				documentIdList.put(CacheConstants.DOCUMETIDCONFIG, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.DOCUMETIDCONFIG).content().toString()));
			}
			catch(Exception e)
			{
				log.error("exception occurred while getting DocumentId config document.. retrying");
				documentIdList.put(CacheConstants.DOCUMETIDCONFIG, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.DOCUMETIDCONFIG).content().toString()));
				log.info("DocumentId config document retrieved");
			}
			
			log.info("fetch configuration documents continue....");
			Thread.sleep(1000);
			try
			{
				documentIdList.put(CacheConstants.SEARCHCACHEDATACONF, objectMapper.readTree(serverConfigService.getDocBYId("SearchCacheDataConfig").content().toString()));
			}
			catch(Exception e)
			{
				log.error("exception occurred while getting SearchCacheDataConfig config document.. retrying");
				documentIdList.put(CacheConstants.SEARCHCACHEDATACONF, objectMapper.readTree(serverConfigService.getDocBYId("SearchCacheDataConfig").content().toString()));
				log.info("SearchCacheDataConfig config document retrieved");
			}
			Thread.sleep(1000);
			try
			{
				documentIdList.put(CacheConstants.SEARCHCONFIG, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.SEARCHCONFIG).content().toString()));
			}
			catch(Exception e)
			{
				log.error("exception occurred while getting search config document.. retrying");
				documentIdList.put(CacheConstants.SEARCHCONFIG, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.SEARCHCONFIG).content().toString()));
				log.info("search config document retrieved");
			}
			
			Thread.sleep(5000);
		    
			Thread.sleep(5000);
			
			Thread.sleep(5000);
			

			log.info("document fetch completed");
			
			log.info("fetch config query document");
			cacheDocumentIdList.put(CacheConstants.SEARCHCACHEQUERIES, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.SEARCHCACHEQUERIES).content().toString()));
			log.info("cacheDocumentIdList : "+cacheDocumentIdList);
			//Thread.sleep(3000);
			
			
			log.info("document fetch completed");
			
		}
		catch(Exception e)
		{
			log.error("Exception while creating configuration documents Cache : ",e);
		}
		
	}*/
	
	
	public static JsonNode getcacheDocList()
	{
		/*if(cacheDocumentIdList == null)
		{
			cacheDocumentIdList = objectMapper.createObjectNode();
			JsonObject SearchCacheQueries = serverConfigService.getDocBYId("SearchCacheQueries").content();
			cacheDocumentIdList.put(CacheConstants.SEARCHCACHEQUERIES, objectMapper.readTree(SearchCacheQueries.toString()));
			return 	cacheDocumentIdList;
		}
		else
		{*/
			return 	cacheDocumentIdList;
		//}
			
	}
	
	public static JsonNode getConfigDocList() 
	{
		/*if(documentIdList==null)
		{
			documentIdList = objectMapper.createObjectNode();
			JsonObject documentIdConfig = serverConfigService.getDocBYId("DocumentIDConfig").content();
			JsonObject searchCacheConfig = serverConfigService.getDocBYId("SearchCacheDataConfig").content();
			JsonObject searchConfig = serverConfigService.getDocBYId("SearchConfiguration").content();
			JsonObject responseMessages = serverConfigService.getDocBYId("ResponseMessages").content();
			log.info("json objects gathered");
			documentIdList.put(CacheConstants.DOCUMETIDCONFIG, objectMapper.readTree(documentIdConfig.toString()));
			documentIdList.put(CacheConstants.SEARCHCACHEDATACONF, objectMapper.readTree(searchCacheConfig.toString()));
			documentIdList.put(CacheConstants.SEARCHCONFIG, objectMapper.readTree(searchConfig.toString()));
			documentIdList.put(CacheConstants.RESPONSEMESSAGES, objectMapper.readTree(responseMessages.toString()));
			return 	documentIdList;
		}
		
		else
			{*/
				return 	documentIdList;
			//}
			
	}
	
	public static void main(String[] args) {
		
		//DocumentDataConfig.getConfigDocList();
		
	}

}
