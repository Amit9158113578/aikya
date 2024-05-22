package com.idep.policy.carrier.res.processor;

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
public class StarPaymentReqProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(StarPaymentReqProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
    try
    {
      String policyRequest = exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
      log.info("StarPaymentReqProcessor : "+reqNode);
      /**
       *  retrieve proposal response and attached it to policy input request node
       */
     
      JsonObject carrierPropRes=null;
      JsonNode carrierProposalResponse =null;
      ((ObjectNode)reqNode).put("documentType", "carrierProposalResponse");
      ((ObjectNode)reqNode).put("requestType", "HealthPolicyRequest");
      exchange.setProperty(ProposalConstants.HLTHPOLICY_INPUT_REQ, this.objectMapper.writeValueAsString(reqNode));
     
     
       JsonDocument document=null;
       if(reqNode.has(ProposalConstants.PLAN_ID)){
    	   exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYCONF_REQ + reqNode.findValue(ProposalConstants.CARRIER_ID).intValue() + 
    			      "-" + reqNode.findValue(ProposalConstants.PLAN_ID).intValue());
    	   exchange.getIn().setHeader("documentId", ProposalConstants.POLICYREQUEST_CONF + reqNode.findValue(ProposalConstants.CARRIER_ID).intValue() + 
 			      "-" + reqNode.findValue(ProposalConstants.PLAN_ID).intValue());
    	   document= serverConfig.getDocBYId(ProposalConstants.POLICYREQUEST_CONF+reqNode.findValue(ProposalConstants.CARRIER_ID).intValue()+ 
    	      "-" + reqNode.findValue(ProposalConstants.PLAN_ID).intValue());
       }else{
    	   exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYCONF_REQ + reqNode.findValue(ProposalConstants.CARRIER_ID).intValue());
    	   exchange.getIn().setHeader("documentId", ProposalConstants.POLICYREQUEST_CONF + reqNode.findValue(ProposalConstants.CARRIER_ID).intValue());
    	   document = serverConfig.getDocBYId(ProposalConstants.POLICYREQUEST_CONF+reqNode.findValue(ProposalConstants.CARRIER_ID).intValue());
       }
      if(document!=null)
      {
    	  	JsonNode proposalConfigNode = objectMapper.readTree(document.content().toString()); 
    	  	log.debug("healthPolicyNumberConfigNode: "+proposalConfigNode);
  			exchange.setProperty(ProposalConstants.PROPOSALREQ_CONFIG,proposalConfigNode);
      }else{
    	  log.info("Unable to read Document : "+exchange.getProperty(ProposalConstants.CARRIER_REQ_MAP_CONF));
      }
      
      log.info("StarPaymentReqProcessor Set body: "+reqNode);
      exchange.getIn().setBody(reqNode);     
      
    }
    catch (Exception e)
    {
    	log.error("Error at StarPaymentReqProcessor : ",e);
	      throw new ExecutionTerminator();
    }
  }
}
