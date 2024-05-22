package com.idep.proposal.exception.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.proposal.util.ProposalConstants;

/**
 * 
 * @author 	yogesh.shisode
 * @version 1.0
 * @since 	23-MAR-2018
 *
 */
public class ProposalExceptionProcessor implements Processor {
	Logger log = Logger.getLogger(ProposalExceptionProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService service = null;
	JsonNode responseConfigNode;
	JsonNode errorNode;

	public void process(Exchange exchange) throws JsonProcessingException {
		try {
			
			String inputReq = exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq.toString());
			log.info("the input request in ProposalExceptionProcessor"+inputReqNode);
			
			
		
			if(this.service == null){
				this.service = CBInstanceProvider.getServerConfigInstance();
				this.responseConfigNode = this.objectMapper.readTree(this.service.getDocBYId(ProposalConstants.RESPONSE_CONFIG_DOC).content().toString());
			}
			

			ObjectNode objectNode = this.objectMapper.createObjectNode();
			if(inputReqNode.has("data"))
			{
				
				log.info("the data field to set:"+inputReqNode);
				objectNode.put(ProposalConstants.PROPOSAL_RES_CODE, inputReqNode.get("responseCode").asText());
				objectNode.put(ProposalConstants.PROPOSAL_RES_MSG,inputReqNode.get("message").asText() );
				objectNode.put(ProposalConstants.PROPOSAL_RES_DATA, inputReqNode.get("data").asText());
			exchange.getIn().setBody(objectNode);
			}
		}catch(Exception e){
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(ProposalConstants.PROPOSAL_RES_CODE, this.responseConfigNode.get(ProposalConstants.ERROR_CONFIG_CODE).intValue());
			objectNode.put(ProposalConstants.PROPOSAL_RES_MSG, this.responseConfigNode.get(ProposalConstants.ERROR_CONFIG_MSG).textValue());
			objectNode.put(ProposalConstants.PROPOSAL_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);
		}
	}
}	 