package com.idep.travelquote.res.processor;

import java.text.DecimalFormat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelQuoteResponseProcessor implements  Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelQuoteResponseProcessor.class.getName());
	DecimalFormat decimalFormat = new DecimalFormat("##.#####");
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try
		{
			double premium =0.0;
			double sumInsured = 0.0;
			
		String quoteResdata = exchange.getIn().getBody().toString();
		JsonNode quoteResponseNode = this.objectMapper.readTree(quoteResdata);

		JsonNode quoteInputRequest = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
		log.info("quoteInputRequest property:::::: "+quoteInputRequest);
		if(quoteInputRequest.get(TravelQuoteConstants.PRODUCT_INFO).has(TravelQuoteConstants.FEATURES)){
			JsonNode Features = quoteInputRequest.get(TravelQuoteConstants.PRODUCT_INFO).get(TravelQuoteConstants.FEATURES);
			log.info("got Features  :::::: "+Features);
			if(Features.has("TravelInfo"+quoteInputRequest.get("travelDetails").get("sumInsured"))){
//				ArrayNode TravelInfo = objectMapper.createArrayNode();
				ArrayNode TravelInfo = (ArrayNode) Features.get("TravelInfo"+quoteInputRequest.get("travelDetails").get("sumInsured"));
//				TravelInfo.add(Features.get("TravelInfo"+quoteInputRequest.get("travelDetails").get("sumInsured")));
				((ObjectNode) Features).removeAll();
				((ObjectNode) Features).put("TravelInfo",TravelInfo);
				((ObjectNode) quoteResponseNode).put("Features",Features);
			}else if(quoteInputRequest.get("productInfo").get("carrierId").asText().equalsIgnoreCase("40")) {
				if( Features.has("TravelInfo"+quoteInputRequest.get("carrierResponse").get("TINS_XML_DATA").get("Segment").get("Insured").get("PlanCode").asText()))
				{
//					ArrayNode TravelInfo = objectMapper.createArrayNode();
					log.info("inside tata AIG Features  :::::: "+Features);
					ArrayNode TravelInfo = (ArrayNode) Features.get("TravelInfo"+quoteInputRequest.get("carrierResponse").get("TINS_XML_DATA").get("Segment").get("Insured").get("PlanCode").asText());
//					TravelInfo.add(Features.get("TravelInfo"+quoteInputRequest.get("travelDetails").get("sumInsured")));
					((ObjectNode) Features).removeAll();
					((ObjectNode) Features).put("TravelInfo",TravelInfo);
					((ObjectNode) quoteResponseNode).put("Features",Features);
			
				}
				else{
					((ObjectNode) quoteResponseNode).put("Features",Features);
				}
			}
			else{
				((ObjectNode) quoteResponseNode).put("Features",Features);	
			}
		}
		ArrayNode travelQuoteResult = objectMapper.createArrayNode();
		if(quoteInputRequest.get(TravelQuoteConstants.PRODUCT_INFO).has("claimIndex"))
		{
			((ObjectNode)quoteResponseNode).put("claimIndex", quoteInputRequest.get(TravelQuoteConstants.PRODUCT_INFO).get("claimIndex").asDouble());
		}
		else
		{
			((ObjectNode)quoteResponseNode).put("claimIndex",0);
		}
		if(quoteResponseNode.has("netPremium")){
			premium=quoteResponseNode.get("netPremium").asDouble();
		}
		
		if(quoteResponseNode.has("sumInsured")){
			sumInsured=quoteResponseNode.get("sumInsured").asDouble();
		}
		double premiumRatio =0.0;
		if(premium > 0 && sumInsured > 0){
			premiumRatio = Double.parseDouble(decimalFormat.format(premium/sumInsured));
		}
		((ObjectNode)quoteResponseNode).put("premiumRatio", premiumRatio);
		
		travelQuoteResult.add(quoteResponseNode);
		ObjectNode quoteResultNode = this.objectMapper.createObjectNode();
		quoteResultNode.put(TravelQuoteConstants.QUOTES, travelQuoteResult);
		
		
		ObjectNode objectNode = this.objectMapper.createObjectNode();
		objectNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_CODE).asInt());
		objectNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_MSG).asText());
		objectNode.put(TravelQuoteConstants.QUOTE_RES_DATA, quoteResultNode);
		log.info("objectNode in TravelQuoteResponseProcessor :"+objectNode);
		exchange.getIn().setBody(objectNode);
		}
		
		catch(Exception e)
		{
			log.error("Exception at TravelQuoteResponseProcessor : ",e);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
		    objectNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_CODE).intValue());
		    objectNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_MSG).textValue());
		    objectNode.put(TravelQuoteConstants.QUOTE_RES_DATA, errorNode);
		    exchange.getIn().setBody(objectNode);
		}
	}

}
