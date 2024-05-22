package com.idep.assisment.info.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class MIASummaryInfoProcessor
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(MIASummaryInfoProcessor.class);
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  GetMIAResponseProcessor pqc = new GetMIAResponseProcessor();
  
  
  public void process(Exchange exchange)
    throws Exception
  {
	  String  questionConfig = null;
    try
    {
      JsonNode request = this.objectMapper.readTree((String)exchange.getIn().getBody(String.class));
      log.info("MIA Assessment Request recived for Processing : "+request);
      if (request.has("professionCode"))
      {
        questionConfig = this.pqc.getMiaSummaryConfig(request);
      }
      ObjectNode responseNode = this.objectMapper.createObjectNode();
      if(questionConfig!=null){
    	  
          responseNode.put("responseCode", "1000");
          responseNode.put("message", "success");
          log.info("MIA response generated for : " +questionConfig );
          responseNode.put("data", questionConfig);
      }else{
    	  responseNode.put("responseCode", "1001");
          responseNode.put("message", "failure");
          responseNode.put("data", "");
    	  log.error("MIA assessment response not found : "+request);
      }
       this.log.info("in get MIASummaryInfoProcessor responsssseeee" + responseNode);
      exchange.getIn().setBody(responseNode);
    }
    catch (Exception e)
    {
      this.log.error("unable to process MIASummaryInfoProcessor  request : ", e);
    }
  }
}
