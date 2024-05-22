package com.idep.policy.carrier.req.processor;

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
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.proposal.util.ProposalConstants;
/**
 * @author pravin.jakhi
 *  class written for store Aditya Birla Proposal Request in CB.
 */
public class ABHIProposalReqStoreProcessor implements Processor {

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ABHIProposalReqStoreProcessor.class.getName());
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String docId=null;
			String proposalRequest = exchange.getIn().getBody(String.class);
			
			String proposalNum = exchange.getProperty(ProposalConstants.PROPOSAL_ID).toString();
			
			JsonObject proposalIdDoc = transService.getDocBYId(proposalNum).content();
			
			JsonNode docObj = objectMapper.readTree(proposalRequest);//PolicyCreationRequest"  JsonObject.fromJson(proposalRequest);
			
			ObjectNode propoosalReq = objectMapper.createObjectNode();
			((ObjectNode)propoosalReq).put("carrierProposalRequest", docObj);
			
			JsonObject proposalDoc=JsonObject.fromJson(propoosalReq.toString());
			JsonNode policyCreationRequest =docObj.get("PolicyCreationRequest");
			docId=policyCreationRequest.get("Quotation_Number").asText();
			
			if(docId == "" || docId == null)
			{
				/*
				 * get the latest sequence number for proposal id from couchbase.
				 * 
				 */
				
				String ABHIQUOTEID = DocumentDataConfig.getConfigDocList().get(ProposalConstants.DOC_ID_CONFIG).get("adityaBirlaHealthQuoteId").asText()+this.serverConfig.updateDBSequence("SEQHEALTHQUOTE");;
				log.info("ABHIQUOTEID: "+ABHIQUOTEID);
				((ObjectNode)propoosalReq).put(ProposalConstants.ABHI_QUOTE_ID, ABHIQUOTEID);
				String ABHI_doc_status = transService.createDocument(ABHIQUOTEID, proposalDoc);
				log.info("ABHI_doc_status: "+ABHI_doc_status);
				proposalIdDoc.put(ProposalConstants.ABHI_QUOTE_ID, ABHIQUOTEID);
				String docUpdation_Status = transService.replaceDocument(proposalNum, proposalIdDoc);
			}
			else
			{
				log.debug("ABHI CarrierQuoteId -- : "+docId +"\n"+docObj);
		        JsonDocument carrierQuoteDoc =  transService.getDocBYId(docId);
		        
		        if(carrierQuoteDoc == null)
		        {
		        	String doc_status = transService.createDocument(docId, proposalDoc);
		        	 	this.log.debug("ABHI Health Proposal Request document created : "+docId+" : "+doc_status);
		        }else{
		        	
		        	/**
		        	 * update ABHI proposal request document if already exist
		        	 */
		        	JsonObject documentContent = transService.getDocBYId(docId).content();
		 	       	String doc_status = transService.replaceDocument(docId, documentContent);
		 	       	log.debug("ABHI Health Proposal Request document updated : "+docId+" : "+doc_status);
		        }
			}
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ABHIProposalReqStoreProcessor|",e);
		}
	}
}
