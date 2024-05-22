package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class RSGProposalReqProcessor implements Processor

{
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(RSGProposalReqProcessor.class.getName());
  JsonNode errorNode;
  
  public void process(Exchange exchange) throws ExecutionTerminator{
	  
   JsonNode reqNode = null;   
   try
    {
      
      String proposalRequest = exchange.getIn().getBody(String.class);
      reqNode = this.objectMapper.readTree(proposalRequest);
      log.info("reqNode proposal Response : "+reqNode);
      JsonNode carrierProposalStatus = reqNode.findValue("Result");
      
      for(JsonNode resposne : reqNode){
    	  log.info("resposne interate : "+resposne);
    	  if(resposne.has("Result") && resposne.get("Result").has("statusCode") && resposne.get("Result").get("statusCode").textValue().equalsIgnoreCase("S-001")){
    		  exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.TRUE);
    		  JsonNode inputReqNode = this.objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
        	  ((ObjectNode)inputReqNode.get("coverageDetails")).put("carrierProposalStatus",reqNode.get("PREMIUMDETAILS").get("ReferralStatus").asText());
        	  exchange.getIn().setHeader("documentId", "PostHealthProposalRequest-"+inputReqNode.get(ProposalConstants.CARRIER_ID).intValue());
    	      exchange.getIn().setBody(inputReqNode);
    	  }else{
    		  exchange.getIn().setHeader(ProposalConstants.REQUESTFLAG, ProposalConstants.FALSE);
    		  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.RSPROPOSALSAVEDDETAILSRES+"|ERROR|"+"status code processing failed :"+reqNode);
        	  ObjectNode obj = this.objectMapper.createObjectNode();
    			obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
    			obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
    			obj.put(ProposalConstants.PROPOSAL_RES_DATA, resposne.get("message").asText() +"\t  Quote ID Expired ,Please recaluate quote ");
    			exchange.getIn().setBody(obj);
        	  throw new ExecutionTerminator();
    	  }
    	  
      }
      
      /**
       * check status code sent by carrier
       */
     
      
      if(carrierProposalStatus.get("statusCode").textValue().equals("S-001"))
      {    	  
    	  JsonNode inputReqNode = this.objectMapper.readTree(exchange.getProperty(ProposalConstants.CARRIER_INPUT_REQ).toString());
    	  ((ObjectNode)inputReqNode.get("coverageDetails")).put("carrierProposalStatus",reqNode.get("PREMIUMDETAILS").get("ReferralStatus").asText());
    	  exchange.getIn().setHeader("documentId", "PostHealthProposalRequest-"+inputReqNode.get(ProposalConstants.CARRIER_ID).intValue());
	      exchange.getIn().setBody(inputReqNode);
      }
      else
      {
    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.RSPROPOSALSAVEDDETAILSRES+"|ERROR|"+"status code processing failed :"+reqNode);
    	  ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, ProposalConstants.RESECODEERROR);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, carrierProposalStatus.get("message").asText() +"\t  Quote ID Expired ,PLease recaluate quote ");
			exchange.getIn().setBody(obj);
    	  throw new ExecutionTerminator();
    	  
      }
      
    }
   
    catch (Exception e)
    {
    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+"|ERROR|"+"proposal save details response processing failed :",e);
    	 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.RSPROPOSALSAVEDDETAILSRES+"|ERROR|"+"status code processing failed :"+reqNode);
   	  ObjectNode obj = this.objectMapper.createObjectNode();
			obj.put(ProposalConstants.PROPOSAL_RES_CODE, 1001);
			obj.put(ProposalConstants.PROPOSAL_RES_MSG, ProposalConstants.RESEMSGEERROR);
			obj.put(ProposalConstants.PROPOSAL_RES_DATA, "proposal submission fail , Conatct to Administrator");
			exchange.getIn().setBody(obj);
    		   throw new ExecutionTerminator();   
    }
  }
}