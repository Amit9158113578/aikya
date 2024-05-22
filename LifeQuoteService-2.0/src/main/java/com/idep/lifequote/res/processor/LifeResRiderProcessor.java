package com.idep.lifequote.res.processor;

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
 * @author sandeep.jadhav
 * Process response Riders
 */
public class LifeResRiderProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeResRiderProcessor.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception{
		try{
			String quoteResdata = exchange.getIn().getBody().toString();
			JsonNode quoteResponseNode = this.objectMapper.readTree(quoteResdata);
			log.debug("LifeResRiderProcessor input : " + quoteResponseNode);

			ArrayNode processedRidersList = objectMapper.createArrayNode();
			if(quoteResponseNode.has("riderList")&&quoteResponseNode.get("riderList").size()>0){
				/**
				 *  read exchange properties to get rider details
				 */
				JsonNode implicitRidersList = objectMapper.readTree(exchange.getProperty("implicitRidersList").toString());
				JsonNode groupedRidersList = objectMapper.readTree(exchange.getProperty("groupedRidersList").toString());
				JsonNode nonSupportedRidersList = objectMapper.readTree(exchange.getProperty("nonSupportedRidersList").toString());
				JsonNode dependentRiders = objectMapper.readTree(exchange.getProperty("dependentRiders").toString());
				ArrayNode globalDependentRiders = (ArrayNode) objectMapper.readTree(exchange.getProperty("globalDependentRiders").toString());

				/**
				 * add implicit riders in response
				 */
				for(JsonNode implicitRider : implicitRidersList){
					((ObjectNode)implicitRider).put(LifeQuoteConstants.RIDER_TYPE,"Included");
					((ObjectNode)implicitRider).put("riderPremiumAmount",0);
					processedRidersList.add(implicitRider);
				}

				/**
				 * add grouped riders in response
				 */
				for(JsonNode groupedRiders : groupedRidersList){
					((ObjectNode)groupedRiders).put(LifeQuoteConstants.RIDER_TYPE,"Included");
					((ObjectNode)groupedRiders).put("riderPremiumAmount",0);
					processedRidersList.add(groupedRiders);
				}

				/**
				 * add not applicable riders (as per product configuration)
				 */
				for(JsonNode nonApplRider : nonSupportedRidersList){
					((ObjectNode)nonApplRider).put(LifeQuoteConstants.RIDER_TYPE,"NA");
					((ObjectNode)nonApplRider).put("riderPremiumAmount",0);
					processedRidersList.add(nonApplRider);
				}
				/**
				 * process riders received from drools
				 */
				if(globalDependentRiders == null){
					for(JsonNode resRider : quoteResponseNode.get("riderList")){
						((ObjectNode)resRider).put(LifeQuoteConstants.RIDER_TYPE,"selected");
						processedRidersList.add(resRider);
					}
				}else{
					for(JsonNode resRider : quoteResponseNode.get("riderList")){
						// check whether this rider is dependent of any other rider.
						if(!globalDependentRiders.has(resRider.get(LifeQuoteConstants.RIDER_ID).asInt())){
							// check does this rider has dependent riders.
							if(dependentRiders.has(resRider.get(LifeQuoteConstants.RIDER_ID).asText())){
								double totalRiderPremium = resRider.get(LifeQuoteConstants.RIDER_PREMIUM_AMOUNT).asDouble();
								
								ArrayNode dependentRider = (ArrayNode) dependentRiders.get(resRider.get(LifeQuoteConstants.RIDER_ID).asText());
								
								for(int i = 0; i < dependentRider.size(); i++){
									for(JsonNode tempResRider : quoteResponseNode.get("riderList")){
										if(tempResRider.get(LifeQuoteConstants.RIDER_ID).asInt() == dependentRider.get(i).asInt()){
											totalRiderPremium += tempResRider.get(LifeQuoteConstants.RIDER_PREMIUM_AMOUNT).asDouble();
											break;
										}
									}
								}
								((ObjectNode)resRider).put(LifeQuoteConstants.RIDER_PREMIUM_AMOUNT,totalRiderPremium);
								((ObjectNode)resRider).put(LifeQuoteConstants.RIDER_TYPE,"selected");
								processedRidersList.add(resRider);
							}else{
								((ObjectNode)resRider).put(LifeQuoteConstants.RIDER_TYPE,"selected");
								processedRidersList.add(resRider);
							}
						}
					}
				}
				
				/**
				 * append rider list to response
				 */
				((ObjectNode)quoteResponseNode).put("riderList",processedRidersList);

				log.debug("Final rider list : " + processedRidersList);

				exchange.getIn().setBody(quoteResponseNode);
			}else{
				log.error("riders not available in quote response");
				
				exchange.getIn().setBody(quoteResponseNode);
			}
		}catch(NullPointerException e){
			this.log.error(e);
			throw new ExecutionTerminator();
		}catch(JsonProcessingException e){
			this.log.error(e);
			throw new ExecutionTerminator();
		}catch(IOException e){
			this.log.error("IOException ",e);
			throw new ExecutionTerminator();
		}catch(Exception e){
			log.error(e);
			throw new ExecutionTerminator();
		}
	}
}