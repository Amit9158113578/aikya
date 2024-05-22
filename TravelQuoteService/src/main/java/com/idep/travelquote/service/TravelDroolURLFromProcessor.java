package com.idep.travelquote.service;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelDroolURLFromProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelDroolURLFromProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	JsonNode droolURL = null;
	JsonNode errorNode;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		this.log.debug("Inside TravelDroolURLFromProcessor.");
		try{
			if(this.droolURL == null){
				this.droolURL = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.DROOL_URL_CONFIG).content().toString());
			}
			
			String quoteEngineRequest = exchange.getIn().getBody().toString();
			JsonNode reqNode = this.objectMapper.readTree(quoteEngineRequest);
			//JsonNode quoteParamNode = reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
			//((ObjectNode)quoteParamNode).put(TravelQuoteConstants.SERVICE_QUOTE_TYPE, 5);
			String validateQuoteType = TravelQuoteConstants.QUOTE_TYPE + 5;
			//JsonNode productInfoNode = reqNode.get(TravelQuoteConstants.PRODUCT_INFO);
			this.log.debug("-------Travel Quote : -----------------");
			this.log.debug("Mapper Input quoteParam node : " + quoteEngineRequest);
			this.log.debug("-----------------------------------------------------------------------------");
			
			// form quote request
			//FormTravelQuoteRequest travelQuoteRequest = new FormTravelQuoteRequest();
			
			//String quoteEngineRequest = travelQuoteRequest.formRequest(quoteParamNode, productInfoNode);
			
			if(quoteEngineRequest.equals(TravelQuoteConstants.EXCEPTION)){
				this.log.error("Travel DroolEngine Req not formed hence setting request flag to False ");
				this.log.error("Travel quoteEngineRequest : " + quoteEngineRequest);
				exchange.getIn().setHeader(TravelQuoteConstants.REQUESTFLAG, TravelQuoteConstants.FALSE);
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				objectNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_CODE).asInt());
				objectNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_MSG).asText());
				objectNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
				exchange.getIn().setBody(objectNode);
			}else{
				this.log.debug("Travel Quote request formed and sent to drool : " + quoteEngineRequest);
				exchange.setProperty(TravelQuoteConstants.CARRIER_REQUEST_FORM,quoteEngineRequest);
				exchange.getIn().setHeader(TravelQuoteConstants.REQUESTFLAG, TravelQuoteConstants.TRUE);
				exchange.getIn().setHeader(TravelQuoteConstants.QUOTE_URL, this.droolURL.get("TravelQuote-"+reqNode.findValue("carrierId").asInt()+"-"+reqNode.findValue("productId").asInt()+"-"+reqNode.findValue("planId").asInt()).asText());
				exchange.getIn().setHeader(TravelQuoteConstants.SERVICE_QUOTE_TYPE, validateQuoteType);
				exchange.getIn().setHeader(TravelQuoteConstants.DROOLS_AUTH_HEADER, TravelQuoteConstants.DROOLS_AUTH_DETAILS);
				exchange.getIn().setHeader(TravelQuoteConstants.DROOLS_CONTENT_TYPE_HEADER, TravelQuoteConstants.DROOLS_CONTENT_TYPE);
				exchange.getIn().setHeader(TravelQuoteConstants.CAMEL_HTTP_METHOD, TravelQuoteConstants.DROOLS_HTTP_METHOD);
				exchange.getIn().removeHeader(TravelQuoteConstants.CAMEL_HTTP_PATH);
				exchange.getIn().setHeader(TravelQuoteConstants.CAMEL_ACCEPT_CONTENT_TYPE, TravelQuoteConstants.DROOLS_ACCEPT_TYPE);
				
			}
			exchange.getIn().setBody(quoteEngineRequest);
		}catch(NullPointerException e){
			this.log.error("please check input request values provided, seems one of value is missing : ", e);
			exchange.getIn().setHeader(TravelQuoteConstants.REQUESTFLAG, TravelQuoteConstants.FALSE);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_CODE).asInt());
			objectNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_MSG).asText());
			objectNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);
		}catch(Exception e){
			this.log.error("Exception occurred, while preparing request for drool : " + e);
			exchange.getIn().setHeader(TravelQuoteConstants.REQUESTFLAG, TravelQuoteConstants.FALSE);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_CODE).asInt());
			objectNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_MSG).asText());
			objectNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);
		}
	}

}