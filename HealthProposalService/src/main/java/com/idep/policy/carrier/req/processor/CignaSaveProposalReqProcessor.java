package com.idep.policy.carrier.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.policy.payment.res.processor.PaymentResponseValidator;
import com.idep.proposal.util.ProposalConstants;

public class CignaSaveProposalReqProcessor implements Processor{

	Logger log =  Logger.getLogger(CignaSaveProposalReqProcessor.class);
	ObjectMapper objectMapper = new ObjectMapper();
	CBService service =  CBInstanceProvider.getPolicyTransInstance();
	
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		
		try{
			
			JsonNode requestNode = objectMapper.readTree(exchange.getProperty(ProposalConstants.HLTHPOLICY_INPUT_REQ).toString());
			
			/**
			 * read proposalId document from database to get carrierQuoteId value
			 */
			String proposalId = requestNode.get("proposalId").asText();
			JsonDocument proposalDocument = service.getDocBYId(proposalId);
			JsonNode proposalDocumentNode = objectMapper.readTree(proposalDocument.content().toString());
			String carrierQuoteId = proposalDocumentNode.get("proposalRequest").get("coverageDetails").get("carrierQuoteId").asText();
			//String carrierQuoteId = proposalDocument.content().get("carrierQuoteId").toString();
			
			/**
			 * Read carrierQuoteId document to get ValidateRequest from database
			 */
			JsonDocument carrierQuoteIdDoc=null;
			try{
				carrierQuoteIdDoc = service.getDocBYId(carrierQuoteId);
			}catch(Exception e){
				log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYCONF_REQ+"|ERROR|Document not found|"+carrierQuoteIdDoc,e);
				throw new ExecutionTerminator();
			}
			String quoteIdDoc = carrierQuoteIdDoc.content().toString();
			JsonNode validateReqNode = objectMapper.readTree(quoteIdDoc);
			/**
			 * Remove  "documentType": "CignaValidateRequest" field from validateReqNode
			 */
			((ObjectNode)validateReqNode).remove("documentType");
			
			String inwardDOList  = exchange.getIn().getBody(String.class);
			JsonNode inwardNode = objectMapper.readTree(inwardDOList);
			/**
			 * Attach inwardDOList with validateReqNode
			 */
			
			((ObjectNode)validateReqNode.get("listofPolicyTO")).putAll((ObjectNode)inwardNode);
			
			exchange.getIn().setBody(validateReqNode.toString());
					
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.POLICYREQ+"|ERROR|CignaSaveProposalReqProcessor|",e);
			throw new ExecutionTerminator();
		}
	}
}

