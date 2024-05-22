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
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class FutureHealthPolicyProcessor implements Processor {

ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(FutureHealthPolicyProcessor.class.getName());
	  CBService service =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  public void process(Exchange exchange)throws com.idep.policy.exception.processor.ExecutionTerminator{
		  
		  JsonNode carrierPropRes=null;
		  JsonNode proposalRequest=null;
	  try
	   {
		  String policyRequest = exchange.getIn().getBody(String.class);
	      JsonNode reqNode = this.objectMapper.readTree(policyRequest);
	     // JsonNode healthProposalConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(ProposalConstants.POLICYREQUEST_CONF+productInfoNode.get(ProposalConstants.CARRIER_ID).intValue()+"-"+productInfoNode.get(ProposalConstants.PLAN_ID).intValue()).content().toString());
	      //JsonNode healthProposalConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId("HealthPolicyRequest-37-38").content().toString());
	      JsonDocument document = serverConfig.getDocBYId("HealthPolicyRequest-"+reqNode.get(ProposalConstants.CARRIER_ID).intValue()+ 
	    	      "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
	  	  JsonNode healthProposalConfigNode = objectMapper.readTree(document.content().toString()); 
		   log.info("healthProposalConfigNode" + healthProposalConfigNode);
		    /**
		     * set request configuration document id HealthPolicyRequest
		     */
		    exchange.setProperty(ProposalConstants.POLICYREQ_CONF,healthProposalConfigNode); 
		    		 
	      
		    if(!reqNode.has("healthProposalResponse"))
		    {
		     if(reqNode.get("transactionStausInfo").has("proposalId"))
		      {
		        JsonDocument proposalDocument = service.getDocBYId(reqNode.get("transactionStausInfo").get("proposalId").asText());
		        carrierPropRes=objectMapper.readTree(proposalDocument.content().toString());
			     if(carrierPropRes!=null)
			        {
				       proposalRequest = (JsonNode) carrierPropRes.get(ProposalConstants.PROPOSAL_REQUEST);
					   if(carrierPropRes.has(ProposalConstants.PAYMENT_RESPROCESSOR))
					     {
					       JsonNode paymentResponse = (JsonNode)carrierPropRes.get(ProposalConstants.PAYMENT_RESPROCESSOR);
					       String apPreferId = paymentResponse.get("apPreferId").textValue();
					       JsonDocument paymentDetailsDocument = service.getDocBYId(apPreferId);
					     if(paymentDetailsDocument!=null)
					     {
					    	 JsonNode paymentdocumentNode = objectMapper.readTree(paymentDetailsDocument.content().toString());
					    	 ((ObjectNode)proposalRequest).put("paymentDetails", paymentdocumentNode);
					     }
					     else
					     {
					    	 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.HEALTHPOLICYREQPREPROCE+"|ERROR|"+"health payment details  document not found:"); 
					     }
				     }
				     else
				     {
				    	 log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.HEALTHPOLICYREQPREPROCE+"|ERROR|"+"health payment response details not found:"); 
				     }
			        }
			     else
				     {
				       log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.HEALTHPOLICYREQPREPROCE+"|ERROR|"+"health proposal document details failed:");
				      }
			        ((ObjectNode)proposalRequest).put("requestType", "HealthPolicyRequest");
			      }
			      else
			      {
			    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.HEALTHPOLICYREQPREPROCE+"|ERROR|"+"health proposal details failed:"+reqNode);
				       throw new ExecutionTerminator();
			      }
			      
		     		ArrayNode insuredMembers = (ArrayNode)proposalRequest.get("insuredMembers");	 
		     		int index=1;
		     for(JsonNode member : insuredMembers){
		    	 ((ObjectNode)member).put("memberNo",index );
		    	 index++;
		     }
		     		
		     
		     
		      // set request configuration document id for sutrrMapper
		     
		      exchange.setProperty(ProposalConstants.CARRIER_INPUT_REQ, this.objectMapper.writeValueAsString(carrierPropRes));
		      exchange.setProperty(ProposalConstants.CARRIER_REQ_MAP_CONF, ProposalConstants.POLICYREQUEST_CONF + reqNode.get(ProposalConstants.CARRIER_ID).intValue() + 
		      "-" + reqNode.get(ProposalConstants.PLAN_ID).intValue());
		      ((ObjectNode)reqNode).putAll((ObjectNode)proposalRequest);
		      log.info("Policy reqNode MemberNo Added : " + reqNode);
		     

		      
		     
		    
	      }  
		    exchange.getIn().setBody(reqNode);   
	    }
	    catch (Exception e)
	    {
	    	log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.HEALTHPOLICYREQPREPROCE+"|ERROR|"+"health policy request processor failed:",e);
	        throw new ExecutionTerminator();
	       
	    }
	  }



}