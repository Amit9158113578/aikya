package com.idep.proposal.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class MasterPolicyReqProcessor
  implements Processor
{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(MasterPolicyReqProcessor.class.getName());
  
  public void process(Exchange exchange)
  {
    try
    {
      String policyRequest = (String)exchange.getIn().getBody(String.class);
      this.log.info("policyRequest::" + policyRequest);
      JsonNode policyMasterReqNode = this.objectMapper.readTree(policyRequest);
      this.log.info("Life policy creation request received from UI : " + policyMasterReqNode);
      
      ObjectNode policyRequestInfo = this.objectMapper.createObjectNode();
      policyRequestInfo.put("message", "life policy request received");
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(policyRequestInfo));
    }
    catch (Exception ex)
    {
      this.log.info("Exception caught : ", ex);
    }
  }
}
