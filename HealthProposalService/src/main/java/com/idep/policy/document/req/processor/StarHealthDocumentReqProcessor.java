package com.idep.policy.document.req.processor;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
import com.idep.services.impl.ECMSManagerAPI;
import com.idep.user.profile.impl.UserProfileServices;

public class StarHealthDocumentReqProcessor implements Processor {
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(StarHealthDocumentReqProcessor.class.getName());
	  CBService policyTransService =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfigService =  CBInstanceProvider.getServerConfigInstance();
	  JsonNode configDocNode=null;
	  String downloadURL=null;
	  String documentId = null;
	  JsonNode contentMgmtConfigNode=null;
	  UserProfileServices profileServices = new UserProfileServices();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub

		try{
		 String policyDocRequest = exchange.getIn().getBody(String.class);
		 JsonNode reqNode = this.objectMapper.readTree(policyDocRequest);
	      log.info("Star Policy Doc request : "+reqNode);
	      String policyNo = reqNode.get("policyNo").asText();
	      
	      JsonNode proposalDoc = objectMapper.readTree(policyTransService.getDocBYId(reqNode.get("proposalId").asText()).content().toString());
	      JsonNode healthProposalRes =objectMapper.readTree(proposalDoc.get("healthProposalResponse").toString()); 
	      configDocNode = objectMapper.readTree((serverConfigService.getDocBYId("HealthPolicyDocDownloadConfig-34-4").content().toString()));
	      downloadURL = configDocNode.get("downLoadURL").asText();
	      log.info("healthProposalRes : "+healthProposalRes);
	      if(configDocNode.has("fieldNameReplacement"))
			{
				for(JsonNode fieldName : configDocNode.get("fieldNameReplacement"))
				{
					log.info("FieldName : "+fieldName);
					downloadURL = downloadURL.replace(fieldName.get("destFieldName").asText(), healthProposalRes.get(fieldName.get("sourceFieldName").asText()).asText());
				}
				
			}
	      URL url = new URL(downloadURL);
	      ObjectNode bodyNode = objectMapper.createObjectNode();
	      log.info("STAR Health Policy Document Download URL Genrated : "+downloadURL);

	      if(configDocNode.has("fieldAddInBody"))
			{
				for(JsonNode fieldName : configDocNode.get("fieldAddInBody"))
				{
					log.info("FieldName : "+fieldName);
					
					if(fieldName.has("deafultValue") && fieldName.get("deafultValue").asText().equalsIgnoreCase("Y") ){
					bodyNode.put(fieldName.get("key").asText(),fieldName.get("value"));
					}else{
						bodyNode.put(fieldName.get("key").asText(),healthProposalRes.get(fieldName.get("key").asText()));
					}
					
				}
				
			}
	      log.info("PolicyDocument bodyNode : "+bodyNode);
	      
	      
	      
	      byte[] postData       = objectMapper.writeValueAsString(bodyNode).getBytes( StandardCharsets.UTF_8 );
	      HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
	      log.info("Connection Opned Success");
	      httpConn.setDoOutput( true );
	      httpConn.setRequestMethod("POST");
	      httpConn.setRequestProperty( "Content-Type", "application/json");
	      httpConn.setRequestProperty( "charset", "UTF-8");
	      httpConn.setRequestProperty( "Content-Length", Integer.toString( bodyNode.asText().length() ));
	      httpConn.setUseCaches( false );
	      try( DataOutputStream wr = new DataOutputStream( httpConn.getOutputStream())) {
	    	  log.info("Body Sending Started............");
	    	  wr.write( postData );
	      }catch(Exception e){
	    	  log.error("Error At Star Health Policy Download : "+e);
	      }
	    
	      log.info("DOnncetion Opned......");
	      int responseCode = httpConn.getResponseCode();
	      log.info("httpConn Response msg : "+httpConn.getResponseMessage());
	      String fileName = "";
	      log.info("Response Code : "+responseCode);
	      if (responseCode == 200)
	      {
		        String contentType = httpConn.getContentType();
		        int contentLength = httpConn.getContentLength();
		        log.info("contentType : "+contentType);
	    	  String result = "";
	    	    String line;
	    	    int bytesRead = -1;
		        byte[] buffer = new byte['?'];
		        InputStream inputStream = httpConn.getInputStream();
		        FileOutputStream outputStream = new FileOutputStream(configDocNode.get("policyDocSaveLocation").asText()+healthProposalRes.get("referenceId").asText()+".pdf");
		        while ((bytesRead = inputStream.read(buffer)) != -1) {
		          outputStream.write(buffer, 0, bytesRead);
		        }
		        log.info("outputStream : "+outputStream);
		        
		        if(outputStream!=null)
				{
					contentMgmtConfigNode = objectMapper.readTree((serverConfigService.getDocBYId("ContentManagementConfig-34-4").content().toString()));
					PolicyDocumentMetaData policyMetaData =  new PolicyDocumentMetaData();
					JsonObject metaData = policyMetaData.createMetaData(proposalDoc, reqNode.get(ProposalConstants.PROPOSAL_ID).asText(), policyNo);
					 ECMSManagerAPI managerAPI = new ECMSManagerAPI();
					 documentId = managerAPI.uploadPolicyDocument(contentMgmtConfigNode, healthProposalRes.get("referenceId").asText()+".pdf",metaData);
					 log.info("Document ID : "+documentId);
				}
	    	    log.info("STAR Health Policy Document updated in Alfresco");
	      }
	      if(!documentId.equals(null))
			{
	    	  updatePolicyDocument(reqNode,documentId);
			}
	           
	    }
	    catch (Exception e)
	    {
	      this.log.error("Exception at Health StarPolicyDocumentReqProcessor : ", e);
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
						log.info("Star Health policy details updated status : "+status);	
						
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
