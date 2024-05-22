package com.idep.data.service.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.productconfig.data.cache.ProductDataConfigCache;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
/**
 * 
 * @author sandeep.jadhav
 * read data from ProductData config bucket
 */
public class ProductDataReader {
	
	  Logger log = Logger.getLogger(ProductDataReader.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode errorNode;
	  CBService productService = CBInstanceProvider.getProductConfigInstance();
	  
	  public String readProductData(String data) throws JsonProcessingException, IOException
	  {
		  try
		  {
			  			  
			  JsonNode inputReqNode = this.objectMapper.readTree(data);
			  String docId = inputReqNode.get("documentType").textValue()+"-"+
					  		 inputReqNode.get("businessLineId").intValue()+"-"+
					  		 inputReqNode.get("carrierId").intValue()+"-"+
					  		 inputReqNode.get("productId").intValue()
					  		 ;
			  
			  /**
			   * check if requested document is already cached
			   */
			  JsonNode cachedDocument = ProductDataConfigCache.getProductDataCache();
			  if(cachedDocument.has(inputReqNode.get("documentType").asText()) &&
					  cachedDocument.get(inputReqNode.get("documentType").asText()).has(docId))
			  {
				  	  log.info("request served from cached data");
					  return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").findValue("successMessage").textValue(), cachedDocument.get(inputReqNode.get("documentType").asText()).get(docId));
			  }
			  
			  else
			  {
				  /**
				   *  fetch document from ProductData bucket
				   */
				  JsonDocument docContent = this.productService.getDocBYId(docId);
				  if(docContent!=null)
				  {
					  JsonNode docDataNode = this.objectMapper.readTree(docContent.content().toString());
					  return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").textValue(), docDataNode);
					  
				  }
				  else
				  {
					  return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("noRecordsMessage").textValue(), this.errorNode);
				  }
			  }
		  }
		  catch(NullPointerException e)
		  {
			  this.log.error("Please check provided input fields to get data : ",e);
			  return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		  }
		  catch(Exception e)
		  {
			  this.log.error("Error while accessing ProductData document : ",e);
			  return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").textValue(), this.errorNode);
		  }
	  }

}
