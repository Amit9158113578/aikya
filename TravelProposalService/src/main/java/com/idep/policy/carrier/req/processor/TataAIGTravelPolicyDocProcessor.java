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
import com.idep.proposal.util.ProposalConstants;

public class TataAIGTravelPolicyDocProcessor implements Processor {

	/* (non-Javadoc)
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(TataAIGTravelPolicyDocProcessor.class.getName());
	  CBService service =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfig =  CBInstanceProvider.getServerConfigInstance();
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try
		{
		String reqBody = exchange.getIn().getBody(String.class);
		JsonNode reqNode = this.objectMapper.readTree(reqBody);
		log.info("Request Node for Fetch Doc : "+reqNode);
		JsonObject carrierPropRes = service.getDocBYId(reqNode.get("proposalId").asText()).content();
		log.info("ProposalId Response : "+carrierPropRes);
		
		
		
		JsonNode fetchReqNode = this.objectMapper.readTree(carrierPropRes.get("travelPolicyResponse").toString());
		((ObjectNode)fetchReqNode).put("requestType", "TravelPolicyDocumentRequest");
		((ObjectNode)fetchReqNode).put("carrierId", carrierPropRes.getInt("carrierId"));
		((ObjectNode)fetchReqNode).put("productId", carrierPropRes.getInt("planId"));
		((ObjectNode)fetchReqNode).put("businessLineId", carrierPropRes.getInt("businessLineId"));
		log.info("Fetch Req Node : "+fetchReqNode);
		
		JsonDocument document = serverConfig.getDocBYId("TravelPolicyDocumentRequest-"+fetchReqNode.get(ProposalConstants.CARRIER_ID).intValue() );
		JsonNode travelPolicyDocConfigNode = this.objectMapper.readTree(document.content().toString()); 
		log.info("travelPolicyDocConfigNode" + travelPolicyDocConfigNode);
		
		 exchange.setProperty(ProposalConstants.TRAVPOLICYDOC_CONFIG,travelPolicyDocConfigNode); 
		exchange.setProperty(ProposalConstants.CARRIER_FETCH_DOC_INPUT_REQ, this.objectMapper.writeValueAsString(fetchReqNode));
		exchange.setProperty(ProposalConstants.CARRIER_UKEY_PKEY_INPUT_REQ, this.objectMapper.writeValueAsString(reqNode));
		 ((ObjectNode)reqNode).putAll((ObjectNode)fetchReqNode);
		 ((ObjectNode)reqNode).put("businessLineId", carrierPropRes.getInt("businessLineId"));
		 exchange.setProperty(ProposalConstants.TRAVPOLICYDOC_INPUT_REQ, reqNode);
		exchange.getIn().setBody(reqNode);
		}
		catch(Exception e)
		{
			log.error("Exception at TataAIGTravelPolicyDocProcessor : ",e);
		}
	}



}
