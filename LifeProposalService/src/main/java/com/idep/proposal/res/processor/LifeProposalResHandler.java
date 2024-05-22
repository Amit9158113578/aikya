package com.idep.proposal.res.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class LifeProposalResHandler
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(LifeProposalResHandler.class.getName());
  
  public void process(Exchange exchange)
    throws Exception
  {
    try
    {
      String carrierResponse = (String)exchange.getIn().getBody(String.class);
      JsonNode carrierResNode = this.objectMapper.readTree(carrierResponse);
      
      JsonNode requestDocNode = this.objectMapper.readTree(exchange.getProperty("carrierInputRequest").toString());
      this.log.info("inside LifeProposalResHandler" + requestDocNode);
      
      ((ObjectNode)requestDocNode).put("carrierResponse", carrierResNode);
      ((ObjectNode)requestDocNode).put("requestType", "LifeProposalResponse");
      
      exchange.setProperty("carrierReqMapConf", "LifeProposalResponse-" + 
        requestDocNode.get("carrierId").intValue() + "-" + 
        requestDocNode.get("productId").intValue());
      
      exchange.setProperty("carrierInputRequest", this.objectMapper.writeValueAsString(carrierResNode));
      this.log.info("setted property of carrierInputRequest " + carrierResNode);
      
      this.log.info("The node in LifeProposalResHandler" + requestDocNode);
      
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
    }
    catch (Exception e)
    {
      this.log.error(exchange.getProperty("logReq").toString() + "LIFEPRORESHANDL" + "|ERROR|" + "life proposal response handler failed:", e);
      throw new ExecutionTerminator();
    }
  }
}
