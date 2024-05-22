package com.idep.lifequote.req.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * @author sandeep.jadhav
 * Process request riders and categorize implicit,explicit and not applicable riders
 */
public class LifeReqRiderProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeReqRiderProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception{
		try{
			String quotedata = exchange.getIn().getBody().toString();
			JsonNode reqNode = this.objectMapper.readTree(quotedata);
			log.debug("LifeReqRiderProcessor input request : "+reqNode);
			JsonNode quoteParamNode = reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM);
			JsonNode productInfoNode = reqNode.get(LifeQuoteConstants.PRODUCT_INFO);

			if(quoteParamNode.has(LifeQuoteConstants.RIDERS)){
				/**
				 * collect all implicit and not applicable riders from product configuration
				 */
				ObjectNode implicitRiders = objectMapper.createObjectNode();
				ObjectNode nonSupportedRiders = objectMapper.createObjectNode();
				ObjectNode groupedRiders = objectMapper.createObjectNode();
				ObjectNode dependentRiders = objectMapper.createObjectNode();
				ArrayNode globalDependentRiders = objectMapper.createArrayNode();
				List<Integer> groupedRiderList = new ArrayList<>();
				List<Integer> dependentRiderList = new ArrayList<>();

				if(productInfoNode.has(LifeQuoteConstants.RIDERS)){
					for(JsonNode pRider : productInfoNode.get(LifeQuoteConstants.RIDERS)){
						/**
						 * check rider type
						 */
						if(pRider.get(LifeQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("I")){
							implicitRiders.put(pRider.get(LifeQuoteConstants.RIDER_ID).asText(), pRider);
						}else if(pRider.get(LifeQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("G")){
							for(JsonNode uiRider : quoteParamNode.get(LifeQuoteConstants.RIDERS)){
								/**
								 * create rider array based on category
								 */
								if(uiRider.get(LifeQuoteConstants.RIDER_ID).asInt() == pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asInt()){
									groupedRiders.put(pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asText(), pRider);
									groupedRiderList.add(pRider.get(LifeQuoteConstants.RIDER_ID).asInt());
									break;
								}
							}
						}else if(pRider.get(LifeQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("D")){
							for(JsonNode uiRider : quoteParamNode.get(LifeQuoteConstants.RIDERS)){
								/**
								 * create rider array based on category
								 */
								if(uiRider.get(LifeQuoteConstants.RIDER_ID).asInt() == pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asInt()){
									if(dependentRiders.get(pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asText()) != null){
										ArrayNode dependentRider = (ArrayNode) dependentRiders.get(pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asText());
										dependentRider.add(pRider.get(LifeQuoteConstants.RIDER_ID).asInt());
										dependentRiders.put(pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asText(), dependentRider);
									}else{
										ArrayNode dependentRider = objectMapper.createArrayNode();
										dependentRider.add(pRider.get(LifeQuoteConstants.RIDER_ID).asInt());
										dependentRiders.put(pRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asText(), dependentRider);
									}
									
									globalDependentRiders.add(pRider.get(LifeQuoteConstants.RIDER_ID).asInt());
									
									dependentRiderList.add(pRider.get(LifeQuoteConstants.RIDER_ID).asInt());
									ObjectNode dependentRiderInfo = objectMapper.createObjectNode();
									dependentRiderInfo.put(LifeQuoteConstants.RIDER_ID, pRider.get(LifeQuoteConstants.RIDER_ID).asInt());
									dependentRiderInfo.put(LifeQuoteConstants.RIDER_NAME, pRider.get(LifeQuoteConstants.RIDER_NAME).asText());
									ArrayNode quoteParamRiderList = (ArrayNode) quoteParamNode.get(LifeQuoteConstants.RIDERS);
									quoteParamRiderList.add(dependentRiderInfo);
									break;
								}
							}
						}else if(pRider.get(LifeQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("E")){
							// Explicit rider array list.
						}else{
							nonSupportedRiders.put(pRider.get(LifeQuoteConstants.RIDER_ID).asText(), pRider);
						}
					}
				}else{
					log.error("riders are not configured for this product");
				}

				/**
				 *  prepare implicit,not applicable and explicit riders list
				 */
				ArrayNode explicitRidersList = objectMapper.createArrayNode();
				ArrayNode implicitRidersArrList = objectMapper.createArrayNode();
				ArrayNode nonApplRidersArrList = objectMapper.createArrayNode();
				ArrayNode groupedRidersArrList = objectMapper.createArrayNode();

				for(JsonNode uRider : quoteParamNode.get(LifeQuoteConstants.RIDERS)){
					/**
					 * create rider array based on category
					 */
					if(implicitRiders.has(uRider.get(LifeQuoteConstants.RIDER_ID).asText())){
						implicitRidersArrList.add(uRider);
					}else if(nonSupportedRiders.has(uRider.get(LifeQuoteConstants.RIDER_ID).asText())){
						nonApplRidersArrList.add(uRider);
					}else{
						if(!groupedRiderList.contains(uRider.get(LifeQuoteConstants.RIDER_ID).asInt()))
							explicitRidersList.add(uRider);
					}
					
					if(groupedRiders.has(uRider.get(LifeQuoteConstants.RIDER_ID).asText())){
						groupedRidersArrList.add(groupedRiders.get(uRider.get(LifeQuoteConstants.RIDER_ID).asText()));
					}
				}

				/**
				 * store rider information in property. This will be used while forming rider response
				 */
				exchange.setProperty("implicitRidersList", implicitRidersArrList);
				exchange.setProperty("groupedRidersList", groupedRidersArrList);
				exchange.setProperty("nonSupportedRidersList", nonApplRidersArrList);
				exchange.setProperty("dependentRiders", dependentRiders);
				exchange.setProperty("globalDependentRiders", globalDependentRiders);
				
				log.debug("Implicit rider list : " + implicitRidersArrList);
				log.debug("Grouped rider list : " + groupedRidersArrList);
				log.debug("Non-Applicable rider list : " + nonApplRidersArrList);
				log.debug("Modified rider list : " + explicitRidersList);
				
				((ObjectNode)reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM)).put(LifeQuoteConstants.RIDERS, explicitRidersList);
			}
			exchange.getIn().setBody(reqNode);
		}catch(NullPointerException e){
			this.log.error(e);
			throw new ExecutionTerminator();
		}catch(JsonProcessingException e){
			this.log.error(e);
			throw new ExecutionTerminator();
		}catch(IOException e){
			this.log.error("IOException : " + e);
			throw new ExecutionTerminator();
		}catch(Exception e){
			log.error(e);
			throw new ExecutionTerminator();
		}
	}
}