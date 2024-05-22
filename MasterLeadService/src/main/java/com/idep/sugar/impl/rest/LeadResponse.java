package com.idep.sugar.impl.rest;

import com.fasterxml.jackson.databind.JsonNode;

public class LeadResponse
{
  JsonNode node;
  
  public String sendRespose()
  {
    return LeadRequestResponse.createResponse(1000, "success", this.node);
  }
}
