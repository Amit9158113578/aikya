package com.idep.proposal.data.commit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author Prathamesh.Waghmare
 * @version 1.0
 * @since   6-APR-2018
 * @Description: CarrierProposal Response Save inside Couch base Document
 */

public class CarrierProposalResponseDBStore implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	@Override
	public void process(Exchange exchange) throws Exception {
		String carrierProposalData = exchange.getIn().getBody(String.class);
		JsonObject proposalResponse = JsonObject.fromJson(carrierProposalData);
		log.info("carrier Proposal response -------"+proposalResponse);

		String proposalId = exchange.getProperty(ProposalConstants.PROPOSAL_ID).toString();
		String carrierProposalResponseDocId = ProposalConstants.CARRIER_PROPOSAL_RESPONSE+proposalId;

		JsonNode proposalResponseNode = objectMapper.readTree(carrierProposalData);

		long carrierId=(long)exchange.getProperty(ProposalConstants.CARRIER_ID);
		long productId=(long) exchange.getProperty(ProposalConstants.PRODUCT_ID);

		ObjectNode responseNode = objectMapper.createObjectNode();

		responseNode.put(ProposalConstants.PROPOSAL_ID,proposalId);
		responseNode.put(ProposalConstants.CARRIER_ID,carrierId );
		responseNode.put(ProposalConstants.PRODUCT_ID, productId);
		responseNode.put(ProposalConstants.DOCUMENT_TYPE,ProposalConstants.CARRIER_PROP_RES);
		responseNode.put("response", proposalResponseNode);

		JsonObject carrierResponseObject=JsonObject.fromJson(responseNode.toString());

		log.info("carrierResponseObject---------"+carrierResponseObject);

		try{
			Date currentDate = new Date();
			proposalResponse.put("proposalCreatedDate", dateFormat.format(currentDate));
			transService.createDocument(carrierProposalResponseDocId , carrierResponseObject);
		}catch(Exception e){
			log.error("Failed to Create life CarrierProposalRequest Document  :  "+carrierProposalResponseDocId,e);
			throw new ExecutionTerminator();
		}
	}
}
