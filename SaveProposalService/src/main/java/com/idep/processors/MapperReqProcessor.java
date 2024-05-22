package com.idep.processors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class MapperReqProcessor
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(MapperReqProcessor.class.getName());
  
  public void process(Exchange exchange)
    throws Exception
  {
    try
    {
    	String mapperReq = (String)exchange.getIn().getBody(String.class);
    	this.log.info("mapperReq : "+mapperReq);
    	JsonNode mapperReqNode = this.objectMapper.readTree(mapperReq);
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(mapperReqNode.get("carrierRequestForm")));
    }
    catch (Exception e)
    {log.info("Exception : "+e);
     // this.log.error(exchange.getProperty("logReq").toString() + "MAPPERREQPROCESSOR" + "|ERROR|" + "carrier request preparation failed", e);
      
      throw new Exception();
    }
  }
}
