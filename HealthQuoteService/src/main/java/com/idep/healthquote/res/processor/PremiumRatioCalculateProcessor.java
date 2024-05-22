package com.idep.healthquote.res.processor;

import java.text.DecimalFormat;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PremiumRatioCalculateProcessor implements Processor {

	Logger log = Logger.getLogger(PremiumRatioCalculateProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	DecimalFormat decimalFormat = new DecimalFormat("##.#####");
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			double premiumRatio =0.0;
			double premium =0.0;
			double sumInsured = 0.0;
			
			JsonNode resNode = objectMapper.readTree( exchange.getIn().getBody(String.class));
			log.info("carrier response generated :  "+resNode);
			JsonNode UIReq = objectMapper.readTree(exchange.getProperty("HealthProductUIReq").toString());
			double claimIndex =0.0;
			String claimRatio =null;
			double hospitalIndex =0.0;
			double benefitIndex=0.0;
			log.info("UIReq in productInfo  "+UIReq);
			if(UIReq!=null){
				
				ArrayNode prductRider = (ArrayNode)UIReq.get("productInfo").get("riderList");
				
				log.info("UI Request for Respoonse Filter : "+UIReq);
				if(UIReq.get("productInfo").has("claimIndex")){
					claimIndex = 	UIReq.get("productInfo").get("claimIndex").asDouble();
				}
				if(UIReq.get("productInfo").has("claimRatio")){
					claimRatio = 	UIReq.get("productInfo").get("claimRatio").asText();
				}
				if(UIReq.get("productInfo").has("hospitalIndex")){
					hospitalIndex = 	UIReq.get("productInfo").get("hospitalIndex").asDouble();
				}
				if(UIReq.get("productInfo").has("benefitIndex")){
					benefitIndex = 	UIReq.get("productInfo").get("benefitIndex").asDouble();
				}
			}
			if(resNode.has("carrierRequestForm")){
			
			if(resNode.get("carrierRequestForm").has("basicPremium")){
				premium=resNode.get("carrierRequestForm").get("basicPremium").asDouble();
			}
			
			if(resNode.get("carrierRequestForm").has("sumInsured")){
				sumInsured=resNode.get("carrierRequestForm").get("sumInsured").asDouble();
			}
			
			if(premium > 0 && sumInsured > 0){
				premiumRatio = Double.parseDouble(decimalFormat.format(premium/sumInsured));
			}
			log.info("Carrier Response in added Premium Ratio : "+premiumRatio);
			((ObjectNode)resNode.get("carrierRequestForm")).put("premiumRatio", premiumRatio);
			((ObjectNode)resNode.get("carrierRequestForm")).put("claimIndex",claimIndex);
			((ObjectNode)resNode.get("carrierRequestForm")).put("hospitalIndex",hospitalIndex);
			((ObjectNode)resNode.get("carrierRequestForm")).put("benefitIndex",benefitIndex);
			((ObjectNode)resNode.get("carrierRequestForm")).put("claimRatio",claimRatio);
			}
			
			else{
				if(resNode.has("basicPremium")){
					premium=resNode.get("basicPremium").asDouble();
				}
				
				if(resNode.has("sumInsured")){
					sumInsured=resNode.get("sumInsured").asDouble();
				}
				
				if(premium > 0 && sumInsured > 0){
					premiumRatio = Double.parseDouble(decimalFormat.format(premium/sumInsured));
				}
				log.info("Carrier Response in added Premium Ratio : "+premiumRatio);
				((ObjectNode)resNode).put("premiumRatio", premiumRatio);
				((ObjectNode)resNode).put("claimIndex",claimIndex);
				((ObjectNode)resNode).put("claimRatio",claimRatio);
				((ObjectNode)resNode).put("hospitalIndex",hospitalIndex);
				((ObjectNode)resNode).put("benefitIndex",benefitIndex);
			}
			
			
			exchange.getIn().setBody(resNode);
		}catch(Exception e){
			log.error("unable to calculate premium Ratio: ",e);
		}
	}

}
