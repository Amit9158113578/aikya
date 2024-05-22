package com.idep.policy.carrier.req.processor;

import java.io.FileOutputStream;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class PolicyDocumentResProcessor implements Processor
{
	
  ObjectMapper objectMapper = new ObjectMapper();
  Logger log = Logger.getLogger(PolicyDocumentResProcessor.class.getName());
  PolicyDocUploaderProcessor policyUpload = new PolicyDocUploaderProcessor();
  
  public void process(Exchange exchange) throws ExecutionTerminator {
	  
	    try
	    {
	    	
	      	
	      String policyDocResponse = exchange.getIn().getBody(String.class);
	      JsonNode policyDocResNode = this.objectMapper.readTree(policyDocResponse);
	      /**
	       * get base64 PDF stream data
	       */
	      String pdfString = policyDocResNode.get("pdfString").asText();
	      
	      /**
	       * read policy input request from property
	       */
	      JsonNode policyDocReqNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.TRAVPOLICYDOC_INPUT_REQ).toString());
	      
	      /**
	       * read carrier configuration from property
	       */
	      JsonNode policyConfigNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.TRAVPOLICYDOC_CONFIG).toString());

	      String filePath = policyConfigNode.get("policyPDFConfig").get("fileLocation").asText();
	      String fileName = policyDocReqNode.get("policyNo").asText().concat(".pdf");
	      filePath = filePath.concat(fileName);
	      
	     /**
	      * create file 
	      */
	      filePath = createPDFFile(pdfString,filePath);
	      
	      if(filePath.length()>0)
	      {
	      
		      /**
		       * set response (this will be used to update user profile with policy PDF details)
		       */
		      ObjectNode policyPDFFileNode = objectMapper.createObjectNode();
		      policyPDFFileNode.put("uKey", policyDocReqNode.get("uKey").asText());
		      policyPDFFileNode.put("pKey", policyDocReqNode.get("pKey").asText());
		      policyPDFFileNode.put("filePath", filePath);
		      policyPDFFileNode.put("contentRepoType", "FileSystem");
		      
		      
		      /**
		       * upload document to content repository (ALFRESCO)
		       */
		      try
		      {
		    	  JsonNode proposalDoc= objectMapper.readTree(exchange.getProperty(ProposalConstants.TRAVPOLICYDOC_INPUT_REQ).toString());
		    	  
		    	  
			      filePath = policyUpload.uploadPolicyDocument(fileName, "ContentManagementConfig-"+policyDocReqNode.get("carrierId").asText()+"-"+policyDocReqNode.get("businessLineId").asText(),proposalDoc);
			      if(filePath.length()>0)
			      {
			          policyPDFFileNode.put("filePath", filePath);
				      policyPDFFileNode.put("contentRepoType", "ContentRepository");
				      policyPDFFileNode.put("contentRepoName", "Alfresco");
			      }
			  }
		      catch(Exception e)
		      {
		    	 log.error("unable to extract Policy Document : ",e);
		    	 throw new ExecutionTerminator();
		      }
	
		      exchange.getIn().setBody(policyPDFFileNode);
	      }
	      else
	      {
	    	  /**
	    	   * throw custom exception to terminate process if unable to create policy pdf file
	    	   */
	    	  throw new ExecutionTerminator();
	      }
	      
	      
	    }
	    catch(Exception e)
	    {
	    	
			throw new ExecutionTerminator();
	    }
  }
  
  public String createPDFFile(String base64Stream,String filePath)
  {
	  try
	  {
		  FileOutputStream fos = new FileOutputStream(filePath);
		  fos.write(Base64.decodeBase64(base64Stream));
	      fos.close();
	      log.info("Policy PDF create successfully : "+filePath);
	      return filePath;
	  }
	  catch(Exception e)
	  {
		  log.error("unable to create policy pdf file at "+filePath+" : ",e);
		  return "";
	  }
      
	  
  }

}
