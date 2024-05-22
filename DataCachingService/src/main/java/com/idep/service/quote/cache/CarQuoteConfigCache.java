package com.idep.service.quote.cache;

import java.util.List;
import java.util.Map;
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
 * Cache all configurations documents used in car quote service
 */
public class CarQuoteConfigCache {
	
	static ObjectNode carQuoteDocIdList = null;
	static JsonNode carProductsListNode = null;
	
	static
	{
		Logger log = Logger.getLogger(CarQuoteConfigCache.class.getName());
		
		try
		{
			
			CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
			ObjectMapper objectMapper = new ObjectMapper();
			carQuoteDocIdList = objectMapper.createObjectNode();
			try
			{
				carQuoteDocIdList.put(CacheConstants.CARCARRIER_Q_LIST, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.CARCARRIER_Q_LIST).content().toString()));
			}
			catch(Exception e)
			{
				log.error("failed to load configuration document CarCarrierQList");
			}

			/**
			 * get All CAR Products
			 */
			CBService productConfigService = CBInstanceProvider.getProductConfigInstance();
			/*JsonArray paramObj = JsonArray.create();
			paramObj.add("Y");*/
			log.info("car product query : "+CacheConstants.CAR_PRODUCTS_QUERY);
			List<Map<String, Object>> productList = productConfigService.executeQuery(CacheConstants.CAR_PRODUCTS_QUERY);
			carProductsListNode = objectMapper.readTree(objectMapper.writeValueAsString(productList));
			log.info("car products list size : "+carProductsListNode.size());
		}
		catch(Exception e)
		{
			log.error("failed to load car products : ",e);
		}
		
	}
	
	
	public static JsonNode getcarQuoteDocCache()
	{
			
		return 	carQuoteDocIdList;
			
	}
	
	public static JsonNode getcarProductsCache()
	{
		return 	carProductsListNode;
			
	}
	
	

}
