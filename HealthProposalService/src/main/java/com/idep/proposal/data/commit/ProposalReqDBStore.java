package com.idep.proposal.data.commit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author sandeep.jadhav
 * Save Proposal Request in DB
 */
public class ProposalReqDBStore implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	@Override
	public void process(Exchange exchange) throws Exception {

		try
		{
			
			String proposalRequest = exchange.getIn().getBody(String.class);
			JsonObject docObj = JsonObject.fromJson(proposalRequest);
			JsonDocument proposalDoc=null;
	        String docId=docObj.getString("proposalId");
	        
	        /*
	        ObjectNode proposalInfo = objectMapper.createObjectNode();
			proposalInfo.put(ProposalConstants.PROPOSAL_ID, docId);*/
	        
	        docObj.put(ProposalConstants.PROPOSAL_ID, docId);
	        docObj.put(ProposalConstants.ENCRYPT_PROPOSAL_ID, exchange.getProperty(ProposalConstants.ENCRYPT_PROPOSAL_ID));
			exchange.getIn().setBody(docObj);
			try{
	        proposalDoc  =  transService.getDocBYId(docId);
	        
	        }catch(Exception e){
	        	log.error("Failed to read health proposal Document  :  "+docId,e);
	        }
	        if(proposalDoc == null)
	        {
	        	/**
	        	 * create proposal document
	        	 */
	        	try{
	        		
	        		Date currentDate = new Date();
	        		docObj.put("proposalCreatedDate", dateFormat.format(currentDate));
	        		transService.createDocument(docId, docObj);
	        		log.info(" Health proposal Document  Crerated :  "+docId);
	        	}catch(Exception e){
	        		log.error("Failed to Create health proposal Document  :  "+docId,e);
	        	}
	        }
	        else
	        {
	        	try{
	        		/****
	        		 * Adding current date and time
	        		 * */
	        		Date currentDate = new Date();
	        		docObj.put("updatedDate", dateFormat.format(currentDate));
	        		/**
		        	 * update proposal document if already exist
		        	 */
	        		JsonObject documentContent = proposalDoc.content().put(docObj.getString(ProposalConstants.DOCUMENT_TYPE), docObj);
	 	       	/**
	 	       	 * update proposal status
	 	       	 */
	        		if(docObj.containsKey("proposalStatus"))
	        		{
	        			documentContent.put("proposalStatus", docObj.getString("proposalStatus"));
	        		}
	        		 
	        		 String  doc_status= transService.replaceDocument(docId, documentContent);
        			
	 	       		this.log.debug("Health Proposal document updated : "+docId+" : "+doc_status);
	        	}catch(Exception e){
	        		log.error("Failed to Update health proposal Document  :  "+docId,e);
	        	}
	        }
			
		}
		catch(Exception e)
		{
			log.error("Exception while creating or updating Health proposal : ",e);
		}
		
	}

}
