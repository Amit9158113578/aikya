package com.idep.policy.document.req.processor;

import javax.xml.bind.DatatypeConverter;

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
import com.idep.downloader.impl.*;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;
import com.idep.services.impl.ECMSManagerAPI;
import com.idep.user.profile.impl.UserProfileServices;

public class CignaPolicyDocumentReqProcessor implements Processor {

	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(CignaPolicyDocumentReqProcessor.class.getName());
	  CBService policyTransService =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfigService =  CBInstanceProvider.getServerConfigInstance();
	  JsonNode configDocNode=null;
	  String downloadURL=null;
	  String documentId = null;
	  JsonNode contentMgmtConfigNode=null;
	  UserProfileServices profileServices = new UserProfileServices();
	  
	  public void process(Exchange exchange) throws ExecutionTerminator {
		  
		    try
		    { 	
		      String policyDocRequest = exchange.getIn().getBody(String.class);
		      JsonNode reqNode = this.objectMapper.readTree(policyDocRequest);
		      String policyNo = reqNode.get("policyNo").asText();
		      JsonNode carrierPropRes =null;
		      try{
		    	  carrierPropRes= objectMapper.readTree(policyTransService.getDocBYId(reqNode.get("proposalId").asText()).content().toString());
		      }catch(Exception e){
		    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCREQ+"|ERROR|CignaPolicyDocumentReqProcessor|",e);
					throw new ExecutionTerminator();
		      }
		      if(carrierPropRes==null){
		    	  log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCREQ+"|ERROR|Document not found|"+reqNode.get("proposalId").asText());
					throw new ExecutionTerminator();
		      }
		      String emailId = carrierPropRes.get("emailId").asText();
		      String password = carrierPropRes.get("dateOfBirth").asText().replaceAll("/", "");
		      log.info("Cignapassword: "+password);
		      log.info("Base64password: "+DatatypeConverter.printBase64Binary(password.getBytes()));
		      log.debug("Base64PolicyNumber: "+DatatypeConverter.printBase64Binary(policyNo.getBytes()));
		      log.debug("Base64emailId: "+DatatypeConverter.printBase64Binary(emailId.getBytes()));
		      
		      /**
		       * Insert base 64 values of email and policynumber
		       */
		      ((ObjectNode)reqNode).put("policyNo", DatatypeConverter.printBase64Binary(policyNo.getBytes()));
		      ((ObjectNode)reqNode).put("emailId", DatatypeConverter.printBase64Binary(emailId.getBytes()));
		      ((ObjectNode)reqNode).put("password", DatatypeConverter.printBase64Binary(password.getBytes()));
		      
		      configDocNode = objectMapper.readTree((serverConfigService.getDocBYId("HealthPolicyDocDownloadConfig-35-4").content().toString()));
		      if(configDocNode!=null){
		      downloadURL = configDocNode.get("downLoadURL").asText();
		      }else{
		    	  log.info(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|Document not found|HealthPolicyDocDownloadConfig-35-4");
					throw new ExecutionTerminator();
		      }
		      /**
			   * Here we check, is there is any field to replace in URL and form the required URL.
			   */
		      if(configDocNode.has("fieldNameReplacement"))
				{
					for(JsonNode fieldName : configDocNode.get("fieldNameReplacement"))
					{
						downloadURL = downloadURL.replace(fieldName.get("destFieldName").asText(), reqNode.get(fieldName.get("sourceFieldName").asText()).asText());
					}
					
				}
		      
		      log.info("Formatted URL : "+downloadURL);
		      DownloaderAPI  downloaderAPI = null;
		      try
		      {
		    	  downloaderAPI = new DownloaderAPI();
		      }
		      catch(Exception e)
		      {
		    	  log.error("Unable to create DownloaderAPI instance ",e);
		      }
				String fileName = downloaderAPI.downloadFile(configDocNode.get("policyDocSaveLocation").asText(), downloadURL);
				
				if(!fileName.equals(null))
				{
					fileName = fileName.substring(fileName.lastIndexOf("/")+1,fileName.length());
					contentMgmtConfigNode = objectMapper.readTree((serverConfigService.getDocBYId("ContentManagementConfig-35-4").content().toString()));
					PolicyDocumentMetaData policyMetaData =  new PolicyDocumentMetaData();
					JsonObject metaData = policyMetaData.createMetaData(carrierPropRes, reqNode.get(ProposalConstants.PROPOSAL_ID).asText(), policyNo);
					 ECMSManagerAPI managerAPI = new ECMSManagerAPI();
					 documentId = managerAPI.uploadPolicyDocument(contentMgmtConfigNode, fileName,metaData);
					 log.info("Document ID : "+documentId);
				}else{
					log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCRES+"|ERROR|fileName is null|Document Download Url|"+downloadURL);
					throw new ExecutionTerminator();
				}
				
				if(!documentId.equals(null))
				{
					updatePolicyDocument(reqNode,documentId);
				}
					      
		    }
		    catch (Exception e)
		    {
		      log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYDOCRES+"|ERROR|CignaPolicyDocumentReqProcessor|",e);
				throw new ExecutionTerminator();
		    }
	  	}
	  
	  
	  public void updatePolicyDocument(JsonNode proposalSampleReqNode,String downloadURL)
		{
					try{
						// get user profile from database
						JsonNode userProfileDetailsNode = profileServices.getUserProfileByUkey(proposalSampleReqNode.get("uKey").asText());
						if(userProfileDetailsNode == null)
						{
							log.info("User Profile not found by view.... Retrying after 20 seconds");
							Thread.sleep(20000);
							userProfileDetailsNode = profileServices.getUserProfileByUkey(proposalSampleReqNode.get("uKey").asText());
						}
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
							log.info("CIGNA policy details updated status : "+status);	
							
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
