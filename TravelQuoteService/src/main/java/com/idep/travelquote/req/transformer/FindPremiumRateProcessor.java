package com.idep.travelquote.req.transformer;


import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;


public class FindPremiumRateProcessor implements Processor {
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(FindPremiumRateProcessor.class.getName());
    CBService serverConfig=CBInstanceProvider.getServerConfigInstance();
	
	JsonNode TravelPremiumQueryConfig = null;
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub
		
		try{
			String quotedata = exchange.getIn().getBody().toString();
			JsonNode reqNode = this.objectMapper.readTree(quotedata);
			
			TravelPremiumQueryConfig=this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.TRAVEL_PREMIUM_QUERY_CONFIG).content().toString());
	
			if(TravelPremiumQueryConfig!=null)
				{
					double premiumAmount = getPremiumAmount(TravelPremiumQueryConfig,reqNode);
					if(premiumAmount > 0.0)
					{
						((ObjectNode)reqNode.get("quoteParam")).put("basePremium", premiumAmount);
						log.info("premium response found :"+reqNode);
					    exchange.getIn().setHeader(TravelQuoteConstants.VALIDATE_PREMIUM,TravelQuoteConstants.TRUE);
						exchange.getIn().setBody(reqNode);
					}
					else
					{
						log.error("Premium Amount Note Found Carrierid :"+reqNode.get(TravelQuoteConstants.PRODUCT_INFO).get(TravelQuoteConstants.CARRIER_ID).asInt()+" PlanId :"+reqNode.get(TravelQuoteConstants.PRODUCT_INFO).get(TravelQuoteConstants.PLANID).asInt());
						exchange.getIn().setHeader(TravelQuoteConstants.VALIDATE_PREMIUM,TravelQuoteConstants.FALSE);
						new ExecutionTerminator();
					}
			  }
			else
			{
				log.error("TravelPremiumQueryConfig document not found in serverConfig :"+TravelPremiumQueryConfig);
			    new ExecutionTerminator();
			}
			
			
		}catch(Exception e)
		{
			log.error("Exception found in FindPremiumRateProcessor :");
			e.printStackTrace();
		}
}
	
	public double getPremiumAmount(JsonNode TravelPremiumQueryConfig,JsonNode requestNode)
	{
		try{
			String statement = null;
			JsonNode quoteParamNode = requestNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
			JsonNode productInfoNode = requestNode.get(TravelQuoteConstants.PRODUCT_INFO);
		    JsonNode premiumPickingQuery = TravelPremiumQueryConfig.get(TravelQuoteConstants.PREMIUM_PICKING_QUERY);
			JsonNode premiumPickingQueryParams = TravelPremiumQueryConfig.get(TravelQuoteConstants.PREMIUM_PICKING_QUERY_PARAMS);
			JsonArray prodArray = JsonArray.create();
			for(JsonNode param : premiumPickingQueryParams){
				if(param.get(TravelQuoteConstants.DEFAULT_VALUE_STATUS).asBoolean()){
					if(param.get(TravelQuoteConstants.DATA_TYPE).asText().equalsIgnoreCase(TravelQuoteConstants.DATA_AS_TEXT)){
						prodArray.add(param.get(TravelQuoteConstants.DEFAULT_VALUE).asText());
					}else{
						prodArray.add(param.get(TravelQuoteConstants.DEFAULT_VALUE).asInt());
					}
				}else{
					if(param.get(TravelQuoteConstants.DATA_TYPE).asText().equalsIgnoreCase(TravelQuoteConstants.DATA_AS_TEXT)){
						prodArray.add(requestNode.get(param.get("reqNode").textValue()).get(param.get(TravelQuoteConstants.KEY).textValue()).textValue());
					}
					else if(param.has(TravelQuoteConstants.CASE)){
						
						if(param.get(TravelQuoteConstants.CASE).textValue().equals("toLower"))
						{
							prodArray.add(requestNode.get(TravelQuoteConstants.CASE).get(param.get(TravelQuoteConstants.KEY).asText()).asText().toLowerCase());
						}
						else
						{
							prodArray.add(requestNode.get(TravelQuoteConstants.CASE).get(param.get(TravelQuoteConstants.KEY).asText()).asText().toUpperCase());
						}
					}
					else if(param.get(TravelQuoteConstants.DATA_TYPE).asText().equalsIgnoreCase(TravelQuoteConstants.DATA_AS_INTEGER)){
						prodArray.add(requestNode.get(param.get("reqNode").asText()).get(param.get(TravelQuoteConstants.KEY).asText()).asInt());
					}
					else if(param.get(TravelQuoteConstants.DATA_TYPE).asText().equalsIgnoreCase(TravelQuoteConstants.DATA_AS_MAP)){
						
						String type = requestNode.get(param.get("reqNode").asText()).get(param.get(TravelQuoteConstants.KEY).asText()).asText();
						prodArray.add(param.get("mappingConfig").get(type).textValue());
					}
				else{
					    if(param.has(TravelQuoteConstants.REPLACE_KEY))
					    {
					    	if(requestNode.get(param.get("reqNode").asText()).get(param.get(TravelQuoteConstants.REPLACE_KEY).asText()).asInt()>0)
					    	{
					    			prodArray.add(requestNode.get(param.get("reqNode").asText()).get(param.get(TravelQuoteConstants.REPLACE_KEY).asText()).asInt());
					    	}
					    else
						{
								prodArray.add(requestNode.get(param.get(TravelQuoteConstants.KEY).asText()).asInt());
					    }
					    }
					    else
					    {
						prodArray.add(requestNode.get(param.get(TravelQuoteConstants.KEY).asText()).asInt());
					    }
					}
				}
			}
			Double amountResult = 0.0;
			JsonObject amount = JsonObject.create(); 
			List<JsonObject> premiumList = null;	
			if(productInfoNode.has("isIterateAsIndividual") && productInfoNode.get("isIterateAsIndividual").asText().equalsIgnoreCase("Y")){
				ArrayNode travellers = (ArrayNode) quoteParamNode.get("travellers");
				log.info("This was previous Array   : " + prodArray);
				for(JsonNode travellersInQuoteParam:travellers){
					int newAge = travellersInQuoteParam.get("age").asInt();
					List<Object> list  = prodArray.toList();
					list.set(3, newAge);
					prodArray = JsonArray.from(list);
					log.info("This Array was changed : " + prodArray);
						
					String queryPicking="I"+"-"+requestNode.get(TravelQuoteConstants.TRAVELDETAILS).get(TravelQuoteConstants.DROOLS_TRIPTYPE).textValue();
					statement = premiumPickingQuery.get(queryPicking).textValue();
					log.info("statement :"+statement);
			     	log.info("prodArray"+prodArray);
					premiumList= serverConfig.executeConfigParamArrQuery(statement, prodArray);
					
					log.info("premiumList value:::::"+premiumList);
					if(premiumList.get(0).containsKey("premiumAmount"))
					{
						amountResult = amountResult + (premiumList.get(0).getDouble("premiumAmount"));
						log.info("amountResult value:::::::"+amountResult);	
					}
					
				}
				log.info("final amountResult value:::::::"+amountResult);
				amount.put("premiumAmount", amountResult);
				if(premiumList != null)
					premiumList.set(0, amount);
				log.info("premiumList final  value:::::::"+premiumList);
			}
			else{
				String queryPicking=quoteParamNode.get(TravelQuoteConstants.DROOLS_PLAN_TYPE).textValue()+"-"+requestNode.get(TravelQuoteConstants.TRAVELDETAILS).get(TravelQuoteConstants.DROOLS_TRIPTYPE).textValue();
				statement = premiumPickingQuery.get(queryPicking).textValue();
				log.info("statement :"+statement);
		     	log.info("prodArray"+prodArray);
				premiumList= serverConfig.executeConfigParamArrQuery(statement, prodArray);
			}
			log.info("premiumList :"+premiumList);
			if(premiumList.size()>0)
			{
				
				return premiumList.get(0).getDouble("premiumAmount");
			}
			else
			{
				log.error("premium amount note found :"+premiumList);
				return 0.0;
			}
			
		}catch(Exception e)
			{
				log.error("error found to getPremiumAmount  in FindPremiumRateProcessor :",e);
				return 0.0;
			}
		
	}
	
}
