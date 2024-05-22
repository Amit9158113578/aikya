package com.idep.policy.req.processor;

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
public class HealthPolicyReqProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(HealthPolicyReqProcessor.class.getName());
  CBService service =  CBInstanceProvider.getPolicyTransInstance();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
    try
    {
      String policyRequest = exchange.getIn().getBody(String.class);
      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
      log.info("HealthPolicyReqProcessor reqNode: "+reqNode);
      /**
       *  retrieve proposal response and attached it to policy input request node
       */
     if(reqNode.has("responseCode") && reqNode.get("responseCode").asText().equalsIgnoreCase("P365RES100")){
    	 reqNode=reqNode.get("data");
     }
      JsonObject carrierPropRes=null;
      JsonNode carrierProposalResponse =null;
      carrierPropRes= service.getDocBYId(reqNode.get("transactionStausInfo").get("proposalId").asText()).content();

      if(carrierPropRes!=null){
    	  carrierProposalResponse = objectMapper.readTree(carrierPropRes.get("healthProposalResponse").toString());
    	  log.info("HealthPolicyReqProcessor carrierProposalResponse: "+carrierProposalResponse);
    	  
      }else{
    	  log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|unable to load Proposal Document :  "+reqNode.get("transactionStausInfo").get("proposalId").asText());
    	  throw new ExecutionTerminator();
      }
      
      ((ObjectNode)reqNode).put("carrierProposalResponse", carrierProposalResponse);
      ((ObjectNode)reqNode).put("documentType", "carrierProposalResponse");
      ((ObjectNode)reqNode).put("requestType", "HealthPolicyRequest");
      ((ObjectNode)reqNode).put("proposalId", reqNode.get("transactionStausInfo").get("proposalId").asText());
      ((ObjectNode)reqNode).put("planId", reqNode.get("productId").intValue());
      // exchange.getIn().setBody(reqNode);   
      
       /**
       * add policy no if carrier provides it in payment response
       */
      
      log.info("HealthPolicyReqProcessor has "+reqNode.get(ProposalConstants.TRANSSTATUSINFO).has("policyNo"));
      
      if(reqNode.get(ProposalConstants.TRANSSTATUSINFO).has("policyNo"))
      {
    	  log.info("reqNode: ");
    	  ((ObjectNode)reqNode).put("policyNo", reqNode.get("transactionStausInfo").get("policyNo").asText());
    	  log.info("reqNode: "+reqNode);
      }
      
      if(!(reqNode.has(ProposalConstants.CARRIER_ID)))
      {
    	  
    	  log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|carrier id is missing in request , Added from Propoosal "+reqNode.get("transactionStausInfo").get("proposalId").asText());
    	  
    	  ((ObjectNode)reqNode).put(ProposalConstants.CARRIER_ID, carrierPropRes.getInt(ProposalConstants.CARRIER_ID));
    	  //throw new ExecutionTerminator();
      }
      if(!(reqNode.has(ProposalConstants.PLAN_ID)))
      {
    	  log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|plan id is missing in request , Added from Propoosal|"+reqNode.get("transactionStausInfo").get("proposalId").asText());
    	  //log.info("plan id is missing in request , Added from Propoosal "+reqNode.get("transactionStausInfo").get("proposalId").asText());
    	  ((ObjectNode)reqNode).put(ProposalConstants.PLAN_ID , carrierPropRes.getInt(ProposalConstants.PRODUCTID));
      }
      
      /**
       *  set request configuration document id for mapper
       */
      exchange.setProperty(ProposalConstants.HLTHPOLICY_INPUT_REQ, this.objectMapper.writeValueAsString(reqNode));
     
      exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYCONF_REQ + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
      "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
      
      JsonDocument document = serverConfig.getDocBYId("HealthPolicyRequest-"+reqNode.get(ProposalConstants.CARRIER_ID).intValue()+ 
    		  "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
      log.info("HealthPolicyReqProcessor document: "+document);

      if(document!=null)
      {
    	  	JsonNode proposalConfigNode = objectMapper.readTree(document.content().toString());
    	  	log.info("HealthPolicyReqProcessor healthPolicyNumberConfigNode: "+proposalConfigNode);
    	  	log.debug("healthPolicyNumberConfigNode: "+proposalConfigNode);
  			exchange.setProperty(ProposalConstants.PROPOSALREQ_CONFIG,proposalConfigNode);
      } 
      exchange.getIn().setBody(reqNode);     
      log.info("HealthPolicyReqProcessor reqNode 3 : "+reqNode);
    }
    catch (Exception e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|HealthPolicyReqProcessor|",e);
	      throw new ExecutionTerminator();
    }
  }
}
