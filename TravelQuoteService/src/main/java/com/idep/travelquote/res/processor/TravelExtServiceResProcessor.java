package com.idep.travelquote.res.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class TravelExtServiceResProcessor implements Processor{

	  Logger log = Logger.getLogger(TravelExtServiceResProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	  JsonNode errorNode;
	  
	  public void process(Exchange exchange) throws Exception {

		  try {
			
		      String response  = exchange.getIn().getBody(String.class);
		      JsonNode finalResultNode = this.objectMapper.readTree(response);
		      ObjectNode resultNode = (ObjectNode)finalResultNode.get(TravelQuoteConstants.CARRIER_REQUEST_FORM);
		     
		      if(!(resultNode.hasNonNull("UIRiders")))
		      {
		    	 resultNode.remove("productRiders");
		    	 resultNode.remove("ridersList");
		      }
		      
		      else
		      {
		    	  JsonNode UIRiders = resultNode.get("UIRiders");
		    	  // step 1 : collect all UI (selected) riders and store it in object node
		    	  ObjectNode UIRidersNode = this.objectMapper.createObjectNode();
		    	  for(JsonNode uirider : UIRiders)
		    	  {
		    		  UIRidersNode.put(uirider.get("riderId").asText(), "Y");
		    		  
		    		  
		    	  }
		    	  JsonNode productriders = resultNode.get("productRiders");
		    	  JsonNode carrierRiders = resultNode.get("ridersList");
		    	  ArrayNode eligibleRiderList = this.objectMapper.createArrayNode();
		    	  // step 2 : find eligible riders by comparing selected and product configured riders
		    	  for(JsonNode uirider : UIRiders)
		    	  {
		    		  for(JsonNode priders: productriders)
		    		  {
		    			  if(uirider.get("riderId").intValue() == priders.get("riderId").intValue())
		    			  {
		    				  eligibleRiderList.add(priders);
		    				  break;
		    			  }
		    		  }
		    	  }
		    	  
		    	  ArrayNode finalArray = this.objectMapper.createArrayNode();
		    	  ObjectNode addedDependentRiders = this.objectMapper.createObjectNode();
		    	  
		    	  for(JsonNode elegRiders : eligibleRiderList)
		    	  {
		    		  
		    		  for(JsonNode cRiders : carrierRiders)
		    		  {
		    			  if(elegRiders.get("riderId").intValue() == cRiders.get("riderId").intValue())
		    			  {
		    				  double riderAmount = cRiders.get("riderValue").doubleValue();
		    				  
		    				  if(elegRiders.has("dependant"))
		    				  {
		    					  ArrayNode depRiderList = this.objectMapper.createArrayNode();
		    					  for(JsonNode depRider : elegRiders.get("dependant"))
		    					  {
		    						  // skip dependent rider if selected in UI 
		    						  // step 3 : skip dependent riders if present in step1
		    						  if(UIRidersNode.has(depRider.get("riderId").asText()))
		    						  {
			    						 this.log.debug("dependent riderId "+depRider.get("riderId").intValue()+" skipped as selected in UI");
		    						  }
		    						  else if(addedDependentRiders.has(depRider.get("riderId").asText()))
		    						  {
			    						this.log.debug("dependent riderId "+depRider.get("riderId").intValue()+" already processed");
		    						  }
		    						  else
		    						  {
		    							  for(JsonNode crider : carrierRiders)
			    						  {
			    							  if(depRider.get("riderId").intValue() == crider.get("riderId").intValue())
			    							  {
			    								  riderAmount = riderAmount + crider.get("riderValue").doubleValue();
			    								  ((ObjectNode)crider).put("riderType", "included");
			    								  depRiderList.add(crider);
			    								  // step 4 : add dependent riders to separate object node 
			    								  addedDependentRiders.put(depRider.get("riderId").asText(), "Y");
			    								  break;
			    							  }
			    						  }
		    						  }
		    					  }
		    					  
		    					  // add dependent riders
		    					  if(depRiderList.size()>0)
		    					  {
		    						  ((ObjectNode)cRiders).put("dependant", depRiderList);
		    					  }
		    					  
		    					  ((ObjectNode)cRiders).put("riderType", "selected");
		    					  ((ObjectNode)cRiders).put("totalValue", riderAmount);
		    					  finalArray.add(cRiders);  
		    				  }
		    				  else
		    				  {
		    					 
		    					((ObjectNode)cRiders).put("riderType", "selected");
			    				((ObjectNode)cRiders).put("totalValue", riderAmount);
			    				// add single rider
			    				finalArray.add(cRiders);
	    					    //break;
	    					    
		    				  }
		    				  
		    				  
		    			  }
		    		  }
		    	  }
		    	  
		    	  resultNode.remove("productRiders");
			      resultNode.remove("UIRiders");
			      
			      // get non-eligible riders from request and attach it to response
			      JsonNode requestDocNode = objectMapper.readTree(exchange.getProperty(TravelQuoteConstants.CARRIER_INPUT_REQUEST).toString());
			      ArrayNode nonEligRidersList = (ArrayNode)requestDocNode.get("quoteParam").get("nonEligibleUIRiders");
			      if(nonEligRidersList.size()>0)
			      {
			    	  for(JsonNode nonEligRider : nonEligRidersList)
			    	  {
			    		  ((ObjectNode)nonEligRider).put("riderType", "NA");
			    		  finalArray.add(nonEligRider); 
			    	  }
			      }
			      
			      resultNode.put("ridersList",finalArray);
			      
		      }
		      
		      
		      ArrayNode finalQuoteResult = this.objectMapper.createArrayNode();
	       	  finalQuoteResult.add(resultNode);
	       	  ObjectNode quoteResultNode = this.objectMapper.createObjectNode();
	       	  quoteResultNode.put(TravelQuoteConstants.QUOTES, finalQuoteResult);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(TravelQuoteConstants.SUCC_CONFIG_CODE).asInt());
		      objectNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get("ResponseMessages").get(TravelQuoteConstants.SUCC_CONFIG_MSG).asText());
		      objectNode.put(TravelQuoteConstants.QUOTE_RES_DATA, quoteResultNode);
		      exchange.getIn().setBody(objectNode);
		      
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+"TravelExtServiceResProcessor"+"|ERROR|"+"rider processing failed :",e);
		      throw new ExecutionTerminator();
		}
	}
}