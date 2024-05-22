package com.idep.data.service.impl;

import java.io.IOException;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.data.rto.cache.PopularCityCache;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.data.service.util.DataConstants;

/**
 * 
 * @author sandeep.jadhav
 * get popular RTO list
 */
public class PopularRTOProvider {
	
	Logger log = Logger.getLogger(PopularRTOProvider.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;
	
	 public String getPopularRTO(String input) throws JsonProcessingException, IOException
	 {
	    
		 try
		 {
			 /**
			  * get popular RTO list from cache 
			  */
			 return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_MSG).textValue(),PopularCityCache.getPopularCities());
		 }
		 catch(Exception e)
		 {
				this.log.error("Exception at PopularRTOProvider : ",e);
				return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
		 }
		 
	 }
	

}
