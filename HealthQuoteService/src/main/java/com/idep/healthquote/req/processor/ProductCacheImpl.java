package com.idep.healthquote.req.processor;

import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ProductCacheImpl {
	
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService productService = CBInstanceProvider.getProductConfigInstance();
	static Logger log = Logger.getLogger(ProductCacheImpl.class);
	static ObjectNode products = null;
	
	public static ObjectNode getProductsList()
	{
		try
		{
			log.info("ProductCacheImpl initiated");
			products = objectMapper.createObjectNode();
			JsonArray paramObj = JsonArray.create();
			paramObj.add("Y");
			List<JsonObject> productList = productService.executeConfigParamArrQuery(HealthQuoteConstants.ALL_PRODUCTS_PLANS_QUERY, 
					   																 paramObj);
			JsonNode productListNode = objectMapper.readTree(productList.toString());
			log.info("products fetched productListNode : "+productListNode);
			ArrayNode preExistingDisProducts = objectMapper.createArrayNode();
			ArrayNode ridersAppProducts = objectMapper.createArrayNode();
			ArrayNode noridersProducts = objectMapper.createArrayNode();
			ArrayNode ridersProducts = objectMapper.createArrayNode();
			
			for(JsonNode product : productListNode)
			{
				JsonNode plansNode = product.get("plans");
				log.info("plansNode: "+plansNode);
				if(plansNode!=null)
				{
					for(JsonNode plan:plansNode)
					{
						if(plan.get("isPlanActive").textValue().equals("Y"))
						{
							ObjectNode productNode = objectMapper.createObjectNode();
							productNode.put("carrierName", product.get("carrierName").textValue());
							productNode.put("insurerIndex", product.get("insurerIndex").doubleValue());
							productNode.put("carrierId", product.get("carrierId").intValue());
							productNode.put("planId", product.get("planId").intValue());
							productNode.put("planType", product.get("planType").textValue());
							productNode.put("Features", product.get("Features"));
							if(plan.has("benefitIndex")){
								productNode.put("benefitIndex", plan.get("benefitIndex").asDouble());
							}
							if(product.has("claimRatio")){
								productNode.put("claimRatio", product.get("claimRatio").asText());
								}
							if(product.has("claimIndex")){
								productNode.put("claimIndex", product.get("claimIndex").doubleValue());
								}
							productNode.putAll((ObjectNode)plan);
							if(plan.has("preExistingDiseaseApp") || plan.has("riderApplicable"))
							{
								if(plan.get("preExistingDiseaseApp").textValue().equals("Y") && plan.get("riderApplicable").textValue().equals("Y")){
									ridersProducts.add(productNode);
									ridersAppProducts.add(productNode);
									preExistingDisProducts.add(productNode);
								}
								else if(plan.get("preExistingDiseaseApp").textValue().equals("Y"))
								{
									preExistingDisProducts.add(productNode);
									ridersProducts.add(productNode);
								}
							
								else if(plan.get("riderApplicable").textValue().equals("Y"))
								{
									ridersAppProducts.add(productNode);
									ridersProducts.add(productNode);
									preExistingDisProducts.add(productNode);
								}
								else if(plan.get("riderApplicable").textValue().equals("N"))
								{
									noridersProducts.add(productNode);
									ridersProducts.add(productNode);
									preExistingDisProducts.add(productNode);
								}	
							}
							else
							{
								log.info("preExistingDiseaseApp or riderApplicable is not available in "+product.get("carrierId").intValue()+" "+product.get("planId").intValue());
							}
							
						}
					}
				}
				
			}
			//System.out.println("noridersProducts size : "+noridersProducts.size());
			//System.out.println("ridersAppProducts size : "+ridersAppProducts.size());
			//System.out.println("ridersProducts size : "+ridersProducts.size());
			//System.out.println("preExistingDisProducts size : "+preExistingDisProducts.size());
			products.put("NoRiders", noridersProducts);
			products.put("Riders", ridersAppProducts);
			products.put("AllProducts", ridersProducts); // excluding pre-existing diseases
			products.put("preExistDiseaseProducts", preExistingDisProducts);
			log.info("ProductCacheImpl completed");
			
						
		}
		catch(Exception e)
		{
			log.error("Exception while caching products : ",e);
		}
		
		return products;
	}

}
