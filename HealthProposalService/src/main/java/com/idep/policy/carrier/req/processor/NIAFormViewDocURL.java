package com.idep.policy.carrier.req.processor;
/**
 * 
 */
//package com.idep.policy.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;
import com.idep.user.profile.impl.UserProfileServices;

/**
 * @author vipin.patil
 *
 * Jun 1, 2017
 * 
 * Forming the URL for New India Assurance to view or download the document.
 */
public class NIAFormViewDocURL implements Processor{

	
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(NIAFormViewDocURL.class.getName());
	  CBService serverConfigService =  CBInstanceProvider.getServerConfigInstance();
	  CBService policyTransService =  CBInstanceProvider.getPolicyTransInstance();
	  JsonNode configDocNode=null;
	  String downloadURL=null;
	  UserProfileServices profileServices = new UserProfileServices();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try
		{
			String fetchDocServiceRes = exchange.getIn().getBody(String.class);
			JsonNode fetchDocServiceResNode = this.objectMapper.readTree(fetchDocServiceRes);
			JsonNode indexArrayNode = fetchDocServiceResNode.get("indexType");
			JsonNode docsArrayNode = fetchDocServiceResNode.get("docs");
			String policyIndexValue = null;
			
			for(int i=0; i<= docsArrayNode.size();i++)
			{
				if(docsArrayNode.get(i).get("name").asText() != null)
				{
					policyIndexValue = indexArrayNode.get(i).get("index").asText();
					log.info("policyIndexNumber :"+policyIndexValue);
					break;
				}
			}
			JsonNode proposalSampleReqNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.POLICY_UKEYPKEY).toString());
			
			JsonObject carrierPropRes = policyTransService.getDocBYId(proposalSampleReqNode.get(ProposalConstants.PROPOSAL_ID).asText()).content();
			JsonNode fetchPolicyDocNode = objectMapper.readTree(carrierPropRes.get("healthPolicyResponse").toString());
			
			int carrierId = carrierPropRes.getInt("carrierId");
			int productId = carrierPropRes.getInt("productId");
			((ObjectNode)fetchPolicyDocNode).put("indexId", policyIndexValue);
			configDocNode = objectMapper.readTree((serverConfigService.getDocBYId(ProposalConstants.HEALTH_POLICY_DOC_DOWNLOAD_CONFIG+"-"+carrierId+"-"+productId).content().toString()));
			downloadURL = configDocNode.get("downLoadURL").asText();
			/**
			 * Here we check, is there is any field to replace in URL and form the required URL.
			 */
			if(configDocNode.has("fieldNameReplacement"))
			{
				for(JsonNode fieldName : configDocNode.get("fieldNameReplacement"))
				{
					downloadURL = downloadURL.replace(fieldName.get("destFieldName").asText(), fetchPolicyDocNode.get(fieldName.get("sourceFieldName").asText()).asText());
				}
				
			}
			log.info("downloadURL of new India:   "+downloadURL);
			updatePolicyDocument(proposalSampleReqNode,downloadURL);
		}
		catch(Exception e)
		{
			log.error("Exception at NewIndiaFormViewDocumentURL : ",e);
		}
	}
	
	
	public void updatePolicyDocument(JsonNode proposalSampleReqNode,String downloadURL)
	{
				try{
					// get user profile from database
					JsonNode userProfileDetailsNode = profileServices.getUserProfileByUkey(proposalSampleReqNode.get("uKey").asText());
					 
					if(userProfileDetailsNode!=null)
					{
						JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");
						
						ObjectNode policyDetailsNode = objectMapper.createObjectNode();
						policyDetailsNode.put("filePath", downloadURL);
						policyDetailsNode.put("contentRepoType", "ContentRepository");// this path will be read by document download service to get the policy PDF
						
						String mobile = userPersonalInfo.get("mobile").asText();
						/**
						 * update policy details
						 */
						boolean status = profileServices.updatePolicyRecordByPkey(mobile, proposalSampleReqNode.get("pKey").asText(), policyDetailsNode);
						log.info("NEW INDIA policy details updated status : "+status);	
						
					}
					else
			         {
			        	 log.error("User profile not found : UserKey"+proposalSampleReqNode.get("uKey").asText() +"Pkey "+proposalSampleReqNode.get("pKey").asText());
			         }
				}
				catch(Exception e)
				{
					log.error("Failed to update policy document details",e);
				}
	}
}
