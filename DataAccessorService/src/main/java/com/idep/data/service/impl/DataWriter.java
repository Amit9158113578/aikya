package com.idep.data.service.impl;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.service.response.DataResponse;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
/**
 * 
 * @author sandeep.jadhav
 * create ServerConfig documents 
 */
public class DataWriter
{
  Logger log = Logger.getLogger(DataWriter.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  JsonNode docDataNode;
  JsonNode errorNode;
  CBService restClient = CBInstanceProvider.getPolicyTransInstance();
  
  public String writeCouchDB(String data) throws JsonProcessingException
  {
    try
    {
	      this.log.info("DataWriter : writeCouchDB invoked");
	      String docid = "Doc-" + getCurrentTimeStamp();
	      docid = docid.replaceAll("\\s", "");
	      this.log.info("document id : " + docid);
	      
	      String status = this.restClient.createAsyncDocument(docid, JsonObject.fromJson(data));
	      
	      if (status.equalsIgnoreCase("doc_created"))
	      {
	        this.log.info("document created successfully");
	        this.docDataNode = this.objectMapper.createObjectNode();
	        ((ObjectNode)this.docDataNode).put("docId", docid);
	        return DataResponse.createResponse(1000, "success", this.docDataNode);
	      }
	      else
	      {
		      this.log.info("Exception while creating document");
		      return DataResponse.createResponse(1002, "failure", this.errorNode);
	      }
    }
    catch (Exception e)
    {
	      e.printStackTrace();
	      return DataResponse.createResponse(1002, "failure", this.errorNode);
    }
   
  }
  
  public Timestamp getCurrentTimeStamp()
  {
    Date date = new Date();
    return new Timestamp(date.getTime());
  }
}
