package com.idep.lifequote.res.processor;

import java.text.DecimalFormat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.lifequote.util.LifeQuoteConstants;

public class PremiumRatioCalculateProcessor implements Processor {

	Logger log = Logger.getLogger(PremiumRatioCalculateProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	DecimalFormat decimalFormat = new DecimalFormat("##.#####");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode resNode = objectMapper.readTree( exchange.getIn().getBody(String.class));
			log.info("carrier response generated :  "+resNode);
			JsonNode quoteRequestNode = null;
			
			if(exchange.getProperty(LifeQuoteConstants.UI_CARQUOTEREQUEST)!=null)
			{
				quoteRequestNode=objectMapper.readTree(exchange.getProperty(LifeQuoteConstants.UI_CARQUOTEREQUEST).toString());
			}
			JsonNode productInfo = objectMapper.readTree(exchange.getProperty(LifeQuoteConstants.PRODUCT_INFO).toString());

			if(productInfo.has("claimIndex"))
			{
				((ObjectNode)resNode).put("claimIndex", productInfo.get("claimIndex").asDouble());
			}
			else
			{
				((ObjectNode)resNode).put("claimIndex",0.0);
			}
			
			if(productInfo.has("claimRatioLife"))
			{
				((ObjectNode)resNode).put("claimRatioLife", productInfo.get("claimRatioLife").asText());
			}
			else
			{
				((ObjectNode)resNode).put("claimRatioLife",0.0);
			}
			
			double premium =0.0;
			double sumInsured = 0.0;
			
			if(resNode.has("basicPremium")){
				premium=resNode.get("basicPremium").asDouble();
			}
			
			if(resNode.has("sumInsured")){
				sumInsured=resNode.get("sumInsured").asDouble();
			}
			double premiumRatio =0.0;
			if(premium > 0 && sumInsured > 0){
				premiumRatio = Double.parseDouble(decimalFormat.format(premium/sumInsured));
			}
			
			log.info("Carrier Response in added Premium Ratio : "+premiumRatio);
			((ObjectNode)resNode).put("premiumRatio", premiumRatio);
			
			/***
			 * benefit Index adding from product 
			 * ***/
			if(productInfo.has("benefitIndex"))
			{
				((ObjectNode)resNode).put("benefitIndex", productInfo.get("benefitIndex").asDouble());
			}
			else
			{
				((ObjectNode)resNode).put("benefitIndex",0.0);
			}
			
			/**
			 * 
			 * Added forProfessonal Journey need response in policyTerm.
			 * **/
			if(quoteRequestNode.has(LifeQuoteConstants.SERVICE_QUOTE_PARAM)){
				if(quoteRequestNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM).has(LifeQuoteConstants.POLICY_TERM)){
					((ObjectNode)resNode).put("policyTerm",quoteRequestNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM).get(LifeQuoteConstants.POLICY_TERM));
				}
			}
			exchange.getIn().setBody(resNode);
		}catch(Exception e){
			log.error("unable to calculate premium Ratio: ",e);
		}
	}

}
