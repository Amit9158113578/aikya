package com.idep.proposal.data.commit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author shweta.joshi
 * Save Proposal Request in DB
 */
public class TravelProposalReqDBStore implements Processor{
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelProposalReqDBStore.class.getName());
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
	     
	        docObj.put(ProposalConstants.PROPOSAL_ID, docId);
			exchange.getIn().setBody(docObj);
			
	        try{
	        proposalDoc  =  transService.getDocBYId(docId);
	        }catch(Exception e){
	        	log.error("Failed to read travel proposal Document  :  "+docId,e);
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
	        		log.info(" Travel proposal Document  Crerated :  "+docId);
	        	}catch(Exception e){
	        		log.error("Failed to Create travel proposal Document  :  "+docId,e);
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
	        			String doc_status = transService.replaceDocument(docId, documentContent);
	 	       		this.log.info("Travel Proposal document updated : "+docId+" : "+doc_status);
	        	}catch(Exception e){
	        		log.error("Failed to Update travel proposal Document  :  "+docId,e);
	        	}
	        }
			
		}
		catch(Exception e)
		{
			log.error("Exception while creating or updating Travel proposal : ",e);
		}
		
	}

}

