package com.idep.data.rto.cache;

import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
/**
 * 
 * @author sandeep.jadhav
 * create popular RTO cache
 */
public class PopularCityCache {
	
	
	static ArrayNode popularCityList = null;
	
	
	static
	{
		ObjectMapper mapper = new ObjectMapper();
		CBService service = CBInstanceProvider.getServerConfigInstance();
		Logger log = Logger.getLogger(PopularCityCache.class.getName());
		popularCityList = mapper.createArrayNode();
		 
		try {
			
			JsonArray paramConfig = JsonArray.create();
			paramConfig.add("Y");
			String statement = "select regisCode,zone,display,crimeIndex,isCostal,pollutionIndex,trafficeIndex,"
							 + "floodIndex,isEarthQuakeArea,popularCityName,city,state"
							 + " from ServerConfig where documentType='RTODetails'"
							 + " and popularCities=$1";
			
			String distinctCityQuery = "select distinct popularCityName"
					 				 + " from ServerConfig where documentType='RTODetails'"
					 				 + " and popularCities=$1";
			
			log.info("Prepare Popular City Cache : "+statement);
			List<JsonObject> popularCity = service.executeConfigParamArrQuery(statement, paramConfig);
			List<JsonObject> distinctpopularCity = service.executeConfigParamArrQuery(distinctCityQuery, paramConfig);
			
			
			ObjectNode cityObjNodeList = mapper.createObjectNode();
			ObjectNode processedCityList = mapper.createObjectNode();
			
			JsonNode distinctpopularCityNode = mapper.readTree(distinctpopularCity.toString());
			JsonNode popolarCityNode = mapper.readTree(popularCity.toString());
			
			for(JsonNode cityNode : popolarCityNode)
			{
				if(processedCityList.has(cityNode.get("popularCityName").asText()))
				{
					ArrayNode cityArr = (ArrayNode)cityObjNodeList.get(cityNode.get("popularCityName").asText());
					cityArr.add(cityNode);
					cityObjNodeList.put(cityNode.get("popularCityName").asText(), cityArr);
				}
				else
				{
					processedCityList.put(cityNode.get("popularCityName").asText(), "Y");
					ArrayNode cityArr = mapper.createArrayNode();
					cityArr.add(cityNode);
					cityObjNodeList.put(cityNode.get("popularCityName").asText(), cityArr);
				}
				
				
			}
			
			for(JsonNode city : distinctpopularCityNode)
			{
				ObjectNode finalCityList = mapper.createObjectNode();
				finalCityList.put("cityName", city.get("popularCityName").asText());
				finalCityList.put("RTODetails",cityObjNodeList.get( city.get("popularCityName").asText()));
				popularCityList.add(finalCityList);
			}
			
			
		}
		catch(Exception e)
		{
			log.error("Unable to prepare PopularCityCache : ",e);
		}
	}
	/**
	 * 
	 * getPopularCities : method to return popular RTO list
	 */
	public static ArrayNode getPopularCities()
	
	{
		return 	popularCityList;
		
	}
	
	

}
