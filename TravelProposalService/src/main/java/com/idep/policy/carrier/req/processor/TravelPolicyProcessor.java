package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class TravelPolicyProcessor implements Processor {

ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(TravelPolicyProcessor.class.getName());
	  CBService service =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  public void process(Exchange exchange)throws ExecutionTerminator{
		  
		  JsonNode carrierPropRes=null;
		  JsonNode proposalRequest=null;
	  try
	   {
		  String policyRequest = exchange.getIn().getBody(String.class);
	      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
	      JsonDocument document = serverConfig.getDocBYId("TravelPolicyRequest-"+reqNode.get(ProposalConstants.CARRIER_ID).intValue()+ 
	    	      "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
	  	  JsonNode tarvelProposalConfigNode = objectMapper.readTree(document.content().toString()); 
		   log.info("travelProposalConfigNode" + tarvelProposalConfigNode);
		    /**
		     * set request configuration document id HealthPolicyRequest
		     */
		    exchange.setProperty(ProposalConstants.POLICYREQ_CONF,tarvelProposalConfigNode); 
		    		 
	      
		    if(!reqNode.has("travelProposalResponse"))
		    {
		     if(reqNode.get("transactionStausInfo").has("proposalId"))
		      {
		        JsonDocument proposalDocument = service.getDocBYId(reqNode.get("transactionStausInfo").get("proposalId").asText());
		        carrierPropRes=objectMapper.readTree(proposalDocument.content().toString());
		        log.info("carrierPropRes: "+carrierPropRes);
			     if(carrierPropRes!=null)
			        {
				       proposalRequest = (JsonNode) carrierPropRes.get(ProposalConstants.PROPOSAL_REQUEST);
					   if(carrierPropRes.has("paymentResponse"))
					     {
					       JsonNode paymentResponse = (JsonNode)carrierPropRes.get("paymentResponse");
					       String apPreferId = paymentResponse.get("apPreferId").textValue();
					       JsonDocument paymentDetailsDocument = service.getDocBYId(apPreferId);
					     if(paymentDetailsDocument!=null)
					     {
					    	 JsonNode paymentdocumentNode = objectMapper.readTree(paymentDetailsDocument.content().toString());
					    	 ((ObjectNode)proposalRequest).put("paymentDetails", paymentdocumentNode);
					     }
					     else
					     {
					    	 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPOLICYREQPREPROCE+"|ERROR|"+"travel payment details  document not found:"); 
					     }
				     }
				     else
				     {
				    	 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPOLICYREQPREPROCE+"|ERROR|"+"travel payment response details not found:"); 
				     }
			        }
			     else
				     {
				       log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPOLICYREQPREPROCE+"|ERROR|"+"travel proposal document details failed:");
				      }
			        ((ObjectNode)proposalRequest).put("requestType", "TravelPolicyRequest");
			      }
			      else
			      {
			    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPOLICYREQPREPROCE+"|ERROR|"+"travel proposal details failed:"+reqNode);
				       throw new ExecutionTerminator();
			      }	
		     
		     
		      // set request configuration document id for sutrrMapper
		     
		      exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, this.objectMapper.writeValueAsString(carrierPropRes));
		      exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYREQUEST_CONF + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
		      "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
		      ((ObjectNode)reqNode).putAll((ObjectNode)proposalRequest);
		      log.info("Policy reqNode : " + reqNode);
	      }  
		    exchange.getIn().setBody(reqNode);   
	    }
	    catch (Exception e)
	    {
	    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.TRAVELPOLICYREQPREPROCE+"|ERROR|"+"travel policy request processor failed:",e);
	        throw new ExecutionTerminator();
	       
	    }
	  }



}