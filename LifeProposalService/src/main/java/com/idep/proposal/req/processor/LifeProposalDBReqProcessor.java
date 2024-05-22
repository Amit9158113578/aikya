package com.idep.proposal.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class LifeProposalDBReqProcessor
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(LifeProposalDBReqProcessor.class.getName());
  CBService service = null;
  JsonNode serviceConfigNode = null;
  
  public void process(Exchange exchange)
    throws Exception
  {
    try
    {
      String proposalRequest = (String)exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(proposalRequest);
      this.log.info("the req node :" + reqNode);
      
      exchange.getIn().setHeader("documentId", "Policies365-LifeProposalRequest");
      exchange.getIn().setBody(reqNode);
    }
    catch (Exception e)
    {
      this.log.error(exchange.getProperty("logReq").toString() + "LIFEPRODBREQPROCESS" + "|ERROR|" + "life proposal db request failed :", e);
      throw new ExecutionTerminator();
    }
  }
}
