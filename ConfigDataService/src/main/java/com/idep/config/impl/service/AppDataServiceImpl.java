package com.idep.config.impl.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.config.response.DataCarrierResponse;
import com.idep.config.util.ConfigConstants;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

public class AppDataServiceImpl {
	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(AppDataServiceImpl.class.getName());
	  CBService service = null;
	  JsonNode responseConfigNode;
	  CBService productservice = null;
	  JsonNode searchConfigNode;
	  JsonNode errorNode;
	
	@SuppressWarnings("unchecked")
	public String getApplicationData(String appData)
	  {
		  
		  try
		  {
			  long lStartTime = System.currentTimeMillis();
			  List<JsonObject> list = null;
			  if ((this.service == null) || (this.productservice == null))
		      {
		        this.log.info("Server & Product bucket instance created");
		        this.service = CBInstanceProvider.getServerConfigInstance();
		        this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ConfigConstants.RESPONSE_CONFIG_DOC).content().toString());
		        this.log.info("ResponseMessages configuration loaded");
		        this.productservice = CBInstanceProvider.getProductConfigInstance();
		        this.searchConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ConfigConstants.APPDATA_SEARCH_CONFIG_DOC).content().toString());
		      }
			  
			  JsonNode inputdataNode = this.objectMapper.readTree(appData);
			  String documentType = inputdataNode.get(ConfigConstants.DOCUMENT_TYPE).textValue();
			  JsonNode currentConfigNode = searchConfigNode.get(documentType);
			  String selectFields = currentConfigNode.get(ConfigConstants.DISPLAY_FIELDS).textValue();
			  String bucketName = currentConfigNode.get(ConfigConstants.BUCKET_NAME).textValue();
			  String whereClause = "where documentType='"+documentType+"' and ";
			  JsonArray configParam = JsonArray.create();
			  int count=1;
			  Map<String, Object> fieldsMap = this.objectMapper.readValue(appData, Map.class);
			  fieldsMap.remove(ConfigConstants.DOCUMENT_TYPE);
		      for (Map.Entry<String, Object> field : fieldsMap.entrySet()) {
		    	  
		        if (inputdataNode.get(field.getKey()).isTextual()) {
		        	configParam.add(inputdataNode.get(field.getKey()).textValue());
		        	whereClause =  whereClause + field.getKey()+"=$"+count +" and "  ;
		        } else if (inputdataNode.get(field.getKey()).isInt()) {
		        	configParam.add(inputdataNode.get(field.getKey()).intValue());
		        	whereClause =  whereClause + field.getKey()+"=$"+count +" and " ;
		        }
		        
		        count++;
		      
		      }
		      whereClause = whereClause.substring(0, whereClause.length() - 4);
		      String queryStatement = " select "+selectFields+" from "+bucketName+" "
		    		  				+ whereClause
		    		  				;	
		      this.log.info("select statement formed : "+queryStatement);
		      if(bucketName.equals("ServerConfig"))
		      {
		    	  list = this.service.executeConfigParamArrQuery(queryStatement, configParam);
		      }
		      else
		      {
		    	  list = this.productservice.executeConfigParamArrQuery(queryStatement, configParam); 
		      }
		      long lEndTime = System.currentTimeMillis();
		      this.log.info("SearchService Query Time Elapsed in milliseconds : " + (lEndTime - lStartTime));
		     
		      if(list.isEmpty())
		      {
		    	  return DataCarrierResponse.createResponse(this.responseConfigNode.findValue(ConfigConstants.NO_RECORDS_CODE).intValue(), this.responseConfigNode.findValue(ConfigConstants.NO_RECORDS_MSG).textValue(), this.errorNode);
		      }
		      else
		      {
		    	  JsonNode docDataNode = this.objectMapper.readTree(list.toString());
		    	  return DataCarrierResponse.createResponse(this.responseConfigNode.findValue(ConfigConstants.SUCC_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(ConfigConstants.SUCC_CONFIG_MSG).textValue(), docDataNode);
		      }
			  
		  }
		  catch(Exception e)
		  {
			  this.log.error("Exception at AppDataServiceImpl : ",e);
			  return DataCarrierResponse.createResponse(this.responseConfigNode.findValue(ConfigConstants.ERROR_CONFIG_CODE).intValue(), this.responseConfigNode.findValue(ConfigConstants.ERROR_CONFIG_MSG).textValue(), this.errorNode);
		  }
		  
	  }

}
