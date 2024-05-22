package com.idep.config.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.config.response.DataCarrierResponse;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

public class AppConfigViewsImpl
{
  ObjectMapper objectMapper = new ObjectMapper();
  JsonNode userInputNode;
  JsonNode docDataNode;
  JsonNode errorNode;
  Logger log = Logger.getLogger(AppConfigViewsImpl.class.getName());
  CBService service = null;
  JsonNode responseConfigNode;
  
  public String getViewData(String viewdata)
  {
    try
    {
      long lStartTime = System.currentTimeMillis();
      this.log.info("getViewData invoked");
      this.userInputNode = this.objectMapper.readTree(viewdata);
      
      if (service == null)
      {
        service = CBInstanceProvider.getServerConfigInstance();
        this.log.info("Server bucket instance created");
        this.log.info("fetching ResponseMessages started");
        this.responseConfigNode = this.objectMapper.readTree(service.getDocBYId("ResponseMessages").content().toString());
        this.log.info("fetching ResponseMessages completed");
      }
      
      List<ObjectNode> viewResult = null;/*service.fetchServerConfigkeyStringView(
    		  									this.userInputNode.get("viewName").textValue(), 
    		  									this.userInputNode.get("viewKey").textValue()
    		  									);*/
      
      long lEndTime = System.currentTimeMillis();
      this.log.info("View Config Service Query Time Elapsed in milliseconds : " + (lEndTime - lStartTime));
      if (viewResult.isEmpty()) {
    	  
    	  return DataCarrierResponse.createResponse(this.responseConfigNode.findValue("noRecordsCode").intValue(), this.responseConfigNode.findValue("noRecordsMessage").textValue(), this.errorNode);
      
      }
      else
      {
    	  this.docDataNode = this.objectMapper.readTree(viewResult.toString());
    	  return DataCarrierResponse.createResponse(this.responseConfigNode.findValue("successCode").intValue(), this.responseConfigNode.findValue("successMessage").textValue(), this.docDataNode);
      }
    }
    catch (JsonProcessingException e)
    {
    	this.log.error("JsonProcessingException at AppConfigViewsImpl : ",e);
    	return DataCarrierResponse.createResponse(this.responseConfigNode.findValue("errorCode").intValue(), this.responseConfigNode.findValue("errorMessage").textValue(), this.errorNode);
    }
    catch (IOException e)
    {
    	this.log.error("IOException at AppConfigViewsImpl : ",e);
    	return DataCarrierResponse.createResponse(this.responseConfigNode.findValue("errorCode").intValue(), this.responseConfigNode.findValue("errorMessage").textValue(), this.errorNode);
    }
    catch (Exception e)
    {
    	this.log.error("Exception at AppConfigViewsImpl : ",e);
    	return DataCarrierResponse.createResponse(this.responseConfigNode.findValue("errorCode").intValue(), this.responseConfigNode.findValue("errorMessage").textValue(), this.errorNode);
    }

  }
}
