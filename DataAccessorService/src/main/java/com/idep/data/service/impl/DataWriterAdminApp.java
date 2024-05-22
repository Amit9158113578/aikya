package com.idep.data.service.impl;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.service.response.DataResponse;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
/**
 * 
 * @author sandeep.jadhav
 * write data in AdminApp bucket
 */
public class DataWriterAdminApp
{
  Logger log = Logger.getLogger(DataWriterAdminApp.class.getName());
  ObjectMapper objectMapper = new ObjectMapper();
  JsonNode errorNode;
  CBService service = null;
  
  public String writeAdminAppDB(String data)
  {
    try
    {
    	
      JsonNode docDataNode = null;;
      this.log.info("DataWriter : writeAdminAppDB invoked");
      if (this.service == null)
      {
        this.log.info("Server bucket instance created");
        this.service = CBInstanceProvider.getAdminAppConfigInstance();
      }
      String docid = "Doc-" + getCurrentTimeStamp();
      docid = docid.replaceAll("\\s", "");
      this.log.info("document id : " + docid);
      JsonObject docData = JsonObject.fromJson(data);
      
      String doc_status = this.service.createDocument(docid, docData);
      this.log.info("document created status : " + doc_status);
      if (doc_status.equalsIgnoreCase("doc_created"))
      {
        this.log.info("document created successfully");
        docDataNode = this.objectMapper.createObjectNode();
        ((ObjectNode)docDataNode).put("docId", docid);
        return DataResponse.createResponse(1000, "success", docDataNode);
      }
      else
      {
	      this.log.info("Exception while creating document");
	      return DataResponse.createResponse(1002, "failure", this.errorNode);
      }
    }
    catch (Exception e)
    {
      System.out.println("Exception while writing doc in AdminAppDB");
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
