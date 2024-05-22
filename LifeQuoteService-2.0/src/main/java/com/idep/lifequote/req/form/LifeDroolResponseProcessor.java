package com.idep.lifequote.req.form;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.lifequote.exception.processor.ExecutionTerminator;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeDroolResponseProcessor implements Processor{

	Logger log = Logger.getLogger(LifeDroolResponseProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	//JsonObject responseConfigNode = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.RESPONSE_CONFIG_DOC);
	JsonNode errorNode;

	public void process(Exchange exchange) throws ExecutionTerminator{
		try{
			String message = exchange.getIn().getBody(String.class);
			JsonNode root = this.objectMapper.readTree(message);
			String results = root.get("result").textValue();
			this.log.info("Life quote response from drool : " + results);

			JsonNode resultsNode = this.objectMapper.readTree(results);
			JsonNode quoteResult = resultsNode.get("results").get(1).get("value").get("element");
			this.log.info("Life quote results : " + quoteResult);
			/*JsonNode prodRatingNode = this.objectMapper.readTree(exchange.getProperty("Ratings").toString());
			JsonNode productRating = null;
			JsonNode productRisksNode = null;

			productRisksNode = prodRatingNode.get("DefaultRiskType");
			
			 */
			ObjectNode objNode = objectMapper.createObjectNode();

			JsonNode quoteResultNode =  quoteResult.get(0);
			JsonNode quoteNode = quoteResultNode.get("com.sutrr.quote.lifequotecalc.LifeQuoteResponse");
			((ObjectNode) quoteNode).put("quoteType", 1);
			objNode.putAll((ObjectNode)quoteNode);
			
			int minAllowedPremium = (int) exchange.getProperty(LifeQuoteConstants.MIN_ALLOWED_PREMIUM);
		
			if(minAllowedPremium > 0)
			{
				int annualPremium = quoteNode.get("annualPremium").intValue();
				
				   if(annualPremium < minAllowedPremium ){
					   objNode.put("annualPremium", minAllowedPremium);
					}
			}
			
			if(quoteNode.has("discountList")){
				ArrayNode discountArrayListDrool = objectMapper.createArrayNode();
				for(JsonNode discObj : quoteNode.get("discountList").get("java.util.ArrayList")){
					discountArrayListDrool.add(discObj.get("com.sutrr.quote.lifequotecalc.LifeDiscountDetails"));
				}

				objNode.put("discountList", discountArrayListDrool);
				this.log.debug("quoteResultNode discountArrayListDrool : " + discountArrayListDrool.toString());
			}

			if(quoteNode.has("riderList")){
				ArrayNode riderArrayListDrool = objectMapper.createArrayNode();
				for(JsonNode riderObj : quoteNode.get("riderList").get("java.util.ArrayList")){
					riderArrayListDrool.add(riderObj.get("com.sutrr.quote.lifequotecalc.LifeAddOnCover"));
				}

				objNode.put("riderList", riderArrayListDrool);
				this.log.debug("quoteResultNode riderArrayListDrool : " + riderArrayListDrool.toString());
			}

			/*this.log.debug("quoteResultNode iterating : " + quoteNode.toString());
			if(productRating != null){
				for(JsonNode ratingNode : productRating){
					if((ratingNode.get("productId").intValue() == quoteNode.get("productId").intValue()) && (ratingNode.get("carrierId").intValue() == quoteNode.get("carrierId").intValue())){
						this.log.debug("matched rating found for product");
						objNode.put("ratingsList", ratingNode.get("categoryMap"));
					}
				}
			}else{
				objNode.put("ratingsList","");
			}*/
			/*log.info("Monthly Base Premium validation Started , monthlyBasePremium > 0 : "+quoteNode);
			if(quoteNode.has("monthlyBasePremium")){
							  
				if(quoteNode.get("monthlyBasePremium").asDouble() < 1){
					log.error("Monthly Base Premium 0 so not sending to UI : terminated Plan flow : "+quoteNode);
					throw new ExecutionTerminator();
				}
			}*/
			
			
			
			/*if(productRisksNode != null){
				objNode.put("risks", productRisksNode);
			}else{
				objNode.put("risks", "");
			}*/

			this.log.info("*********************  finalQuoteResult **********************  : " + objNode);
			exchange.getIn().setBody(objNode);
		}catch(NullPointerException e){
			this.log.error("********** failure at drool ***********: ",e);
			this.log.error("****************************************************************");
			throw new ExecutionTerminator();
		}catch(JsonProcessingException e){
			this.log.error("unable to process drool response JSON ",e);
			throw new ExecutionTerminator();
		}catch(IOException e){
			this.log.error("IOException ",e);
			throw new ExecutionTerminator();
		}catch(Exception e){
			this.log.error("Exception ",e);
			throw new ExecutionTerminator();
		}
	}
}