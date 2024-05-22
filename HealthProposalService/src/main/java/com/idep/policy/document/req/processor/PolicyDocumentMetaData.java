/**
 * @author Pravin.Jakhi
 */
package com.idep.policy.document.req.processor;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;
/**
 * @author pravin.jakhi
 *  this return purpose is get the proposal number and read and send emailId,mobileNumber,PolicyBond,PolicyNumber
 * 
 */
public class PolicyDocumentMetaData {
	CBService policyTransService =  CBInstanceProvider.getPolicyTransInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	public JsonObject createMetaData(JsonNode proposalDoc , String proposalNo,String PolicyNumber) throws JsonProcessingException, IOException{
		JsonObject metaDataNode = JsonObject.create(); 
		
		/**
		 * proposalDoc if null then proposal Doc reading from DB
		 * */
	if(proposalDoc==null){
		proposalDoc = objectMapper.readTree(policyTransService.getDocBYId(proposalNo).content().toString());
		
		if(PolicyNumber!=null && PolicyNumber.equalsIgnoreCase("")){
		metaDataNode.put("policyNumber", PolicyNumber);
		}else{
			metaDataNode.put("policyNumber", proposalDoc.findValue("policyNo").asText());	
		}
		metaDataNode.put("customerId", proposalDoc.get(ProposalConstants.PROPOSAL_ID).asText());
		metaDataNode.put("emailId", proposalDoc.get("emailId").asText());
		metaDataNode.put("mobileNumber",proposalDoc.get("mobile").asText());
		metaDataNode.put("policyBond",ProposalConstants.POLICYBOND);
	}else{
		if(PolicyNumber!=null && PolicyNumber.equalsIgnoreCase("")){
			metaDataNode.put("policyNumber", PolicyNumber);
			}else{
				metaDataNode.put("policyNumber", proposalDoc.findValue("policyNo").asText());	
			}
		metaDataNode.put("customerId", proposalDoc.get(ProposalConstants.PROPOSAL_ID).asText());
		metaDataNode.put("emailId", proposalDoc.get("emailId").asText());
		metaDataNode.put("mobileNumber",proposalDoc.get("mobile").asText());
		metaDataNode.put("policyBond",ProposalConstants.POLICYBOND);
	}
	return metaDataNode ;
	}
	
}
