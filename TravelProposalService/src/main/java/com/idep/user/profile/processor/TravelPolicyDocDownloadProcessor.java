package com.idep.user.profile.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.downloader.impl.DownloaderAPI;
import com.idep.proposal.util.ProposalConstants;
import com.idep.services.impl.ECMSManagerAPI;
import com.idep.user.profile.impl.UserProfileServices;

public class TravelPolicyDocDownloadProcessor implements Processor{


	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(TravelPolicyDocDownloadProcessor.class.getName());
	  CBService policyTransService =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfigService =  CBInstanceProvider.getServerConfigInstance();
	  JsonNode configDocNode=null;
	  JsonNode contentMgmtConfigNode=null;
	  String documentId = null;
	  String downloadURL=null;
	  UserProfileServices profileServices = new UserProfileServices();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try
		{
		String reqBody = exchange.getIn().getBody(String.class);
		JsonNode reqNode = objectMapper.readTree(reqBody);
		log.info("Request Node for Fetch Doc : "+reqNode);
		exchange.setProperty(ProposalConstants.CARRIER_POLICY_DOC_INPUT_REQ, this.objectMapper.writeValueAsString(reqNode));
		JsonObject carrierPropRes = policyTransService.getDocBYId(reqNode.get("proposalId").asText()).content();
		log.info("ProposalId Response : "+carrierPropRes);
		JsonNode fetchPolicyDocNode = objectMapper.readTree(carrierPropRes.get("bikePolicyResponse").toString());
		
		int carrierId = carrierPropRes.getInt("carrierId");
		int productId = carrierPropRes.getInt("productId");
		log.info("Doc Id : "+ProposalConstants.POLICY_DOWNLOAD_CONFIG_DOC+"-"+carrierId+"-"+productId);
		//configDocNode = objectMapper.readTree((serverConfigService.getDocBYId(BikeProposalConstants.POLICY_DOWNLOAD_CONFIG_DOC+"-"+carrierId+"-"+productId).content().toString()));
		
		downloadURL = configDocNode.get("downLoadURL").asText();
		/**
		 * Here we check, is there is any field to replace in URL and form the required URL.
		 */
		if(configDocNode.has("fieldNameReplacement"))
		{
			downloadURL = downloadURL.replace(configDocNode.get("fieldNameReplacement").asText(), fetchPolicyDocNode.get("policyNo").asText());
		}
		if(configDocNode.has("keyReplacement"))
		{
			downloadURL = downloadURL.replace(configDocNode.get("keyReplacement").asText(), fetchPolicyDocNode.get("policydocKey").asText());
		}
		log.info("Formatted URL : "+downloadURL);
		DownloaderAPI downloaderAPI = new DownloaderAPI();
		String fileName = downloaderAPI.downloadFile(configDocNode.get("policyDocSaveLocation").asText(), downloadURL);
		fileName = fileName.substring(fileName.lastIndexOf("/")+1,fileName.length());
		log.info("fileName : "+fileName);
		
		if(!fileName.equals(null))
		{
			int businessLineId = carrierPropRes.getInt("businessLineId");
		//	contentMgmtConfigNode = objectMapper.readTree((serverConfigService.getDocBYId(BikeProposalConstants.CONTENT_MGMT_CONFIG_DOC+"-"+carrierId+"-"+businessLineId).content().toString()));
			ECMSManagerAPI managerAPI = new ECMSManagerAPI();
			 
			JsonObject metaDataObj =JsonObject.create();
			JsonNode proposalJsonNode = objectMapper.readTree(carrierPropRes.toString());
			metaDataObj.put("policyNumber", fetchPolicyDocNode.get("policyNo").asText());
			metaDataObj.put("customerId", proposalJsonNode.get("proposalId").toString());
		    metaDataObj.put("emailId", proposalJsonNode.get("emailId").asText());
		    metaDataObj.put("mobileNumber", proposalJsonNode.get("mobile").asText());
		    metaDataObj.put("policyBond","BikePolicyBond");
		         
		    documentId = managerAPI.uploadPolicyDocument(contentMgmtConfigNode, fileName, metaDataObj);
		    log.info("Document ID : "+documentId);
		    
		    if(documentId != null)
			{
		    	updatePolicyDocument(reqNode,documentId);
			}
		}
		
		
		}
		catch(Exception e)
		{
			log.error("Exception At : BikePolicyDocDownloadProcessor :",e);
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
						policyDetailsNode.put("contentRepoName","Alfresco");
						
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
