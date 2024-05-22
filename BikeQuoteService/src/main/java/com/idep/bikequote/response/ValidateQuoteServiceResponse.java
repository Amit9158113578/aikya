package com.idep.bikequote.response;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.exception.processor.ExecutionTerminator;
import com.idep.bikequote.util.BikeQuoteConstants;
import com.idep.data.searchconfig.cache.DocumentDataConfig;

public class ValidateQuoteServiceResponse implements Processor {
	Logger log = Logger.getLogger(ValidateQuoteServiceResponse.class);

	ObjectMapper mapper = new ObjectMapper();

	public void process(Exchange exchange) throws Exception {
		try {
			String response = exchange.getIn().getBody().toString();
			JsonNode responseNode = this.mapper.readTree(response);
			if (response.contains("No service was found.")) {
				ObjectNode failure = this.mapper.createObjectNode();
				failure.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES103");
				failure.put(BikeQuoteConstants.QUOTE_RES_MSG, "invoke service down");
				failure.put(BikeQuoteConstants.QUOTE_RES_DATA, "error");
				exchange.getIn().setBody(responseNode);
			}
			if (responseNode.has(BikeQuoteConstants.QUOTE_RES_CODE)) {
				JsonNode configDoc = mapper.readTree(exchange.getProperty("configDoc").toString());
				if (responseNode.get(BikeQuoteConstants.QUOTE_RES_CODE).asText().equals(DocumentDataConfig.getConfigDocList()
						.get(BikeQuoteConstants.RESPONSE_CONFIG_DOC).get("ResponseCodeSuccess").asText())) {
					JsonNode inputRequest = this.mapper
							.readTree(exchange.getProperty(BikeQuoteConstants.REQUEST_DATA).toString());
					if (Integer.parseInt(exchange.getProperty("CamelLoopIndex").toString()) < Integer
							.parseInt(exchange.getIn().getHeader(BikeQuoteConstants.NO_SERVICE_INVOKE).toString()) - 1)
						responseNode = updateQuoteReqDetails(responseNode, inputRequest, configDoc);
					exchange.getIn().setBody(responseNode);
				} else {
					exchange.getIn().setHeader(BikeQuoteConstants.SERVICE_INVOKE, "False");
					responseNode = validateCarrierErrorResponse(responseNode, configDoc);
					exchange.getIn().setBody(responseNode);
				}
			} else {
				exchange.getIn().setHeader(BikeQuoteConstants.SERVICE_INVOKE, "False");
				ObjectNode failure = this.mapper.createObjectNode();
				failure.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES103");
				failure.put(BikeQuoteConstants.QUOTE_RES_MSG, "invoke service down");
				failure.put(BikeQuoteConstants.QUOTE_RES_DATA, "error");
				exchange.getIn().setBody(responseNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.log.error("Exception in quote response processor");
		}
	}

	public JsonNode updateQuoteReqDetails(JsonNode response, JsonNode inputRequest, JsonNode docConfig)
			throws Exception {
		ObjectNode updateProposalRequest = mapper.createObjectNode();
		try {
			updateProposalRequest.put(BikeQuoteConstants.DROOLS_CARRIERID, inputRequest.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText());
			updateProposalRequest.put("productId", inputRequest.findValue("productId").asText());
			updateProposalRequest.put(BikeQuoteConstants.QUOTE_ID, inputRequest.findValue(BikeQuoteConstants.QUOTE_ID).asText());
			updateProposalRequest.put(inputRequest.get("stage").asText()+"Response", response.get(BikeQuoteConstants.QUOTE_RES_DATA));
			if (docConfig != null) {
				
				if (docConfig.get(inputRequest.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText()).has("insurerReqDetails")) {
			          ArrayNode proposalUpdateReqDetails = (ArrayNode)docConfig.get(inputRequest.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText()).get("insurerReqDetails");
			          for (JsonNode key : proposalUpdateReqDetails)
			            updateProposalRequest.put(key.asText(), inputRequest.findValue(key.asText())); 
			        } 
				if (docConfig.has("commonQuoteInputReqDetails")) {
			          ArrayNode proposalUpdateReqDetails = (ArrayNode)docConfig.get("commonQuoteInputReqDetails");
			          for (JsonNode key : proposalUpdateReqDetails)
			            updateProposalRequest.put(key.asText(), inputRequest.findValue(key.asText())); 
			        } 
				ObjectNode responseNode = mapper.createObjectNode();
				responseNode.put("lob", inputRequest.findValue("businessLineId").asInt());
				responseNode.put(BikeQuoteConstants.REQUEST_DATA, (JsonNode) updateProposalRequest);
				responseNode=icicUserIDVUpdate(responseNode);
				return (JsonNode) responseNode;
			}
			((ObjectNode) inputRequest.get(BikeQuoteConstants.REQUEST_DATA)).put(inputRequest.get("stage").asText()+"Response", response.get(BikeQuoteConstants.QUOTE_RES_DATA));
			
			//ICICI user idv update logic
			
		} catch (Exception e) {
			ObjectNode createObjectNode = this.mapper.createObjectNode();
			createObjectNode.put(BikeQuoteConstants.DROOLS_CARRIERID, inputRequest.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asInt());
			createObjectNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES102");
			createObjectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "exception in update stage request details method ");
			createObjectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, e.getMessage());
			throw new ExecutionTerminator();
		}
		return inputRequest;
	}

	public JsonNode validateCarrierErrorResponse(JsonNode failureNode, JsonNode docConfig) throws Exception {
		try {
			if (!failureNode.has(BikeQuoteConstants.DROOLS_CARRIERID))
				return failureNode;
			if (docConfig.has("carrierErrorResponse")) {
				ObjectNode dataNode = this.mapper.createObjectNode();
				ArrayNode carrierErrorResponse = (ArrayNode) docConfig.get("carrierErrorResponse");
				JsonNode data = failureNode.get(BikeQuoteConstants.QUOTE_RES_DATA);
				for (JsonNode key : carrierErrorResponse) {
					if (data.findValue(key.asText()) != null) {
						JsonNode value = data.findValue(key.asText());
						String nodeType = value.getClass().getSimpleName();
						if (nodeType.equalsIgnoreCase("ArrayNode")) {
							dataNode.put(key.asText(), value);
							continue;
						}
						String valueStr = data.findValue(key.asText()).asText();
						dataNode.put(key.asText(), valueStr);
					}
				}
				((ObjectNode) failureNode).put(BikeQuoteConstants.QUOTE_RES_DATA, (JsonNode) dataNode);
			} else {
				return failureNode;
			}
		} catch (Exception e) {
			this.log.error("Error in validateCarrierErrorResponse method :" + e.toString());
		}
		return failureNode;
	}
	
	public ObjectNode icicUserIDVUpdate(JsonNode responseNode) {
		try {
			
		if (responseNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asInt() == 29) {
			if (responseNode.get(BikeQuoteConstants.REQUEST_DATA).has(BikeQuoteConstants.SERVICE_VEHICLE_PARAM)) {
				if (responseNode.get(BikeQuoteConstants.REQUEST_DATA).get(BikeQuoteConstants.SERVICE_VEHICLE_PARAM).get("IDV").asInt() > 0) {
					if (responseNode.get(BikeQuoteConstants.REQUEST_DATA).has("IDVResponse")) {
						if(responseNode.get(BikeQuoteConstants.REQUEST_DATA).get("IDVResponse").has("IDVResponse"))
						{
							((ObjectNode) responseNode.get(BikeQuoteConstants.REQUEST_DATA).get("IDVResponse").get("IDVResponse")).put("minimumprice",
									responseNode.get(BikeQuoteConstants.REQUEST_DATA).get(BikeQuoteConstants.SERVICE_VEHICLE_PARAM).get("IDV").asInt());
						}
					}
				}
			}
		}
		}catch (Exception e) {
			log.error("error in icicUserIDVUpdate method in validateQuoteServiceResponse class :"+e.getMessage());
		}
		return (ObjectNode)responseNode;

	}
}
