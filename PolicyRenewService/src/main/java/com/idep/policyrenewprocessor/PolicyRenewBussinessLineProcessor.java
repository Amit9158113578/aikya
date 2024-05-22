package com.idep.policyrenewprocessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class PolicyRenewBussinessLineProcessor implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  
  public void process(Exchange exchange)
    throws Exception
  {
    Logger log = Logger.getLogger(PolicyRenewBussinessLineProcessor.class.getName());
    String request = exchange.getIn().getBody().toString();
    JsonNode reqNode = this.objectMapper.readTree(request);
    log.info("Processing Recieved in PolicyRenewBussinessLineProcessor ");
    if ((reqNode.has("proposalId")) && (reqNode.get("proposalId").asText() != null)) {
      if (reqNode.get("proposalId").asText().contains("PROP000L"))
      {
        log.info("Its Life");
        exchange.getIn().setHeader("LOB", "Life");
        exchange.getIn().setBody(reqNode);
      }
      else if (reqNode.get("proposalId").asText().contains("PROP000B"))
      {
        log.info("Its Bike");
        exchange.getIn().setHeader("LOB", "Bike");
        exchange.getIn().setBody(reqNode);
      }
      else if (reqNode.get("proposalId").asText().contains("PROP000C"))
      {
        log.info("Its Car");
        exchange.getIn().setHeader("LOB", "Car");
        exchange.getIn().setBody(reqNode);
      }
      else if (reqNode.get("proposalId").asText().contains("PROP000H"))
      {
        log.info("Its Health");
        exchange.getIn().setHeader("LOB", "Health");
        exchange.getIn().setBody(reqNode);
      }
    }
  }
}
