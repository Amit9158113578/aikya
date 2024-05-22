package com.idep.lifequote.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.lifequote.util.LifeQuoteConstants;

public class ProductValidator {
	Logger log = Logger.getLogger(ProductValidator.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	public ArrayNode validateLifeProduct(JsonNode quoteParamNode, List<JsonObject> productsList){
		ArrayNode finalProductList = this.objectMapper.createArrayNode();
		try{
			log.info("inside ProductValidator");
			
			
			ObjectNode processedProductList = objectMapper.createObjectNode();
			JsonNode allProductList = objectMapper.readTree(productsList.toString());
			
			
			this.log.debug("Inside product validation to pick correct product as per requested.");
			this.log.debug("Product Node in product Validator aka product:"+allProductList);
			this.log.debug("quoteParamNode  Node in product Validator:"+quoteParamNode);
			
						

			int totalRidersSelected = quoteParamNode.get(LifeQuoteConstants.RIDERS) != null ? quoteParamNode.get(LifeQuoteConstants.RIDERS).size() : 0;
			if(totalRidersSelected > 0){
				for(JsonNode product : allProductList){
					
					if(!productExist(processedProductList, product)){
						this.log.debug("Inside product validation to pick correct product as per requested.");
						this.log.debug("Product Node in product Validator:"+product);
						this.log.debug("quoteParamNode  Node in product Validator:"+quoteParamNode);
						this.log.debug("processedProductList  Node in product Validator:"+processedProductList);
						
						log.debug("call to validate product");
						
						
						
						if(validate(product, quoteParamNode, processedProductList)){
							this.log.debug("Inside product validation to pick correct product as per requested.");
							this.log.debug("Product Node in product Validator:"+product);
							this.log.debug("quoteParamNode  Node in product Validator:"+quoteParamNode);
							this.log.debug("processedProductList  Node in product Validator:"+processedProductList);
							
							
							finalProductList.add(product);
						}
					}
				}
			}else{	
					for(JsonNode product : allProductList)
					{
						log.debug("checking for product"+product);
						if(product.has("isTermCalculationRequired"))
						{
							int productPayoutId=product.get("payoutId").asInt();
							int payoutId=quoteParamNode.get("payoutId").asInt();
							int carrierId=product.get("carrierId").asInt();
							log.debug("payout:"+productPayoutId+ "  payout:"+payoutId+"  carrierId:"+carrierId);
							
							//flag added for Kotak product check
							if(productPayoutId==payoutId)
							{
								log.debug("checking for payout:"+productPayoutId+ "payout:"+payoutId+"carrierId:"+carrierId);
								int minTermLimit=product.get("minTermLimit").asInt();
								int maxTermLimit=product.get("maxTermLimit").asInt();
								int policyTerm=quoteParamNode.get("policyTerm").asInt();
								int age=quoteParamNode.get("age").asInt();
								log.debug("age:"+age+"  term:"+policyTerm );
								int isTermUpto75=policyTerm+age;
								log.debug(" age insured upto:"+isTermUpto75 );
							
								if(minTermLimit>=policyTerm||maxTermLimit==isTermUpto75)
								{
									finalProductList.add(product);
									productExist(processedProductList, product);
									log.debug("adding the product:"+product.get("productId").asInt());
									log.debug("picked product:"+product);
								}
								else
								{
									log.debug("validation not avilable for kotak product");
								}
								
							}
							else
							{
								log.debug("validation not avilable for kotak product");
							}
								
						}
						else
						{
							finalProductList.add(product);
						}	
					}
					

					
					
					
					
				}
			
	
			this.log.info("Final Product List : " + finalProductList);
		}catch(Exception e){
			this.log.error("Exception at LifeRequestProcessor : ", e);
		}
		

		return finalProductList;
	}

	public boolean productExist(ObjectNode processedProductList, JsonNode product){
		this.log.debug("Inside product exist status check to avoid duplicate products.");
		boolean productStatus = false;
		
		
		
		if(processedProductList != null && processedProductList.size() > 0){
			for(JsonNode processedProduct : processedProductList) {
				if(processedProduct.get(LifeQuoteConstants.CARRIER_ID).asInt() == product.get(LifeQuoteConstants.CARRIER_ID).asInt()
						&& processedProduct.get(LifeQuoteConstants.CARRIER_PRODUCT_ID).asInt() == product.get(LifeQuoteConstants.CARRIER_PRODUCT_ID).asInt()){
					productStatus = true;
					break;
				}
			}
		}
		
		if(productStatus){
			this.log.info("Product already selected for carrier : " + product.get(LifeQuoteConstants.CARRIER_ID) + " : " + product.get(LifeQuoteConstants.CARRIER_PRODUCT_ID) + " : " + product.get(LifeQuoteConstants.PRODUCT_ID));			
		}
		this.log.debug("Product exist status check completed.");
		return productStatus;
	}

	public boolean validate(JsonNode product, JsonNode quoteParamNode,ObjectNode processedProductList){
		this.log.debug("Inside product validation to pick correct product as per requested.");
		this.log.debug("Product Node in product Validator:"+product);
		this.log.debug("quoteParamNode  Node in product Validator:"+quoteParamNode);
		this.log.debug("processedProductList  Node in product Validator:"+processedProductList);
		
		
		ObjectNode implicitProdRiderList = objectMapper.createObjectNode();
		List<Integer> tempUIRiderList = new ArrayList<>();
		
		boolean productStatus = false;
		boolean groupRiderStatus = true;
	
		
		int implicitUIRiderCount = 0;
		
		JsonNode selectedUIRiders = quoteParamNode.get(LifeQuoteConstants.RIDERS);
		JsonNode selectedProductRiders = product.get(LifeQuoteConstants.RIDERS);

		for(JsonNode prodRider : selectedProductRiders){
			if(prodRider.get(LifeQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("I")){
				implicitProdRiderList.put(prodRider.get(LifeQuoteConstants.RIDER_ID).asText(), prodRider);
			}
		}

		for(JsonNode uiRider : selectedUIRiders){
			if(implicitProdRiderList.has(uiRider.get(LifeQuoteConstants.RIDER_ID).asText())){
				implicitUIRiderCount++;
			}
			tempUIRiderList.add(uiRider.get(LifeQuoteConstants.RIDER_ID).asInt());
		}

		for(JsonNode uiRider : selectedUIRiders){
			for(JsonNode prodRider : selectedProductRiders){
				if(uiRider.get(LifeQuoteConstants.RIDER_ID).asInt() == prodRider.get(LifeQuoteConstants.RIDER_ID).asInt()){
					if(prodRider.get(LifeQuoteConstants.RIDER_TYPE).asText().equalsIgnoreCase("G") && 
							!tempUIRiderList.contains(prodRider.get(LifeQuoteConstants.RIDER_GROUP_ID).asInt())){
						groupRiderStatus = false;
						break;
					}
				}
			}
			if(!groupRiderStatus)
				break;
		}

		if(groupRiderStatus){
			if(implicitProdRiderList.size() > 0 && implicitUIRiderCount == 0){
				productStatus = false;
			}else{
				for(JsonNode uiRider : selectedUIRiders){
					boolean riderPresent = false;
					for(JsonNode prodRider : selectedProductRiders){
						if(uiRider.get(LifeQuoteConstants.RIDER_ID).asInt() == prodRider.get(LifeQuoteConstants.RIDER_ID).asInt()){
							riderPresent = true;
							break;
						}
					}
					if(!riderPresent){
						productStatus = false;
						break;
					}else{
						productStatus = true;
					}
				}
			}
		}
		
		if(productStatus){
			this.log.info("Validated Product for carrier : " + product.get(LifeQuoteConstants.CARRIER_ID) + " : " + product.get(LifeQuoteConstants.CARRIER_PRODUCT_ID) + " : " + product.get(LifeQuoteConstants.PRODUCT_ID));
			ObjectNode processedProductListBackup = objectMapper.createObjectNode();
			processedProductListBackup = processedProductList;
			log.debug("processedProductListBackup value :"+processedProductListBackup);
			//checking if the node is already present. If present put method will return its value.We are capturing it, and putting it with different name
			JsonNode returnedNode = processedProductList.put(product.get(LifeQuoteConstants.CARRIER_ID).asText()+"-"+product.get(LifeQuoteConstants.PRODUCT_ID).asText(), product);

			if(returnedNode != null) {
				processedProductList.put(returnedNode.get(LifeQuoteConstants.CARRIER_ID).asText()+"-new",processedProductListBackup.get(returnedNode.get(LifeQuoteConstants.CARRIER_ID).asText()));
				log.info("new Modified processedProductList value :"+processedProductList);
			}
		}else{
			this.log.debug("Invalidated Product for carrier : " + product.get(LifeQuoteConstants.CARRIER_ID) + " : " + product.get(LifeQuoteConstants.CARRIER_PRODUCT_ID) + " : " + product.get(LifeQuoteConstants.PRODUCT_ID));
		}
		this.log.info("Product validation process completed.");
		return productStatus;
	}
}