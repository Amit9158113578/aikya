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
 * @description: Carrier Proposal Request Save inside Couch base Document
 */

public class CarrierProposalRequestDBStore implements Processor{
	CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(ProposalReqDBStore.class.getName());
	CBService transService =  CBInstanceProvider.getPolicyTransInstance();
	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

	@Override
	public void process(Exchange exchange) throws Exception {

		String carrierProposalData = exchange.getIn().getBody(String.class);
		JsonObject proposalRequest = JsonObject.fromJson(carrierProposalData);

		String proposalId = exchange.getProperty(ProposalConstants.PROPOSAL_ID).toString();
		String carrierProposalRequestDocId = ProposalConstants.CARRIER_PROPOSAL_REQUEST+proposalId;

		JsonNode proposalRequestNode = objectMapper.readTree(carrierProposalData);

		long carrierId=(long)exchange.getProperty(ProposalConstants.CARRIER_ID);
		long productId=(long) exchange.getProperty(ProposalConstants.PRODUCT_ID);

		ObjectNode requestNode = objectMapper.createObjectNode();

		requestNode.put("request", proposalRequestNode);
		requestNode.put(ProposalConstants.PROPOSAL_ID,proposalId);
		requestNode.put(ProposalConstants.CARRIER_ID,carrierId );
		requestNode.put(ProposalConstants.PRODUCT_ID, productId);
		requestNode.put(ProposalConstants.DOCUMENT_TYPE,ProposalConstants.CARRIER_PROP_REQ);

		JsonObject carrierRequestObject=JsonObject.fromJson(requestNode.toString());

		log.info("carrierRequestObject---------"+carrierRequestObject);

		try{
			Date currentDate = new Date();
			proposalRequest.put("proposalCreatedDate", dateFormat.format(currentDate));
			transService.createDocument(carrierProposalRequestDocId, carrierRequestObject);
		}catch(Exception e){
			log.error("Failed to Create life CarrierProposalRequest Document  :  " + carrierProposalRequestDocId, e);
			throw new ExecutionTerminator();
		}
	}
}
