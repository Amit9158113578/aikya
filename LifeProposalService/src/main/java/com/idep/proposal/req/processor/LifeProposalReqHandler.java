package com.idep.proposal.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class LifeProposalReqHandler
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(LifeProposalReqHandler.class.getName());
  
  public void process(Exchange exchange)
    throws Exception
  {
    try
    {
      String carrierResponse = (String)exchange.getIn().getBody(String.class);
      this.log.info("Carrier Response in LifeProposalReqHandler: " + carrierResponse);
      JsonNode carrierReqNode = this.objectMapper.readTree(carrierResponse);
      
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(carrierReqNode));
    }
    catch (Exception e)
    {
      this.log.error(exchange.getProperty("logReq").toString() + "LIFEPROREQHANDLER" + "|ERROR|" + "life proposal request handler failed :", e);
      throw new ExecutionTerminator();
    }
  }
}
