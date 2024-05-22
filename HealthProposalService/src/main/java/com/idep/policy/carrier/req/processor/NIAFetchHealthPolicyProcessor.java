/**
 * 
 */
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

/**
 * @author vipin.patil
 *
 * May 23, 2017
 */
public class NIAFetchHealthPolicyProcessor implements Processor{

	/* (non-Javadoc)
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	  ObjectMapper objectMapper = new ObjectMapper();
	  Logger log = Logger.getLogger(NIAFetchHealthPolicyProcessor.class.getName());
	  CBService service =  CBInstanceProvider.getPolicyTransInstance();
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		try
		{
		String reqBody = exchange.getIn().getBody(String.class);
		JsonNode reqNode = objectMapper.readTree(reqBody);
		log.info("Request Node in NIAFetchHealthPolicyProcessor : "+reqNode);
		JsonObject carrierPropRes = service.getDocBYId(reqNode.get("proposalId").asText()).content();
		JsonNode fetchReqNode = objectMapper.readTree(carrierPropRes.get("healthProposalResponse").toString());
		((ObjectNode)fetchReqNode).put("requestType", "HealthPolicyDocRequest");
		((ObjectNode)fetchReqNode).put("carrierId", carrierPropRes.getInt("carrierId"));
		((ObjectNode)fetchReqNode).put("planId", carrierPropRes.getInt("productId"));
		
		JsonDocument document = serverConfig.getDocBYId(ProposalConstants.HEALTH_POLICY_DOC_REQUEST+fetchReqNode.get("carrierId")+ 
	    		  "-" + fetchReqNode.get("planId"));
	      log.info("NIAFetchHealthPolicyProcessor document: "+document);

	      if(document!=null)
	      {
	    	  	JsonNode proposalConfigNode = objectMapper.readTree(document.content().toString());
	    	  	log.debug("healthPolicyDocConfigNode: "+proposalConfigNode);
	  			exchange.setProperty(ProposalConstants.PROPOSALREQ_CONFIG,proposalConfigNode);
	      } 
	     
		exchange.setProperty(ProposalConstants.POLICY_FETCHDOC_INPUTREQ, this.objectMapper.writeValueAsString(fetchReqNode));
		exchange.setProperty(ProposalConstants.POLICY_UKEYPKEY, this.objectMapper.writeValueAsString(reqNode));
	
		exchange.getIn().setBody(fetchReqNode);
		
		}
		catch(Exception e)
		{
			log.error("Exception at NewIndiaFetchHealthPolicyProcessor : ",e);
		}
	}

}
