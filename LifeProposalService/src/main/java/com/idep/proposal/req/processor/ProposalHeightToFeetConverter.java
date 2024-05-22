package com.idep.proposal.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.text.DecimalFormat;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ProposalHeightToFeetConverter
  implements Processor
{
  Logger log = Logger.getLogger(ProposalHeightToFeetConverter.class);
  ObjectMapper objectMapper = new ObjectMapper();
  
  public void process(Exchange exchange)
    throws Exception
  {
    String proposalRequest = (String)exchange.getIn().getBody(String.class);
    JsonNode proposalReqNode = this.objectMapper.readTree(proposalRequest);
    log.debug("entering in ProposalHeightToFeetConverter");
    Integer proposerHeight = Integer.valueOf(proposalReqNode.get("proposerDetails").get("heightInCM").asInt());
    
    Double Feet = Double.valueOf(proposerHeight.intValue() / 30.48D);
    DecimalFormat F = new DecimalFormat("##.0");
    String HeightinFeet = F.format(Feet).replace(".", "'");
    
    ((ObjectNode)proposalReqNode.get("proposerDetails")).put("heightInFeet", HeightinFeet);
    try
    {
      JsonNode proposalId = proposalReqNode.get("proposalId");
      JsonNode KotakQuoteNumber = proposalReqNode.get("premiumDetails").get("KotakQuoteNumber");
      exchange.setProperty("proposalId", proposalId);
      exchange.setProperty("KotakQuoteNumber", KotakQuoteNumber);
    }
    catch (Exception e)
    {
      this.log.error("failed to read proposalId");
    }
    exchange.getIn().setBody(proposalReqNode);
  }
}
