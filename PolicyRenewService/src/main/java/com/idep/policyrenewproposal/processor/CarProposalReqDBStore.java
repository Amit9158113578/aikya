package com.idep.policyrenewproposal.processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.policyrenew.exception.processor.ExecutionTerminator;

/**
 * 
 * Save Proposal Request in DB
 */
public class CarProposalReqDBStore implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarProposalReqDBStore.class.getName());
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	@Override
	public void process(Exchange exchange) throws Exception {

		String doc_status = null;

		try
		{

			String proposalRequest = exchange.getIn().getBody(String.class);
			JsonObject docObj = JsonObject.fromJson(proposalRequest);
			String docId=docObj.getString("proposalId");
			exchange.getIn().setBody(docObj);

			JsonDocument proposalDoc =  null;

			try
			{
				proposalDoc = transService.getDocBYId(docId);
			}
			catch(Exception e)
			{
				log.error("failed to read proposal document from DB :"+docId);
				throw new ExecutionTerminator();
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
				}
				catch(Exception e)
				{
					log.error("Failed to Create car proposal Document  :  "+docId,e);
					throw new ExecutionTerminator();
				}
			}
			else
			{
				/**
				 * Adding current date and time
				 */
				Date currentDate = new Date();
				docObj.put("updatedDate", dateFormat.format(currentDate));
				/**
				 * update proposal document if already exist
				 */
				String documentType = docObj.getString("documentType");
				JsonObject documentContent = proposalDoc.content().put(documentType, "documentType");

				/**
				 * update proposal status
				 */
				if(docObj.containsKey("proposalStatus"))
				{
					documentContent.put("proposalStatus", docObj.getString("proposalStatus"));
				}


				try
				{
					transService.replaceDocument(docId, documentContent);

				}catch(Exception e)
				{
					log.error("Failed to Update car proposal Document  :  "+docId,e);
					throw new ExecutionTerminator();
				}
			}
		}
		catch(Exception e)
		{
			log.error("|ERROR|"+"car proposal db stored failed :"+doc_status,e);
			throw new ExecutionTerminator();
		}

	}

}
