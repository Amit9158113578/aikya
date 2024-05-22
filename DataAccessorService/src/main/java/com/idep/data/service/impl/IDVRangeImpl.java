package com.idep.data.service.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.data.service.util.DataConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
/**
 * 
 * @author sandeep.jadhav
 * get min and max idv range for provided quote id
 */
public class IDVRangeImpl {
	
	Logger log = Logger.getLogger(IDVRangeImpl.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;
	CBService transService = CBInstanceProvider.getBucketInstance(DataConstants.QUOTE_BUCKET);
	CBService service = CBInstanceProvider.getServerConfigInstance();
	JsonNode responseConfigNode;
	
	public String getIDVDetails(String quoteId) throws JsonProcessingException, IOException
	{
		try
		{
			JsonNode quoteDataNode = this.objectMapper.readTree(quoteId);
			JsonNode docDataNode = this.objectMapper.readTree(this.transService.getDocBYId(quoteDataNode.get("QUOTE_ID").textValue()).content().toString());
			ObjectNode idvRangeData = this.objectMapper.createObjectNode();
			
			idvRangeData.put("lowestIDV", ((Double)docDataNode.get("lowestIDV").doubleValue()).longValue());
			idvRangeData.put("highestIDV", ((Double)docDataNode.get("highestIDV").doubleValue()).longValue());
			
			return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_MSG).textValue(), idvRangeData);
		}
		catch(Exception e)
		{
			this.log.error("Exception at IDVRangeImpl : ",e);
			return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
		}
	}
	
}
