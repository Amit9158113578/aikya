package com.idep.data.service.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.data.service.response.DataResponse;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

public class DataReaderAdminApp {
	
	  Logger log = Logger.getLogger(DataWriterAdminApp.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode docDataNode;
	  JsonNode errorNode;
	  CBService service = null;
	
	 public String readAdminAppDB(String data)
	  {
	    try
	    {
	    	this.log.info("DataReaderAdminApp : readAdminAppDB invoked");
	        if (this.service == null)
	        {
	          this.log.info("Server bucket instance created");
	          this.service = CBInstanceProvider.getAdminAppConfigInstance();
	        }
	        
	        JsonNode inputNode = objectMapper.readTree(data);
	        String statement = " select p.createdDate,p.formStructure from `AdminAppData` p where "
	        				 + " documentType = '"+inputNode.findValue("documentType").textValue()+"'"
	        		//		 + " and carrierId = "+inputNode.findValue("carrierId").intValue()
	        				 + " and businessLineId = "+inputNode.findValue("businessLineId").intValue()
	        		//		 + " and functionalityType = '"+inputNode.findValue("functionalityType").textValue()+"'"
	        				 + " and screenId = "+inputNode.findValue("screenId").intValue()
	        		//		 + " and createdBy = '"+inputNode.findValue("createdBy").textValue()+"'"
	        				 + " group by p.createdDate ORDER BY p.createdDate desc LIMIT 1"
	        				// + " and createdDate = '"+inputNode.findValue("createdDate").textValue()+"'"
	        				 ;
	        this.log.info("form builder data statement : "+statement);
	        List<JsonObject> list = null;
	        list = this.service.executeConfigQuery(statement, "formStructure");
	        
	        
	        if(list.get(0).size()>0)
	        {
	        	this.docDataNode=objectMapper.readTree(list.toString());
	        	return DataResponse.createResponse(1000, "success", docDataNode);
	        }
	        else
	        {
	        
	        	return DataResponse.createResponse(1009, "No Records Found", docDataNode);
	        }
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    	return DataResponse.createResponse(1002, "failure", this.errorNode);
	    }
	  }

}
