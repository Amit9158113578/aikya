package com.idep.proposal.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.proposal.exception.processor.ExecutionTerminator;

public class LifeProposalServiceImpl
{
  ObjectMapper objectMapper = new ObjectMapper();
  
  public String submitLifeProposal(String proposal)
    throws Exception
  {
    JsonNode reqNode = null;
    try
    {
      reqNode = this.objectMapper.readTree(proposal);
      this.objectMapper.writeValueAsString(reqNode);
      return reqNode.toString();
    }
    catch (Exception e)
    {
      throw new ExecutionTerminator();
    }
  }
  
  public String sendMessage(String proposal)
  {
    return proposal;
  }
}
