package com.idep.policy.req.processor;


import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.proposal.carrier.req.processor.SoapUtils;
import com.idep.proposal.util.ProposalConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class PolicyConfigurationLoaderProcessor implements Processor {
	Logger log = Logger.getLogger(PolicyConfigurationLoaderProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;
	int carrierId;

	public void process(Exchange exchange) {
		try {
			String request = (String)exchange.getIn().getBody(String.class);
			JsonNode reqNode = SoapUtils.objectMapper.readTree(request);
			JsonNode loadedDoc = null;
			String docId = null;

			if(reqNode.has("transactionStausInfo") && reqNode.get("transactionStausInfo").has("apPreferId")){
				String payResId = reqNode.get("transactionStausInfo").get("apPreferId").asText();
				JsonNode payResDoc = SoapUtils.objectMapper.readTree(((JsonObject)SoapUtils.policyTrans.getDocBYId(payResId).content()).toString());
				String PaymentId = payResDoc.get("clientResponse").get("PaymentId").asText();
				String TransactionId = payResDoc.get("clientResponse").get("TransactionId").asText();
				((ObjectNode) reqNode).put("PaymentId",PaymentId);
				((ObjectNode) reqNode).put("TransactionId",TransactionId);

			}
			this.carrierId = reqNode.findValue("carrierId").asInt();
			int productId = reqNode.findValue("productId").asInt();
			String requestType = "HealthPolicyRequest";
			
			docId = "JOLT-" + requestType + "-" + this.carrierId + "-" + productId ;
			log.info("docId :"+docId);
			loadedDoc = SoapUtils.objectMapper.readTree(((JsonObject)SoapUtils.serverConfig.getDocBYId(docId).content()).toString());
			log.info("loadedDoc :"+loadedDoc);
			if (loadedDoc != null) {
				ObjectNode object = SoapUtils.objectMapper.createObjectNode();
				object.put("inputRequest", reqNode);
				object.put("configuration", loadedDoc.get("configuration"));
				exchange.setProperty("inputRequest", reqNode);
				exchange.getIn().setHeader("documentFound", "True");
				exchange.getIn().setBody(object);
			} else {
				this.log.error("Configuration Document Not Found for docId :" + docId);
				exchange.getIn().setHeader("documentFound", "False");
				ObjectNode objectNode = objectMapper.createObjectNode();
				objectNode.put(ProposalConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_CODE).intValue());
				objectNode.put(ProposalConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_MSG).textValue());
				objectNode.put(ProposalConstants.QUOTE_RES_DATA, this.errorNode);
				exchange.getIn().setBody(objectNode);        } 

		} catch (Exception e) {
			this.log.error("Exception at ConfigurationLoaderProcessor : " + e.getMessage());
			exchange.getIn().setHeader("documentFound", "False");
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put(ProposalConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_CODE).intValue());
			objectNode.put(ProposalConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(ProposalConstants.RESPONSE_CONFIG_DOC).get(ProposalConstants.ERROR_CONFIG_MSG).textValue());
			objectNode.put(ProposalConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);    } 
	}
}
