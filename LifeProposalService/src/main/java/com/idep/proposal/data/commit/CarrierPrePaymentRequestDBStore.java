package com.idep.proposal.data.commit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class CarrierPrePaymentRequestDBStore implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CarrierPrePaymentRequestDBStore.class.getName());
	CBService transService = CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	public void process(Exchange exchange)
			throws Exception
	{
		String carrierProposalData = exchange.getIn().getBody(String.class);
		String proposalId = exchange.getProperty(ProposalConstants.PROPOSAL_ID).toString();
		String documentId = ProposalConstants.CARRIERPREPROPREQUEST+"-"+proposalId;
		this.log.info("pre payment id generated carrierPrePaymentRequest...........:" + documentId);

		String carrierProposalDataFormatted = "\"" + carrierProposalData + "\"";
		ObjectNode requestNode = this.objectMapper.createObjectNode();
		
		long carrierId=(long)exchange.getProperty(ProposalConstants.CARRIER_ID);
		long productId=(long) exchange.getProperty(ProposalConstants.PRODUCT_ID);
		
		requestNode.put(ProposalConstants.REQUEST, carrierProposalDataFormatted);
		requestNode.put(ProposalConstants.PROPOSAL_ID,proposalId);
		requestNode.put(ProposalConstants.CARRIER_ID,carrierId );
		requestNode.put(ProposalConstants.PRODUCT_ID, productId);
		requestNode.put(ProposalConstants.DOCUMENT_TYPE,ProposalConstants.CARRIERPREPROPREQUEST);

		String carrierRequesttNode = requestNode.toString();
		JsonObject carrierRequestObject = JsonObject.fromJson(carrierRequesttNode);
		try
		{
			Date currentDate = new Date();
			carrierRequestObject.put(ProposalConstants.PROPOSALCREATEDATE, this.dateFormat.format(currentDate));
			this.transService.createDocument(documentId, carrierRequestObject);
		}
		catch (Exception e)
		{
			this.log.error("Failed to Create Life CarrierPrePaymentRequestDBStore Document  :  " + documentId, e);
			throw new ExecutionTerminator();
		}
	}
}
