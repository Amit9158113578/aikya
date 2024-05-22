package com.idep.travelquote.req.processor;

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
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author sandeep.jadhav
 * Process request riders and categorize implicit,explicit and not applicable riders
 */
public class TravelReqRiderProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelReqRiderProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception{
		try{
			String quotedata = exchange.getIn().getBody().toString();
			JsonNode reqNode = this.objectMapper.readTree(quotedata);
			log.debug("TravelReqRiderProcessor input request : "+reqNode);
			JsonNode quoteParamNode = reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
			JsonNode productInfoNode = reqNode.get(TravelQuoteConstants.PRODUCT_INFO);

			if(quoteParamNode.has(TravelQuoteConstants.RIDERS)){
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

				if(productInfoNode.has(TravelQuoteConstants.RIDERS)){
					for(JsonNode pRider : productInfoNode.get(TravelQuoteConstants.RIDERS)){
						/**
						 * check rider type
						 */
						if(pRider.get(TravelQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("I")){
							implicitRiders.put(pRider.get(TravelQuoteConstants.RIDER_ID).asText(), pRider);
						}else if(pRider.get(TravelQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("G")){
							for(JsonNode uiRider : quoteParamNode.get(TravelQuoteConstants.RIDERS)){
								/**
								 * create rider array based on category
								 */
								if(uiRider.get(TravelQuoteConstants.RIDER_ID).asInt() == pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asInt()){
									groupedRiders.put(pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asText(), pRider);
									groupedRiderList.add(pRider.get(TravelQuoteConstants.RIDER_ID).asInt());
									break;
								}
							}
						}else if(pRider.get(TravelQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("D")){
							for(JsonNode uiRider : quoteParamNode.get(TravelQuoteConstants.RIDERS)){
								/**
								 * create rider array based on category
								 */
								if(uiRider.get(TravelQuoteConstants.RIDER_ID).asInt() == pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asInt()){
									if(dependentRiders.get(pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asText()) != null){
										ArrayNode dependentRider = (ArrayNode) dependentRiders.get(pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asText());
										dependentRider.add(pRider.get(TravelQuoteConstants.RIDER_ID).asInt());
										dependentRiders.put(pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asText(), dependentRider);
									}else{
										ArrayNode dependentRider = objectMapper.createArrayNode();
										dependentRider.add(pRider.get(TravelQuoteConstants.RIDER_ID).asInt());
										dependentRiders.put(pRider.get(TravelQuoteConstants.RIDER_GROUP_ID).asText(), dependentRider);
									}
									
									globalDependentRiders.add(pRider.get(TravelQuoteConstants.RIDER_ID).asInt());
									
									dependentRiderList.add(pRider.get(TravelQuoteConstants.RIDER_ID).asInt());
									ObjectNode dependentRiderInfo = objectMapper.createObjectNode();
									dependentRiderInfo.put(TravelQuoteConstants.RIDER_ID, pRider.get(TravelQuoteConstants.RIDER_ID).asInt());
									dependentRiderInfo.put(TravelQuoteConstants.RIDER_NAME, pRider.get(TravelQuoteConstants.RIDER_NAME).asInt());
									ArrayNode quoteParamRiderList = (ArrayNode) quoteParamNode.get(TravelQuoteConstants.RIDERS);
									quoteParamRiderList.add(dependentRiderInfo);
									break;
								}
							}
						}else if(pRider.get(TravelQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("E")){
							// Explicit rider array list.
						}else{
							nonSupportedRiders.put(pRider.get(TravelQuoteConstants.RIDER_ID).asText(), pRider);
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

				for(JsonNode uRider : quoteParamNode.get(TravelQuoteConstants.RIDERS)){
					/**
					 * create rider array based on category
					 */
					if(implicitRiders.has(uRider.get(TravelQuoteConstants.RIDER_ID).asText())){
						implicitRidersArrList.add(uRider);
					}else if(nonSupportedRiders.has(uRider.get(TravelQuoteConstants.RIDER_ID).asText())){
						nonApplRidersArrList.add(uRider);
					}else{
						if(!groupedRiderList.contains(uRider.get(TravelQuoteConstants.RIDER_ID).asInt()))
							explicitRidersList.add(uRider);
					}
					
					if(groupedRiders.has(uRider.get(TravelQuoteConstants.RIDER_ID).asText())){
						groupedRidersArrList.add(groupedRiders.get(uRider.get(TravelQuoteConstants.RIDER_ID).asText()));
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
				
				log.info("Implicit rider list : " + implicitRidersArrList);
				log.info("Grouped rider list : " + groupedRidersArrList);
				log.info("Non-Applicable rider list : " + nonApplRidersArrList);
				log.info("Modified rider list : " + explicitRidersList);
				
				((ObjectNode)reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM)).put(TravelQuoteConstants.RIDERS, explicitRidersList);
			}
			exchange.setProperty(TravelQuoteConstants.CARRIER_INPUT_REQUEST,this.objectMapper.writeValueAsString(reqNode));
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