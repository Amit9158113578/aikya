package com.idep.data.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.data.service.util.DataConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
/**
 * 
 * @author sandeep.jadhav
 * get plan riders for product
 */
public class ProductPlanRidersImpl {
	
	  Logger log = Logger.getLogger(ProductPlanRidersImpl.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode errorNode;
	  CBService service = CBInstanceProvider.getServerConfigInstance();
	  CBService productService = CBInstanceProvider.getProductConfigInstance();
	  
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String getProductPlanRiders(String input) throws JsonProcessingException, IOException
	  {
	    try
	    {
	    	JsonNode inputReqNode = this.objectMapper.readTree(input);
	    	List<JsonObject> productList = null;
	    	JsonArray paramObj = JsonArray.create();
	    	paramObj.add(inputReqNode.get("businessLineId").intValue());
	    	paramObj.add(inputReqNode.get("carrierId").intValue());
	    	paramObj.add(inputReqNode.get("productId").intValue());
	    	String documentType = inputReqNode.get("documentType").textValue();
	    	this.log.debug("parameters : "+paramObj);
	    	String statement = null;
	    	if(inputReqNode.get("businessLineId").intValue()==4)
	    	{

	    	    statement = " select p.plans as plans from ProductData p where p.documentType='"+documentType+"' "
	    					 + " and p.businessLineId=$1"
	    					 + " and p.carrierId=$2"
	    					 + " and p.planId=$3"	
	    						;
	    	    
	    	    this.log.debug("plan query statement : "+statement);
		    	productList = productService.executeConfigParamArrQuery(statement, paramObj);
		    	
		    	if(productList.size()==0)
		    	{
		    		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.NO_RECORDS_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.NO_RECORDS_MSG).textValue(), this.errorNode);
		    	}
		    	else
		    	{
		    		JsonObject planRiders = productList.get(0);
			    	Map<String, Object> ridersMap= planRiders.toMap();
					ArrayList riderArray = (ArrayList)ridersMap.remove("riderList");
					JsonArray addRiderArray = JsonArray.create();
					for(int i=0;i<riderArray.size();i++)
					{
						Map<String,Object> riderMap = (Map<String,Object>)riderArray.get(i);
						if(riderMap.get("riderType").toString().equalsIgnoreCase("PR"))
						{
							JsonObject rider = JsonObject.from(riderMap);
							addRiderArray.add(rider);
						}
					}
		    		JsonNode docDataNode = this.objectMapper.readTree(addRiderArray.toString());
		    		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_MSG).textValue(), docDataNode);
		    	}
	    	    
	    	    
	    	}
	    	else
	    	{

	    	    statement = " select p.riderList as riderList from ProductData p where p.documentType='"+documentType+"' "
	    					 + " and p.businessLineId=$1"
	    					 + " and p.carrierId=$2"
	    					 + " and p.productId=$3"	
	    						;
	    	    
	    	    
	    	    this.log.debug("query statement : "+statement);
		    	productList = productService.executeConfigParamArrQuery(statement, paramObj);
		    	
		    	if(productList.size()==0)
		    	{
		    		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.NO_RECORDS_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.NO_RECORDS_MSG).textValue(), this.errorNode);
		    	}
		    	else
		    	{
		    		JsonObject planRiders = productList.get(0);
			    	Map<String, Object> ridersMap= planRiders.toMap();
					ArrayList riderArray = (ArrayList)ridersMap.remove("riderList");
					JsonArray addRiderArray = JsonArray.create();
					for(int i=0;i<riderArray.size();i++)
					{
						Map<String,Object> riderMap = (Map<String,Object>)riderArray.get(i);
						if(riderMap.get("riderType").toString().equalsIgnoreCase("PR"))
						{
							JsonObject rider = JsonObject.from(riderMap);
							addRiderArray.add(rider);
						}
					}
		    		JsonNode docDataNode = this.objectMapper.readTree(addRiderArray.toString());
		    		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.SUCC_CONFIG_MSG).textValue(), docDataNode);
		    	}
	    	}
	    	
	    }
		catch(Exception e)
		{
			  this.log.error("Error while accessing Product Plan Riders : ",e);
			  return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		}
	  }

}
