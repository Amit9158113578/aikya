package com.idep.policy.carrier.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
/**
 * @author sandeep jadhav
 *
 */
public class StarHealthPolicyurlProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(StarHealthPolicyurlProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
    try
    {
      String policyRequest = exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
      exchange.getIn().setHeader("requestURL", exchange.getProperty("requestURL"));
      exchange.getIn().setBody(reqNode);     
    }
    catch (Exception e)
    {
    	log.error("|ERROR|StarsHealthPolicyReqProcessor|",e);
	      throw new ExecutionTerminator();
    }
  }
}
