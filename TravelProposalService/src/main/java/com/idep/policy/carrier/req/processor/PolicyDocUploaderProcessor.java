package com.idep.policy.carrier.req.processor;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.req.processor.PolicyDocumentMetaData;
import com.idep.proposal.util.ProposalConstants;
import com.idep.services.impl.ECMSManagerAPI;


public class PolicyDocUploaderProcessor {
	
	  	ObjectMapper objectMapper = new ObjectMapper();
	  	Logger log = Logger.getLogger(PolicyDocUploaderProcessor.class.getName());
	  	CBService serverConfigService =  CBInstanceProvider.getServerConfigInstance();
	  	ECMSManagerAPI managerAPI = new ECMSManagerAPI();
	
		public String uploadPolicyDocument(String fileName,String documentId,JsonNode proposalDoc)
		
		{
			try
			{
				JsonNode contentMgmtConfigNode=objectMapper.readTree(serverConfigService.getDocBYId(documentId).content().toString());
				String policyNo = proposalDoc.findValue("policyNo").asText();
				PolicyDocumentMetaData policyMetaData =  new PolicyDocumentMetaData();
				JsonObject metaData = policyMetaData.createMetaData(proposalDoc, proposalDoc.get(ProposalConstants.PROPOSAL_ID).asText(), policyNo);
				String filePath = managerAPI.uploadPolicyDocument(contentMgmtConfigNode, fileName,metaData);
				return filePath;
			}
			catch(Exception e)
			{
				log.error("Exception while uploading document to Alfresco ",e);
				return "";
			}
			
			
		}

}
