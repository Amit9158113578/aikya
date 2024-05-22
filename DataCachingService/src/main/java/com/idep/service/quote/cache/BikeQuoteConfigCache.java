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
 * Cache all configurations documents used in bike quote service
 */
public class BikeQuoteConfigCache {
	
	static ObjectNode bikeQuoteDocIdList = null;
	static JsonNode bikeProductsListNode = null;
	
	static
	{
		Logger log = Logger.getLogger(BikeQuoteConfigCache.class.getName());
		
		try
		{
			
			CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
			ObjectMapper objectMapper = new ObjectMapper();
			bikeQuoteDocIdList = objectMapper.createObjectNode();
			try
			{
				bikeQuoteDocIdList.put(CacheConstants.BIKECARRIER_Q_LIST, objectMapper.readTree(serverConfigService.getDocBYId(CacheConstants.BIKECARRIER_Q_LIST).content().toString()));
			}
			catch(Exception e)
			{
				log.error("failed to load configuration document BikeCarrierQList");
			}

			/**
			 * get All Bike Products
			 */
			CBService productConfigService = CBInstanceProvider.getProductConfigInstance();
			/*JsonArray paramObj = JsonArray.create();
			paramObj.add("Y");*/
			log.info("bike product query : "+CacheConstants.BIKE_PRODUCTS_QUERY);
			List<Map<String, Object>> productList = productConfigService.executeQuery(CacheConstants.BIKE_PRODUCTS_QUERY);
			bikeProductsListNode = objectMapper.readTree(objectMapper.writeValueAsString(productList));
			log.info("bike products list size : "+bikeProductsListNode.size());
			
		}
		catch(Exception e)
		{
			log.error("failed to load bike products : ",e);
		}
		
	}
	
	
	public static JsonNode getbikeQuoteDocCache()
	{
			
		return 	bikeQuoteDocIdList;
			
	}
	
	public static JsonNode getbikeProductsCache()
	{
		return 	bikeProductsListNode;
			
	}
	

}
