package com.idep.proposal.dbstore.processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * 
 * @author shweta.joshi
 * Save Proposal Request in DB
 */
public class ProposalReqDBStore implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	static String ExceptionHandlerQ = "ExceptionHandlerQ";
	@Override
	public void process(Exchange exchange) throws Exception 
	{CamelContext camelContext = exchange.getContext();
	ProducerTemplate template = camelContext.createProducerTemplate();
	JsonObject docObj = null;
	try
	{
		String proposalRequest = exchange.getIn().getBody(String.class);
		log.debug("proposalRequest in ProposalReqDBStore: "+proposalRequest);
		docObj = JsonObject.fromJson(proposalRequest);
		JsonDocument proposalDoc=null;
		String docId=docObj.getString("proposalId");
		docObj.put(ProposalConstants.PROPOSAL_ID, docId);
		exchange.getIn().setBody(docObj);

		try
		{
			proposalDoc  =  transService.getDocBYId(docId);
		}catch(Exception e)
		{
			log.error("Failed to read health proposal Document  :  "+docId,e);
		}
		if(proposalDoc == null)
		{
			/**
			 * create proposal document
			 */
			try
			{
				Date currentDate = new Date();
				docObj.put("proposalCreatedDate", dateFormat.format(currentDate));
				transService.createDocument(docId, docObj);
				log.debug(" Travel proposal Document  Created :  "+docId);
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
				JsonObject documentContent = null;
				String documentType = null;
				documentType = docObj.getString("documentType");
				documentContent = JsonObject.fromJson((proposalDoc.content()).put(docObj.getString("documentType"), docObj).toString());

				/**
				 * update proposal status
				 */
				if(docObj.containsKey("proposalStatus"))
				{
					documentContent.put("proposalStatus", docObj.getString("proposalStatus").toString());
				}

				String doc_status = transService.replaceDocument(docId, documentContent);
			}
			catch(Exception e)
			{
				log.error("Failed to Update travel proposal Document  :  "+docId,e);
			}
		}	
	}
	catch(Exception e)
	{
		log.error("Exception while creating or updating Travel proposal : ",e);

		String trace = "Error in Class :"+ProposalReqDBStore.class+"   Line Number :"+Thread.currentThread().getStackTrace()[0].getLineNumber();
		log.info("Erroror messgaes ProposalReqDBStore"+ProposalReqDBStore.class+"    "+Thread.currentThread().getStackTrace()[0].getLineNumber());
		String uri = "activemq:queue:" + ExceptionHandlerQ;
		docObj.put("transactionName","ProposalReqDBStore");
		docObj.put("Exception",e.toString());
		docObj.put("ExceptionMessage",trace);
		exchange.getIn().setBody(docObj.toString());
		log.info("sending to exception handler queue"+docObj);
		exchange.setPattern(ExchangePattern.InOnly);
		template.send(uri, exchange);
	}

	}

}

