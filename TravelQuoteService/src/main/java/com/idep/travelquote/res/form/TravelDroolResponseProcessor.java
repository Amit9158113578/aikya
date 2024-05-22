package com.idep.travelquote.res.form;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelDroolResponseProcessor implements Processor {
	Logger log = Logger.getLogger(TravelDroolResponseProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode errorNode;
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		try{
			String message = exchange.getIn().getBody(String.class);
			JsonNode root = this.objectMapper.readTree(message);
			log.debug("root logs: "+root);
			String results = root.get("result").textValue();
					
			//String results = "{ \"result\": { \"results\": [ { \"key\": \"0\", \"value\": { \"com.sutrr.quote.travellerquotecalc.TravelQuoteRequest\": { \"isExecuted\": true, \"quoteParamRequestList\": { \"java.util.ArrayList\": [] } } } }, { \"key\": \"objects\", \"value\": { \"type\": \"LIST\", \"element\": [ { \"com.sutrr.quote.travellerquotecalc.TravelQuoteParam\": { \"source\": \"India\", \"productId\": 2, \"numOfMember\": 1, \"minAge\": 82, \"maxAge\": 199, \"gender\": \"NA\", \"productTerm\": 65, \"tripType\": \"Single\", \"preExtDisease\": \"N\", \"minSA\": 2500000, \"maxSA\": 2999999, \"premiumAmount\": 0, \"sumAssured\": 2500000, \"carrierId\": 46, \"age\": 35, \"carrier\": \"Religare Health Insurance Company Ltd\", \"isActiveStatus\": \"Y\", \"startDate\": \"13/04/2018\", \"endDate\": \"13/04/2018\", \"quoteType\": 5, \"insurerIndex\": 4.61828753399448, \"question_1_status\": \"Yes\", \"question_2_status\": \"Yes\", \"question_3_status\": \"Yes\", \"destination\": \"Japan\", \"travellersInfo\": { \"java.util.ArrayList\": [ { \"com.sutrr.quote.travellerquotecalc.TravellerDetails\": { \"age\": 23, \"gender\": \"Male\" } }, { \"com.sutrr.quote.travellerquotecalc.TravellerDetails\": { \"age\": 21, \"gender\": \"Female\" } } ] } } }, { \"com.sutrr.quote.travellerquotecalc.PremiumDetails\": { \"carrierId\": 46, \"productId\": 2, \"minAge\": 82, \"maxAge\": 199, \"gender\": \"NA\", \"productTerm\": 65, \"tripType\": \"Single\", \"preExtDisease\": \"N\", \"minSA\": 2500000, \"maxSA\": 2999999, \"sumAssured\": 2500000, \"premiumAmount\": 4177.14, \"numOfMembers\": 1 } } ] } } ], \"facts\": [ { \"key\": \"0\", \"value\": { \"external-form\": \"0:1:653258379:653258379:4:DEFAULT:NON_TRAIT:com.sutrr.quote.travellerquotecalc.TravelQuoteRequest\" } } ] } }";
			
			this.log.debug("Travel quote response from drool : " + results);

			JsonNode resultsNode = this.objectMapper.readTree(results);
			JsonNode quoteResult = resultsNode.get("results").get(1).get("value").get("element");
			this.log.debug("Travel quote results : " + quoteResult);

			ObjectNode objNode = objectMapper.createObjectNode();
			
			//JsonNode quoteResultNode =  quoteResult.get(0);
			JsonNode quoteNode = quoteResult.findValue("com.sutrr.quote.travellerquotecalc.TravelQuoteResponse");
			((ObjectNode) quoteNode).put("quoteType", 5);

			JsonNode inputReq = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.UI_QUOTEREQUEST).toString());
			
			JsonNode productInfo = inputReq.get(TravelQuoteConstants.PRODUCT_INFO);
			 ((ObjectNode)quoteNode).put(TravelQuoteConstants.QUOTE_ID, exchange.getProperty(TravelQuoteConstants.QUOTE_ID).toString());
			 ((ObjectNode)quoteNode).put("requestType", TravelQuoteConstants.CARRIER_QUOTE_RESPONSE);			
			if(inputReq!=null){
				((ObjectNode) quoteNode).put("inputReq", inputReq);
			}
	            /* 
				((ObjectNode) quoteNode).put("planId", inputReq.findValue("planId").asInt());
				((ObjectNode) quoteNode).put("planName", inputReq.findValue("productName").asText());
				((ObjectNode) quoteNode).put("insuranceCompany", inputReq.findValue("carrierName").asText());
				((ObjectNode) quoteNode).put("policyTerm", inputReq.findValue("policyTerm").asInt());
				((ObjectNode)quoteNode).put("adultCount", inputReq.findValue("adultCount").asInt());
				((ObjectNode)quoteNode).put("childCount", inputReq.findValue("childCount").asInt());
				((ObjectNode)quoteNode).put("totalCount", inputReq.findValue("totalCount").asInt());
                ((ObjectNode)quoteNode).put("travellers", inputReq.findValue("travellers"));	
				((ObjectNode)quoteNode).put("insurerIndex", inputReq.findValue("insurerIndex"));
				if(inputReq.get("productInfo").get("TripTypes").get(0).has("sumInsuredCurrency")){
				((ObjectNode)quoteNode).put("sumInsuredCurrency", inputReq.findValue("sumInsuredCurrency").asText());
				}
				if(inputReq.get("travelDetails").has("tripDuration")){
				((ObjectNode)quoteNode).put("tripDuration", inputReq.findValue("tripDuration").asInt());
				}*/
			
			objNode.putAll((ObjectNode)quoteNode);
			if(quoteNode.has("discountList")){
				ArrayNode discountArrayListDrool = objectMapper.createArrayNode();
				for(JsonNode discObj : quoteNode.get("discountList").get("java.util.ArrayList")){
					discountArrayListDrool.add(discObj.get("com.sutrr.quote.travelquotecalc.TravelDiscountDetails"));
				}
				 objNode.put("discountList", discountArrayListDrool);
				 this.log.debug("quoteResultNode discountArrayListDrool : " + discountArrayListDrool.toString());
			}

			if(quoteNode.has("riderList")){
				ArrayNode riderArrayListDrool = objectMapper.createArrayNode();
				for(JsonNode riderObj : quoteNode.get("riderList").get("java.util.ArrayList")){
					riderArrayListDrool.add(riderObj.get("com.sutrr.quote.travelquotecalc.TravelAddOnCover"));
				}
				objNode.put("riderList", riderArrayListDrool);
				this.log.debug("quoteResultNode riderArrayListDrool : " + riderArrayListDrool.toString());
			}
			this.log.debug("Final Quote Result genrated from Drool : " + objNode);
			
		      exchange.getIn().setHeader("documentId", TravelQuoteConstants.CARRIER_QUOTE_RESPONSE+"-"+productInfo.get(TravelQuoteConstants.CARRIER_ID).intValue()+
					  "-"+productInfo.get(TravelQuoteConstants.PLANID).intValue());
		      log.info("requestDocNode input to mapper: "+objNode);
			  exchange.getIn().setBody(this.objectMapper.writeValueAsString(objNode));
		    
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
