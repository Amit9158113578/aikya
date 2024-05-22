package com.idep.policy.purchase.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.policy.purchase.util.PurchaseStmtConstants;

public class PolicyPurchaseResProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PolicyPurchaseReqProcessor.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
			String purchaseStmt = exchange.getIn().getBody(String.class);
		  	JsonNode purchaseStmtNode =  objectMapper.readTree(purchaseStmt);
			
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(PurchaseStmtConstants.PROPOSAL_RES_CODE, DocumentDataConfig.getConfigDocList().get(PurchaseStmtConstants.RESPONSE_MSG).get(PurchaseStmtConstants.SUCC_CONFIG_CODE).asInt());
			objectNode.put(PurchaseStmtConstants.PROPOSAL_RES_MSG, DocumentDataConfig.getConfigDocList().get(PurchaseStmtConstants.RESPONSE_MSG).get(PurchaseStmtConstants.SUCC_CONFIG_MSG).asText());
			objectNode.put(PurchaseStmtConstants.PROPOSAL_RES_DATA, purchaseStmtNode);
		    exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
			
		}
		catch(Exception e)
		{
			log.error("Exception at PolicyPurchaseResProcessor : ",e);
		}
		
		
	}

}
