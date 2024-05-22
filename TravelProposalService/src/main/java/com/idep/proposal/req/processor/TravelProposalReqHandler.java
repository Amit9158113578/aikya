package com.idep.proposal.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class TravelProposalReqHandler  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(TravelProposalReqHandler.class.getName());
  
  public void process(Exchange exchange)
    throws Exception
  {
    String carrierRequest = exchange.getIn().getBody(String.class);
    JsonNode carrierReqNode = this.objectMapper.readTree(carrierRequest);
    exchange.getIn().setBody(this.objectMapper.writeValueAsString(carrierReqNode.get(ProposalConstants.CARRIER_MAPPER_REQ)));
  }
}