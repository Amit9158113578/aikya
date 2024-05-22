package com.idep.proposal.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author yogesh.shisode
 * @version 1.0
 * @since   23-MAR-2018
 * 
 */
public class MasterProposalReqProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(MasterProposalReqProcessor.class.getName());
	CBService service = null;
	JsonNode serviceConfigNode = null;

	@Override
	public void process(Exchange exchange){
		try{
			String proposalRequest = exchange.getIn().getBody(String.class);
			JsonNode proposalMasterReqNode =  this.objectMapper.readTree(proposalRequest);
			String deviceId = exchange.getIn().getHeader("deviceId").toString();
			 ((ObjectNode)proposalMasterReqNode).put("deviceId", deviceId);
			log.info("Life proposal updated UI request : " + proposalMasterReqNode);

			if(this.service == null){
				this.service = CBInstanceProvider.getServerConfigInstance(); 
				this.serviceConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ProposalConstants.LIFE_SERVICE_URL_CONFIG_DOC).content().toString());
			}

			log.info("serviceConfigNode : " + serviceConfigNode);
			int carrierId = proposalMasterReqNode.get("carrierId").intValue();
			int productId = proposalMasterReqNode.get("productId").intValue();
			String proposalSubURL = this.serviceConfigNode.get("Proposal-" + carrierId + "-" + productId).textValue();
			
			log.info("proposalSubURL : " + proposalSubURL);
			exchange.getIn().setHeader(ProposalConstants.PROPOSAL_SERVICE_HEADER, proposalSubURL);
			exchange.getIn().setBody(objectMapper.writeValueAsString(proposalMasterReqNode));
		}catch(Exception ex){
			log.info("Exception caught : " , ex);
		}
	}
}
