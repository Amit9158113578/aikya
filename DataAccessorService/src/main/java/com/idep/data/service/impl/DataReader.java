package com.idep.data.service.impl;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.data.service.response.DataResponse;
import com.idep.data.service.util.DataConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * 
 * @author sandeep.jadhav
 * read configuration documents
 */
public class DataReader
{
  Logger log = Logger.getLogger(DataReader.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  JsonNode docDataNode;
  JsonNode errorNode;
  CBService service = CBInstanceProvider.getServerConfigInstance();
  
  public String readCouchDB(String inputdata) throws JsonProcessingException, IOException
  {
    try
    {
     
    	  /**
    	   * read user input
    	   */
    	  JsonNode userInputNode = this.objectMapper.readTree(inputdata);
    	  JsonDocument docContent = service.getDocBYId(userInputNode.get("documentType").textValue());
    	  
    	  /**
    	   * check if requested document is available
    	   */
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
    catch (JsonParseException e)
    {
      	this.log.error("JsonParseException at DataReader : ",e);
		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
    catch (JsonMappingException e)
    {
    	this.log.error("JsonMappingException at DataReader : ",e);
		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
    catch (IOException e)
    {
    	this.log.error("IOException at DataReader : ",e);
		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
    catch (Exception e)
    {
    	this.log.error("Exception at DataReader : ",e);
		return DataResponse.createResponse(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_CODE).intValue(), DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(DataConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
    }
    
  }
}
