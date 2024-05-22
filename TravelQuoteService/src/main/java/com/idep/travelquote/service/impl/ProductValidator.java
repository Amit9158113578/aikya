package com.idep.travelquote.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.IntegratedParserConfiguration;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.req.processor.TravelCountryCacheProcessor;
import com.idep.travelquote.util.DateFormatter;
import com.idep.travelquote.util.TravelQuoteConstants;

public class ProductValidator {
	Logger log = Logger.getLogger(ProductValidator.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	DateFormatter dateformat = new DateFormatter();
	ObjectNode allCarrierCountryList = null;
	public ArrayNode validateTravelProduct(JsonNode reqNode, List<JsonObject> productsList)
	{
		ArrayNode finalProductList = this.objectMapper.createArrayNode();
		try{
			ObjectNode processedProductList = objectMapper.createObjectNode();
			JsonNode allProductList = objectMapper.readTree(productsList.toString());
			JsonNode quoteParamNode = reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
			int totalRidersSelected = quoteParamNode.get(TravelQuoteConstants.RIDERS) != null ? quoteParamNode.get(TravelQuoteConstants.RIDERS).size() : 0;
			if(totalRidersSelected > 0){
				for(JsonNode product : allProductList){
					if(!productExist(processedProductList, product)){
						if(validateProduct(product, reqNode, processedProductList)){
							finalProductList.add(product);
						}
					}
				}
			}else{
				for(JsonNode product : allProductList){
					//finalProductList.add(product);
					if(validateProduct(product, reqNode, processedProductList)){
						finalProductList.add(product);
					}
				}
			}
		}catch(Exception e){
			this.log.error("Exception at TravelRequestProcessor : ", e);
		}
		return finalProductList;
	}

	public boolean productExist(ObjectNode processedProductList, JsonNode product){
		this.log.info("Inside product exist status check to avoid duplicate products.");
		boolean productStatus = false;
		if(processedProductList != null && processedProductList.size() > 0){
			for(JsonNode processedProduct : processedProductList) {
				if(processedProduct.get(TravelQuoteConstants.CARRIER_ID).asInt() == product.get(TravelQuoteConstants.CARRIER_ID).asInt()
						&& processedProduct.get(TravelQuoteConstants.CARRIER_PRODUCT_ID).asInt() == product.get(TravelQuoteConstants.CARRIER_PRODUCT_ID).asInt()){
					productStatus = true;
					break;
				}
			}
		}
		
		if(productStatus){
			this.log.info("Product already selected for carrier : " + product.get(TravelQuoteConstants.CARRIER_ID) + " : " + product.get(TravelQuoteConstants.CARRIER_PRODUCT_ID) + " : " + product.get(TravelQuoteConstants.PRODUCT_ID));			
		}
		this.log.info("Product exist status check completed.");
		return productStatus;
	}
	
	
	public boolean validateProduct(JsonNode product, JsonNode reqNode,ObjectNode processedProductList)throws ExecutionTerminator
	{
		boolean productFlag=false;
		boolean destinationPresent = false;
		int productCount=0;

		try{
			JsonNode travelDetails = reqNode.get(TravelQuoteConstants.TRAVELDETAILS);
			
			JsonNode quoteParam = reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
			log.debug("Product Details : "+product);
			
			/**
			 * Validating sumInsured is in the range
			 * */
			if(product.has("minAllowedSA") && product.has("maxAllowedSA")){
				productFlag=validateSumInsured(product.get("minAllowedSA").asDouble(), product.get("maxAllowedSA").asDouble(),travelDetails.get("sumInsured").asDouble());
			}
			if(!productFlag){
				log.debug("Product not picked due to sumInsured not in range : "+product.findValue("carrierId").asText()+" : "+product.findValue("productId").asText());
				return productFlag;
			}
			if(product.has("minmember") && product.has("maxmember")){
				productFlag=validateMemberCount(product.get("minmember").asInt(), product.get("maxmember").asInt(),quoteParam.get("travellers").size());
			}
			if(!productFlag){
				log.debug("Product not picked due to MemberCount not in range : "+product.findValue("carrierId").asText()+" : "+product.findValue("planId").asText());
				return productFlag;
			}
			/**
			 * Validating policyTerm is in the range
			 * */
			if(product.has("minAllowedTerm") && product.has("maxAllowedTerm")){
				
				//productFlag=validatePolicyTerm(product.get("minAllowedTerm").asInt(), product.get("maxAllowedTerm").asInt(),travelDetails.get("policyterm").asInt());
				log.debug("POlicy term Calculated : "+dateformat.getDaysDifference(travelDetails.get("startdate").asText(), travelDetails.get("enddate").asText()));
				((ObjectNode)reqNode).put("policyTerm",dateformat.getDaysDifference(travelDetails.get("startdate").asText(), travelDetails.get("enddate").asText()));
				productFlag=validatePolicyTerm(product.get("minAllowedTerm").asInt(), product.get("maxAllowedTerm").asInt(),(int) dateformat.getDaysDifference(travelDetails.get("startdate").asText(), travelDetails.get("enddate").asText()));
				
				
			}
			if(!productFlag){
				log.debug("Product not picked due to policyterm not in range : "+product.findValue("carrierId").asText()+" : "+product.findValue("productId").asText());
				return productFlag;
			}
			
			/**
			 * Validating TripDuration
			 * */
			if(product.has("minTripDuration") && product.has("maxTripDuration")){
				log.debug("Inside TripDuration validation");
				log.debug("product inside TripDuration validation: "+product);
				productFlag=validateTripDuration(product.get("minTripDuration").asInt(),product.get("maxTripDuration").asInt(),travelDetails.get("tripDuration").asInt());
			}
			if(!productFlag){
				log.info("Product not picked due to tripDuration not in range : "+product.findValue("carrierId").asText()+" : "+product.findValue("productId").asText());
				return productFlag;
			}
			
			
			if(product.has("TripTypes") ){
				
				ArrayNode ageValidateNode = (ArrayNode)product.get("TripTypes");
				ArrayNode inputAgeNode = (ArrayNode)quoteParam.get("travellers");
				for(JsonNode inputageVal:inputAgeNode)
		    	  {
	    	  		int inputAge = inputageVal.get("age").asInt();
	    	  		for(JsonNode ageVal:ageValidateNode)
			    	  {
		    	  		int minAge = ageVal.get("MinAgeAtEntry").asInt();
		    	  		int maxAge = ageVal.get("MaxAgeofEntry").asInt();
		 				productFlag=validateAge(minAge,maxAge,inputAge);

			    		  
			    	  }

		    		  
		    	  }
				
			}
			if(!productFlag){
				log.debug("Product not picked due to Age not in range : "+product.findValue("carrierId").asText()+" : "+product.findValue("productId").asText());
				return productFlag;
			}
			/****
			 * Validating product on selection on country  
			 * **/
		
			
			  if(product.get("TripTypes").get(0).has("minNoOfDays")){
		    	  	this.log.debug("Inside MultiTripTravel Validation");
					productFlag=validateMultiTripNumberOfDays(product.get("TripTypes").get(0).get("minNoOfDays").asInt(), product.get("TripTypes").get(0).get("maxNoOfDays").asInt(),travelDetails.get("tripDuration").asInt());
		      }
		      
		      if (!productFlag)
		      {
		        this.log.debug("Product not picked MultiTrip Number Of days Validation  : " + product.findValue("carrierId").asText() + "-" + product.findValue("productId").asText() + "-" + product.findValue("planId").asText());
		        return productFlag;
		      }
			  if(quoteParam.get("pedStatus").asText().equalsIgnoreCase("Y")){
				if(product.has("isPedSupported") && product.get("isPedSupported").asText().equalsIgnoreCase(quoteParam.get("pedStatus").asText()) ){
		    	  	productFlag=true;
				}else{
		    	  productFlag=false;
				}
		  	}
		      if (!productFlag)
		      {
		        this.log.debug("Product Failed due to diseases not supported  : " + product.findValue("carrierId").asText() + "-" + product.findValue("productId").asText() + "-" + product.findValue("planId").asText());
		        return productFlag;
		      }
		      if(product.has("isSelfMandatory"))
		      {
			      if( product.get("isSelfMandatory").asText().equalsIgnoreCase("Y") ){
			    	  
			    	  ArrayNode travellers = (ArrayNode)quoteParam.get("travellers");
						for(JsonNode member:travellers)
				    	{
							 this.log.info("inside Traveller relation  Node  : " +member.get("relation").asText());
			 				productFlag=validateIsSelfMandatory(member.get("relation").asText());
			    	  		if(productFlag==true)
			    	  			break;
			    	  	}
				  }
		      }
		      if (!productFlag)
		      {
		        this.log.info("Product Failed due Self is not Present  : " + product.findValue("carrierId").asText() + "-" + product.findValue("productId").asText() + "-" + product.findValue("planId").asText());
		        return productFlag;
		      }
		      
		      
		      productFlag = validateProductCountry(product, reqNode);
		      if (!productFlag)
		      {
		        this.log.debug("Product not picked due country not avilable for this plan  : " + product.findValue("carrierId").asText() + "-" + product.findValue("productId").asText() + "-" + product.findValue("planId").asText());
		        return productFlag;
		      }
			this.log.debug("Product validation process completed.");
			return productFlag;
		}
		catch(Exception e){
			log.error("unable to valiidate product ReqNode  : "+reqNode);
			log.error("unable to valiidate product because : ",e);
			throw new  ExecutionTerminator();
		}
		
	} 
	
	public boolean validateAge(int minAge,int maxAge,int inputAge ){
		
		if(minAge <= inputAge && maxAge >= inputAge){
			return true;
		}else{
			return false;
		}
		
	}
	
	public boolean validateSumInsured(double minSumInsured , double maxSumInsured,double reqSumInsured){
		
		if(minSumInsured <= reqSumInsured && maxSumInsured >= reqSumInsured){
			return true;
		}else{
			log.debug("Product not picked due to SumInsured range ");
			return false;
		}
	}
	public boolean validateIsSelfMandatory(String relation ){
		
		if(relation.equalsIgnoreCase("Self")){
			
			return true;
		}else{
			
			return false;
		}
		
	}
	
	public boolean validatePolicyTerm(int minPolicyTerm , int maxPolicyTerm, int reqPolicyTerm){
		
		if(minPolicyTerm<=reqPolicyTerm &&  maxPolicyTerm >= reqPolicyTerm){
			return true;
		}
		return false;
	}
	
	public boolean validateTripDuration(int minTripDuration ,int maxTripDuration , int uiTripDuration){
		
		if(minTripDuration<=uiTripDuration &&  maxTripDuration >= uiTripDuration){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean validateMemberCount(int minMember , int maxMember, int uiMember){
		
		if(minMember<=uiMember &&  maxMember >= uiMember){
			return true;
		}else{
			return false;
		}
	}
	 public boolean validateProductCountry(JsonNode product, JsonNode reqNode)
	  {
	    int carrierId = 0;int productId = 0;int planId = 0;
	    boolean productValidate = false;
	    ArrayNode CarrierCountryListNode=objectMapper.createArrayNode();
	    try
	    {
	      carrierId = product.findValue("carrierId").asInt();
	      productId = product.findValue("productId").asInt();
	      planId = product.findValue("planId").asInt();
	      
	      JsonNode CountryList = TravelCountryCacheProcessor.getAllCarrierCountryList().get("CarrierCountryConfig");
	      if ((carrierId != 0) && (productId != 0) && (planId != 0))
	      {
	        if (CountryList.has("DestinationDetailsMapping-" + carrierId + "-" + productId + "-" + planId))
	        {
	          ArrayNode CarrierCountryList = (ArrayNode)CountryList.get("DestinationDetailsMapping-" + carrierId + "-" + productId + "-" + planId).get("TravelContinentList");
	          if (CarrierCountryList.size() > 0)
	          {
	            ArrayNode UIDestination = (ArrayNode)reqNode.get("travelDetails").get("destinations");
	            int counter=0;
	            boolean isCountryMandatoryFlag=false;
	            for (JsonNode destination : UIDestination)
	            {
	              
	    
	              if (CarrierCountryList.get(0).has(destination.get("countryCode").asText()))
	              {
		            	CarrierCountryListNode.add( CarrierCountryList.get(0).get(destination.get("countryCode").asText()));
		            	
		            	/* below code only applicable if "isCountryMandatory" is Y in productData (TravelProduct-36-1-1)
		            	 * and "isMandatory" is present and = Y in your destination mapping refer (DestinationMapping-36-1-1)
		            	 */
		  		      if(product.has("isCountryMandatory") && product.get("isCountryMandatory").asText().equalsIgnoreCase("Y")){
		  		    	  if(CarrierCountryList.get(0).get(destination.get("countryCode").asText()).has("isMandatory")&& CarrierCountryList.get(0).get(destination.get("countryCode").asText()).get("isMandatory").asText().equalsIgnoreCase("Y")){
		  		    		isCountryMandatoryFlag=true;
		  		    		counter++;
		  		    	  }
		  		      }else{

		            	((ObjectNode)product).put("CarrierCountryListNode",CarrierCountryListNode );
		            	productValidate = true;
		  		      }
	              }
	              else
	              {
	                productValidate = false;
	                break;
	              }
	            }
	            if(isCountryMandatoryFlag==true){
	            	if(counter>0){
	            		this.log.info("counter isCountryMandatoryFlag count : "+counter);
	            		((ObjectNode)product).put("CarrierCountryListNode",CarrierCountryListNode );
	            		productValidate = true;
	  		    	  }else{
	  		    		productValidate = false;
	  		    	  }
  		    	  }
	          }
	          else
	          {
	            this.log.error("Product failed at country valdiation : DestinationDetailsMapping-" + carrierId + "-" + productId + "-" + planId);
	            productValidate = false;
	          }
	        }
	        else
	        {
	          this.log.error("Product failed at country valdiation : DestinationDetailsMapping-" + carrierId + "-" + productId + "-" + planId);
	        }
	      }
	      else {
	        this.log.debug("document not presnt in Cache List : DestinationDetailsMapping-" + carrierId + "-" + productId + "-" + planId);
	      }
	    }
	    catch (Exception e)
	    {
	      this.log.debug("unable to validate Country List : DestinationDetailsMapping-" + carrierId + "-" + productId + "-" + planId);
	    }
	    return productValidate;
	  }
	  	 public boolean validateMultiTripNumberOfDays(int minNoOfDays , int maxNoOfDays, int uiNoOfdays){
			
			if(minNoOfDays<=uiNoOfdays &&  maxNoOfDays >= uiNoOfdays){
				return true;
			}
			return false;
		}

	
}