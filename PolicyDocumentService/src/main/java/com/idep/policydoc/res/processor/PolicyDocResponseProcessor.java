package com.idep.policydoc.res.processor;

import java.io.FileOutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policydoc.util.PolicyDocViewConstants;
import com.idep.services.impl.ECMSManagerAPI;
import com.idep.user.profile.impl.UserProfileServices;

/**
 * 
 * @author sandeep.jadhav
 *
 */
public class PolicyDocResponseProcessor implements Processor {

	Logger log = Logger.getLogger(PolicyDocResponseProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	CBService servercongi =  CBInstanceProvider.getServerConfigInstance();
	ECMSManagerAPI managerAPI = new ECMSManagerAPI();
	UserProfileServices profileServices = new UserProfileServices();
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try 
		{
		 
		 /**
		  *  decode base64 string and create pdf
		  */
		 log.info("process initiated to update policy document path");	
		 String pdfFileLoc =  exchange.getProperty("pdfFileLocation").toString();
		 pdfFileLoc = pdfFileLoc+"signed"+".pdf";
		 FileOutputStream fos = new FileOutputStream(pdfFileLoc);
		         
		 String resJSON = exchange.getIn().getBody(String.class);
		 JsonNode policyResNode  = objectMapper.readTree(resJSON);
		 
		 // to create PDF file append this to base64 string
		 String pdfString = "data:application/pdf;base64,";
		 String bas64String  = policyResNode.get("data").get("signedPDF").textValue();
		 String kotakPDF = bas64String.replaceAll(pdfString, "");
         fos.write(Base64.decodeBase64(kotakPDF));
         
         fos.close();
         
         /**
	       * upload document to content repository (ALFRESCO)
	       */
        JsonNode policyDocReqNode = objectMapper.readTree(exchange.getProperty(PolicyDocViewConstants.CARRIER_PROPOSAL_PRO).toString());
        
        ObjectNode policyPDFFileNode = objectMapper.createObjectNode();
        
        JsonObject metaDataObj =JsonObject.create();
        
        metaDataObj.put("policyNumber", policyDocReqNode.get("carPolicyResponse").get("policyNo").asText());
       
        metaDataObj.put("customerId", policyDocReqNode.get("proposalId").asText());
      
        metaDataObj.put("emailId", policyDocReqNode.get("emailId").asText());
        
        metaDataObj.put("mobileNumber", policyDocReqNode.get("mobile").asText());
        
        metaDataObj.put("policyBond","CarPolicyBond");
        String policyNo = policyDocReqNode.get("carPolicyResponse").get("policyNo").asText();
        policyNo=policyNo.replaceAll("/", "");
        String fileName=policyNo+"signed"+".pdf";
        
	      try
	      {
	    	  
	    	 String documentId= "ContentManagementConfig-"+policyDocReqNode.get(PolicyDocViewConstants.CARRIER_ID)+"-"+policyDocReqNode.get("businessLineId");
	    	
	    	 JsonNode contentMagConfig = objectMapper.readTree(servercongi.getDocBYId(documentId).content().toString());
	    	 Thread.sleep(10000); // wait until file gets written to filesystem
	    	 String filePath = managerAPI.uploadPolicyDocument(contentMagConfig, fileName, metaDataObj);
		      
		     if(filePath.length()>0)
		      {
		          policyPDFFileNode.put("filePath", filePath);
			      policyPDFFileNode.put("contentRepoType", "ContentRepository");
			      policyPDFFileNode.put("contentRepoName", "Alfresco");
		      }
		      else
		      {
		    	  policyPDFFileNode.put("filePath", pdfFileLoc);
			      policyPDFFileNode.put("contentRepoType", "FileSystem");
		      }
		  }
	      catch(Exception e)
	      {
	    	  log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+"POLICYDOCRESPRO|ERROR|"+"Exception while uploading policy document at Alfresco:",e);
	    	  policyPDFFileNode.put("filePath", pdfFileLoc);
		      policyPDFFileNode.put("contentRepoType", "FileSystem");
	      }
	      
         
         /**
          * update policy document details in user profile
          */
         JsonNode userProfileNode = exchange.getProperty("userProfileData", JsonNode.class);
         JsonNode userPolicyKeyNode = exchange.getProperty("userPolicyKeys", JsonNode.class);
 	     String policyKey = userPolicyKeyNode.get("pKey").asText();
 	     String mobile = userProfileNode.get("mobile").asText();
        
 	     /**
 	      * update policy details
 	      */
 	     boolean status = profileServices.updatePolicyRecordByPkey(mobile, policyKey, policyPDFFileNode);
         log.info("policy details updated status : "+status);
         
          log.info(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICY_SIGN_RESPONSE+"|SUCCESS|"+"Policy Sign successfully :"+policyDocReqNode.findValue("policyNo").asText());
		  ObjectNode obj = objectMapper.createObjectNode();
	      obj.put("responseCode",1000);
	      obj.put("message","success");
	      exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
         
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(PolicyDocViewConstants.LOG_REQ).toString()+PolicyDocViewConstants.POLICYDOCRESPRO+"|ERROR|"+"Exception at PolicyDocResponseProcessor",e);
			ObjectNode obj = objectMapper.createObjectNode();
			obj.put("responseCode",1002);
	        obj.put("message","error");
	        exchange.getIn().setBody(objectMapper.writeValueAsString(obj));
	         
		}
		
	}

}
