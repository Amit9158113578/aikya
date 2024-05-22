package com.idep.rest.impl.service;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.smsemail.response.bean.ResponseSender;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class ReportViewer
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(SMSEmailImplService.class.getName());
  CBService service = null;
  JsonNode errorNode;
  
  @SuppressWarnings("unchecked")
public String viewReport(String reportDetails)
  {
    if (this.service == null)
    {
      this.service = CBInstanceProvider.getServerConfigInstance();
      this.log.info("ServerConfig bucket Instance created");
    }
    try
    {
      JsonNode node = this.objectMapper.readTree(reportDetails);
      
      String doc = ((JsonObject)this.service.getDocBYId("Template-" + node.findValue("funcType").textValue()).content()).toString();
      this.log.info("REPORT VIEW Email template : " + doc);
      Map<String, String> valueMap = objectMapper.readValue(doc, Map.class);
      String reportBody = (String)valueMap.get("body");
      this.log.info("Report Body  : " + reportBody);
      try
      {
        Map<String, String> paramMap = objectMapper.readValue(node.findValue("paramMap").toString(), Map.class);
        this.log.info("map values : " + paramMap.toString());
        
        Iterator<String> iterator = paramMap.keySet().iterator();
        while (iterator.hasNext())
        {
          String paramKey = (String)iterator.next();
          String paramvalue = ((String)paramMap.get(paramKey)).toString();
          reportBody = reportBody.replaceAll("%" + paramKey + "%", paramvalue);
        }
      }
      catch (NullPointerException e)
      {
        this.log.error("paramMap missing in Request");
      }
      this.log.info("reportBody after replacing placeholders : " + reportBody);
      ObjectNode reportData = this.objectMapper.createObjectNode();
      reportData.put("body", reportBody);
      
      return ResponseSender.createResponse(1000, "success", reportData);
    }
    catch (JsonParseException e)
    {
      e.printStackTrace();
      this.log.error("unable to parse JSON input provided, unexpected character occurred");
      return ResponseSender.createResponse(1001, "failure", this.errorNode);
    }
    catch (JsonMappingException e)
    {
      e.printStackTrace();
      this.log.error("Please check input values. Unrecognized field occurred, unable to map input values to bean");
      return ResponseSender.createResponse(1001, "failure", this.errorNode);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      this.log.error("IOException occurred, unable to write response");
      return ResponseSender.createResponse(1001, "failure", this.errorNode);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return ResponseSender.createResponse(1001, "failure", this.errorNode);
    }
    
  }
}
