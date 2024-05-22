package com.idep.processors;
/**
 * 
 * @author suraj.huljute
 * 30-NOV-2018
 * In this service we are 
 * 1.creating proposal
 * 2.updating proposal using proposalId
 * 3.forwarding same proposalId to respective service (like SubmitCarProposal)
 * 4.updating proposalId in CRM by putting request in leads queue.  
 */

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.util.SaveProposalConstants;

public class SaveProposalProcessor implements Processor {
	static ObjectMapper objectMapper = new ObjectMapper();
	static Logger log = Logger.getLogger(SaveProposalProcessor.class.getName());
	ObjectNode leadProfileNode = this.objectMapper.createObjectNode();
	static CBService policyTransService=null;
	String messageId=null;
	static
    {try{
    	policyTransService =  CBInstanceProvider.getPolicyTransInstance();
    }catch(Exception e){
    	log.error("Error Occured while creating instance of PolicyTransaction Bucket");
    }
    }
	
	public void process(Exchange exchange)throws Exception
	   { 
		String proposalRequestData = (String)exchange.getIn().getBody(String.class);
		JsonNode reqNode = objectMapper.readTree(proposalRequestData);
		try
 	    {
			//calcProposal added in condition for ramp at the time of lead we are creating proposal document.
		      	if(!reqNode.has(SaveProposalConstants.PROPOSALID)){ 
		      		if(reqNode.has(SaveProposalConstants.MESSAGE_ID)&& reqNode.has(SaveProposalConstants.BUSINESS_LINE_ID)&& reqNode.has(SaveProposalConstants.QUOTE_ID)){
		      		messageId=reqNode.get(SaveProposalConstants.MESSAGE_ID).asText();
				    leadProfileNode=((ObjectNode)objectMapper.readTree(((JsonObject)policyTransService.getDocBYId(SaveProposalConstants.LEAD_PROFILE+"-"+messageId).content()).toString()));          	 
			      	if(leadProfileNode!=null){
				 		leadProfileNode=((ObjectNode)leadProfileNode.get(SaveProposalConstants.LEADDETAILS));
				      	//((ObjectNode)leadProfileNode).put(SaveProposalConstants.BUSINESS_LINE_ID,businessLineId);
				 		leadProfileNode.putAll((ObjectNode)reqNode);
				 		log.info("Request for Mapper : "+leadProfileNode);
				      	exchange.setProperty(SaveProposalConstants.CARRIER_REQ_MAP_CONF,SaveProposalConstants.SAVE_PROPOSAL_CONF);
				        exchange.getIn().setBody(this.objectMapper.writeValueAsString(leadProfileNode));
				        exchange.getIn().setHeader(SaveProposalConstants.PROPOSAL_FLAG,SaveProposalConstants.FALSE);
				        exchange.setProperty(SaveProposalConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(leadProfileNode));
				        exchange.getIn().setHeader( SaveProposalConstants.REQ_FLAG, SaveProposalConstants.TRUE);
				        exchange.getIn().setHeader(SaveProposalConstants.DOCUMENTID, SaveProposalConstants.SAVE_PROPOSAL_CONF);
				        exchange.getIn().setHeader(SaveProposalConstants.EXCEPTION_FLAG,SaveProposalConstants.FALSE);
			      	}else{
		      		throw new NullPointerException(SaveProposalConstants.ERR_FETCH_DOC+SaveProposalConstants.SAVE_PROPOSAL_CONF);
		      	}
		      	}else{
					throw new NullPointerException(SaveProposalConstants.ERR_MESSAGEID_BUSINESSID);
				}
			}
		   else{
				 exchange.getIn().setBody(this.objectMapper.writeValueAsString(reqNode));
	      		 exchange.getIn().setHeader(SaveProposalConstants.PROPOSAL_FLAG,SaveProposalConstants.TRUE);
			     exchange.getIn().setHeader(SaveProposalConstants.EXCEPTION_FLAG,SaveProposalConstants.FALSE);

	      	}
       }catch(NullPointerException e){
		ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
		saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.FAILURE_CODE);
		saveProposalResponse.put(SaveProposalConstants.RES_MSG,e.getMessage());
		saveProposalResponse.put(SaveProposalConstants.RES_DATA,SaveProposalConstants.FAILURE);
	    exchange.getIn().setBody(saveProposalResponse);
	    exchange.getIn().setHeader(SaveProposalConstants.EXCEPTION_FLAG,SaveProposalConstants.TRUE);
	      
    }
catch(Exception e)
    {
		ObjectNode saveProposalResponse = this.objectMapper.createObjectNode();
		saveProposalResponse.put(SaveProposalConstants.RES_CODE,SaveProposalConstants.FAILURE_CODE);
		saveProposalResponse.put(SaveProposalConstants.RES_MSG,e.getMessage());
		saveProposalResponse.put(SaveProposalConstants.RES_DATA,SaveProposalConstants.FAILURE);
	    exchange.getIn().setBody(saveProposalResponse);    
	    exchange.getIn().setHeader(SaveProposalConstants.EXCEPTION_FLAG,SaveProposalConstants.TRUE);
    }
 	     }
}