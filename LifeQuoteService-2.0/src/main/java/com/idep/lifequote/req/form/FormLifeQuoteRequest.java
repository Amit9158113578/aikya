package com.idep.lifequote.req.form;

import java.util.List;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class FormLifeQuoteRequest {
	Logger log = Logger.getLogger(FormLifeQuoteRequest.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	JsonNode lifePremiumRateConfig = null;
	ArrayNode premiumListArrayNode = objectMapper.createArrayNode();
	int productId = 0;
	int carrierId = 0;

	public String formRequest(JsonNode quoteParamNode, JsonNode product, CBService serverConfig, CBService productService){
		this.log.info("Inside FormLifeQuoteRequest");
		String quoteRequestString = "";
		try{

			this.productId = product.get(LifeQuoteConstants.DROOLS_PRODUCT_ID).intValue();
			this.carrierId = product.get(LifeQuoteConstants.DROOLS_CARRIERID).intValue();
			String lifeQuoteConfigDocId = LifeQuoteConstants.LIFE_QUOTE_CONFIG + "-" + this.carrierId + "-" + this.productId;
			this.lifePremiumRateConfig = this.objectMapper.readTree(serverConfig.getDocBYId(lifeQuoteConfigDocId).content().toString());
			log.info("lifePremiumRateConfig"+lifePremiumRateConfig);
			// Drool quote request updated by default input parameters.
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_CARRIER, product.get(LifeQuoteConstants.DROOLS_CARRIER_NAME).toString());
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_CARRIERID, this.carrierId);
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_PRODUCT_ID, this.productId);
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_PRODUCT_NAME, product.get(LifeQuoteConstants.DROOLS_PRODUCT_NAME).asText());
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_INSURER_INDEX, product.get(LifeQuoteConstants.DROOLS_INSURER_INDEX).doubleValue());
			
			//Check if product support Incremental sumInsured approach or not
			if(lifePremiumRateConfig.has("productAllowIncrementalApproach"))
			{
				//Read max SI value that product support
				int incrementalValue = 0;
				if(lifePremiumRateConfig.has("incrementalValue")){
					incrementalValue = lifePremiumRateConfig.get("incrementalValue").intValue();	
				}
				
				int inputSelectedSA = quoteParamNode.get(LifeQuoteConstants.DROOLS_SUM_INSURED).intValue();
					/*
					 * Priyanka - This condition is added for HDFC implementation.
					 * Approach : Median Premium calc approach where SA is not absolute.
					 * */
					if(lifePremiumRateConfig.has("SABaseIncrement"))
					{
						JsonNode saList = product.get(LifeQuoteConstants.PRODUCT_SA_LIST);
						boolean isNotAbsSA = false;
						int minSA  = 0;
						int maxSA  = 0;
						for(int counter=0; counter < (saList.size()-1); counter++ )
						{
							minSA = saList.get(counter).asInt();
							maxSA = saList.get(counter+1).asInt();
							if(minSA == inputSelectedSA || maxSA == inputSelectedSA)
							{
								getProductPremiumRateListDetails(quoteParamNode, product, productService,inputSelectedSA,incrementalValue);
								break;
							}
							else if(inputSelectedSA > minSA && inputSelectedSA < maxSA)
							{
								getProductPremiumRateListDetails(quoteParamNode, product, productService,minSA,incrementalValue);
								getProductPremiumRateListDetails(quoteParamNode, product, productService,maxSA,incrementalValue);
								isNotAbsSA = true;
								break;
							}
						}
						// Fetching selected carrier wise rider details from DB.
						getRiderDetails(quoteParamNode, product, productService);
						log.info("HDFC riders processed");
						if(isNotAbsSA)
						{
							JsonNode premiumList = quoteParamNode.get("premiumRateList"); 
							Double minPremiumRate = premiumList.get(0).get("premiumRate").asDouble();
							Double maxPremiumRate = premiumList.get(1).get("premiumRate").asDouble();
							((ObjectNode)quoteParamNode).put("minSumInsured", minSA);
							((ObjectNode)quoteParamNode).put("maxSumInsured", maxSA);
							((ObjectNode)quoteParamNode).put("minPremiumRate", minPremiumRate);
							((ObjectNode)quoteParamNode).put("maxPremiumRate", maxPremiumRate);
							((ObjectNode)quoteParamNode).remove("premiumRateList");
						}
						quoteRequestString = quoteRequestString + quoteParamNode.toString() + ",";
						quoteRequestString = quoteRequestString.substring(0, quoteRequestString.length() - 1);
						quoteRequestString = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART1 + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART2;
						String quoteEngineRequest = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART3  + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART4 ;
						this.log.info("Generated request in getProductPremiumRateListDetails if condition : " + quoteEngineRequest);
						return quoteEngineRequest;
					}
					else if(inputSelectedSA > incrementalValue){
							
							log.info("Inside getProductPremiumRateListDetails");
							int SumAssured1 = incrementalValue;
							log.info("SumAssured1 value in getProductPremiumRateListDetails: "+SumAssured1);						
							int SumAssured2 = 100000000;
							
							getProductPremiumRateListDetails(quoteParamNode, product, productService,SumAssured1,incrementalValue);
							getProductPremiumRateListDetails(quoteParamNode, product, productService,SumAssured2,incrementalValue); 
							
							// Fetching selected carrier wise rider details from DB.
							getRiderDetails(quoteParamNode, product, productService);
							log.info("HDFC riders processed");
							
							quoteRequestString = quoteRequestString + quoteParamNode.toString() + ",";
							quoteRequestString = quoteRequestString.substring(0, quoteRequestString.length() - 1);
							quoteRequestString = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART1 + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART2;
							String quoteEngineRequest = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART3  + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART4 ;
							this.log.info("Generated request in getProductPremiumRateListDetails if condition : " + quoteEngineRequest);
							return quoteEngineRequest;
						}
						else
						{
							getProductPremiumRateListDetails(quoteParamNode, product, productService,inputSelectedSA,incrementalValue); 
							
							// Fetching selected carrier wise rider details from DB.
							getRiderDetails(quoteParamNode, product, productService);
							log.info("HDFC riders processed");							
							
							quoteRequestString = quoteRequestString + quoteParamNode.toString() + ",";
							quoteRequestString = quoteRequestString.substring(0, quoteRequestString.length() - 1);
							quoteRequestString = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART1 + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART2;
							String quoteEngineRequest = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART3  + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART4 ;
							this.log.info("Generated request in getProductPremiumRateListDetails else condition : " + quoteEngineRequest);
							return quoteEngineRequest;
						}
				
			}
			else
			{
				int inputSelectedSA = quoteParamNode.get(LifeQuoteConstants.DROOLS_SUM_INSURED).intValue();
				int incrementalValue = 0;
				// Calculating Sum Assured range from selected input request.
				findSumAssuredRange(quoteParamNode, product, productService);
				log.info("sum insured range calculated");

				// Validate selected policy term.
				calculatePolicyTerm(quoteParamNode, product, productService);
				log.info("policy term calculated");
				
				// Fetching selected carrier wise premium rate details from DB.
				//getProductPremiumRateDetails(quoteParamNode, product, productService);
				getProductPremiumRateListDetails(quoteParamNode, product, productService,inputSelectedSA,incrementalValue);
				
				log.info("product premium calculated");
				
				// Fetching selected carrier wise higher premium rate details from DB.
				getProductHigherPremiumRateDetails(quoteParamNode, product, productService);
				log.info("higher premium rate calculated");
				
				// Fetching selected carrier wise lower premium rate details from DB.
				getProductLowerPremiumRateDetails(quoteParamNode, product, productService);
				log.info("lower premium rate calculated");
				
				// Fetching selected carrier wise rider details from DB.
				getRiderDetails(quoteParamNode, product, productService);
				log.info("riders processed");

				quoteRequestString = quoteRequestString + quoteParamNode.toString() + ",";
				quoteRequestString = quoteRequestString.substring(0, quoteRequestString.length() - 1);
				quoteRequestString = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART1 + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART2;
				String quoteEngineRequest = LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART3  + quoteRequestString + LifeQuoteConstants.DROOLS_LIFEQUOTE_REQUEST_PART4 ;
				this.log.debug("Generated request in FormLifeQuoteRequest : " + quoteEngineRequest);
				return quoteEngineRequest;
			}
			
			
		}catch(Exception e){
			this.log.error("Exception at FormLifeQuoteRequest for Life : ", e);
			return quoteRequestString;
		}
	}

	public void findSumAssuredRange(JsonNode quoteParamNode, JsonNode product, CBService productService) throws Exception{
		log.info("quoteParamNode"+quoteParamNode);
		log.info("product"+product);
		log.info("productService"+productService);
		
		int minSumAssured = product.get(LifeQuoteConstants.MIN_ALLOWED_SUM_INSURED).intValue();
		int maxSumAssured = product.get(LifeQuoteConstants.MAX_ALLOWED_SUM_INSURED).intValue();
		int selSumAssured = quoteParamNode.get(LifeQuoteConstants.DROOLS_SUM_INSURED).intValue();
		log.info("minSumAssured"+minSumAssured);
		log.info("maxSumAssured"+maxSumAssured);
		log.info("selSumAssured"+selSumAssured);
		

		int lowerSumAssured = 0;
		int higherSumAssured = 0;
		ArrayNode sumAssuredList = (ArrayNode) product.get("sumAssuredList");

		log.info("sumAssuredList"+sumAssuredList);
		if(selSumAssured <= sumAssuredList.get(0).intValue()){
			selSumAssured = lowerSumAssured = higherSumAssured = sumAssuredList.get(0).intValue();
		}else{
			for(int i = 1; i < sumAssuredList.size(); i++){
				if(selSumAssured == sumAssuredList.get(i).intValue()){
					lowerSumAssured = higherSumAssured = 0;
					break;
				}else if(selSumAssured < sumAssuredList.get(i).intValue()){
					lowerSumAssured = sumAssuredList.get(i-1).intValue();
					higherSumAssured = sumAssuredList.get(i).intValue();
					break;
				}else{
					if(i == (sumAssuredList.size() - 1)){
						selSumAssured = lowerSumAssured = higherSumAssured = sumAssuredList.get(i).intValue();
						break;
					}
				}
			}
		}

		this.log.info("Lower Sum Assured : " + lowerSumAssured);
		this.log.info("Higher Sum Assured : " + higherSumAssured);
		this.log.info("Selected Sum Assured : " + selSumAssured);

		((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_HIGHER_SUM_ASSURED, higherSumAssured);
		((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_LOWER_SUM_ASSURED, lowerSumAssured);
		((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_SUM_INSURED, selSumAssured);

		if(selSumAssured < minSumAssured){
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_SUM_INSURED, minSumAssured);
		}else if (selSumAssured > maxSumAssured){
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.DROOLS_SUM_INSURED, maxSumAssured);
		}
	}

	public void calculatePolicyTerm(JsonNode quoteParamNode, JsonNode product, CBService productService)throws Exception{
		int policyMaxTerm = product.get("MaxTerm").intValue();
		int termMultiple = product.get("termMultiple").intValue();
		int policyTerm = quoteParamNode.get(LifeQuoteConstants.POLICY_TERM).intValue();
		int maxMaturityAge = product.get("maxMaturityAge").intValue();

		if (policyTerm > policyMaxTerm){
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, policyMaxTerm);
		}else{
			int diff = policyTerm % termMultiple;

			if(diff >= termMultiple * 0.5D){
				int count = termMultiple - diff;
				policyTerm += count;
				if(policyTerm > maxMaturityAge){
					((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, maxMaturityAge);
				}else{
					((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, policyTerm);
				}
			}else{
				policyTerm -= diff;
				((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, policyTerm);
			}
		}
	}

	public void getProductPremiumRateDetails(JsonNode quoteParamNode, JsonNode product, CBService productService){
		this.log.info("Inside getProductPremiumRateDetails.");

		JsonNode productPremiumQueryDetails = this.lifePremiumRateConfig.get(LifeQuoteConstants.PREMIUM_CONFIG).get(LifeQuoteConstants.QUERY_DETAILS);
		ArrayNode productPremiumQueryWhereParam = (ArrayNode)productPremiumQueryDetails.get(LifeQuoteConstants.PARAM_ABS_SA).get(LifeQuoteConstants.PARAM);
		String productPremiumQuery = productPremiumQueryDetails.get(LifeQuoteConstants.QUERY).asText();

		/* prepare an array for parameterized query */
		JsonArray paramArray = JsonArray.create();
		paramArray.add(this.carrierId);
		paramArray.add(this.productId);

		for(int i = 0; i < productPremiumQueryWhereParam.size(); i++){
			JsonNode defaultValue = productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE);
			if(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
				if(defaultValue.isTextual()){
					paramArray.add(defaultValue.textValue());
				}else{
					paramArray.add(defaultValue.intValue());
				}
			}else{
				JsonNode actualValue = quoteParamNode.get(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_NAME).textValue());
				if(actualValue == null){
					if(defaultValue.isTextual()){
						paramArray.add(defaultValue.textValue());
					}else{
						paramArray.add(defaultValue.intValue());
					}
				}else{
					if(actualValue.isTextual()){
						paramArray.add(actualValue.textValue());
					}else if(actualValue.isInt()){
						paramArray.add(actualValue.intValue());
					}else if(actualValue.isDouble()){
						paramArray.add(actualValue.doubleValue());
					}else if(actualValue.isBoolean()){
						paramArray.add(actualValue.booleanValue());
					}else if(actualValue.isLong()){
						paramArray.add(actualValue.longValue());
					}
				}
			}
		}

		this.log.info("productPremiumQuery : " + productPremiumQuery);
		this.log.info("productPremiumQueryWhereParam values : " + paramArray);

		// find product level premium rate
		List<JsonObject> prodPremiumRates = productService.executeConfigParamArrQuery(productPremiumQuery.trim(), paramArray);
		this.log.info("Inside getProductPremiumRateDetails query result : " + prodPremiumRates);

		if(prodPremiumRates.size() > 0){
			this.log.info("Product premium rate found in DB");
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.PREMIUM_RATE, prodPremiumRates.get(0).getDouble(LifeQuoteConstants.PREMIUM_RATE));
		}else{
			this.log.info("Product premium rate list is empty");
		}
	}

	public void getProductHigherPremiumRateDetails(JsonNode quoteParamNode, JsonNode product, CBService productService)throws Exception {
		this.log.info("Inside getProductHigherPremiumRateDetails.");

		JsonNode productPremiumQueryDetails = this.lifePremiumRateConfig.get(LifeQuoteConstants.PREMIUM_CONFIG).get(LifeQuoteConstants.QUERY_DETAILS);
		ArrayNode productPremiumQueryWhereParam = (ArrayNode)productPremiumQueryDetails.get(LifeQuoteConstants.PARAM_MAX_SA).get(LifeQuoteConstants.PARAM);
		String productPremiumQuery = productPremiumQueryDetails.get(LifeQuoteConstants.QUERY).asText();

		/* prepare an array for parameterized query */
		JsonArray paramArrayHigher = JsonArray.create();
		paramArrayHigher.add(this.carrierId);
		paramArrayHigher.add(this.productId);

		for(int i = 0; i < productPremiumQueryWhereParam.size(); i++){
			JsonNode defaultValue = productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE);
			if(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
				if(defaultValue.isTextual()){
					paramArrayHigher.add(defaultValue.textValue());
				}else{
					paramArrayHigher.add(defaultValue.intValue());
				}
			}else{
				JsonNode actualValue = quoteParamNode.get(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_NAME).textValue());
				if(actualValue == null){
					if(defaultValue.isTextual()){
						paramArrayHigher.add(defaultValue.textValue());
					}else{
						paramArrayHigher.add(defaultValue.intValue());
					}
				}else{
					if(actualValue.isTextual()){
						paramArrayHigher.add(actualValue.textValue());
					}else if(actualValue.isInt()){
						paramArrayHigher.add(actualValue.intValue());
					}else if(actualValue.isDouble()){
						paramArrayHigher.add(actualValue.doubleValue());
					}else if(actualValue.isBoolean()){
						paramArrayHigher.add(actualValue.booleanValue());
					}
				}
			}
		}

		this.log.info("productHigherPremiumQuery : " + productPremiumQuery);
		this.log.info("productHigherPremiumQueryWhereParam values : " + paramArrayHigher);

		// find product level premium rate
		List<JsonObject> prodPremiumRatesHigher = productService.executeConfigParamArrQuery(productPremiumQuery, paramArrayHigher);
		this.log.info("Inside getProductHigherPremiumRateDetails query result : " + prodPremiumRatesHigher);

		if(prodPremiumRatesHigher.size() > 0){
			this.log.info("Product higher premium rate found in DB");
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.HIGHER_PREMIUM_RATE, prodPremiumRatesHigher.get(0).getDouble(LifeQuoteConstants.PREMIUM_RATE));
		}else{
			this.log.info("Product higher premium rate list is empty");
		}
	}

	public void getProductLowerPremiumRateDetails(JsonNode quoteParamNode, JsonNode product, CBService productService)throws Exception {
		this.log.info("Inside getProductLowerPremiumRateDetails.");

		JsonNode productPremiumQueryDetails = this.lifePremiumRateConfig.get(LifeQuoteConstants.PREMIUM_CONFIG).get(LifeQuoteConstants.QUERY_DETAILS);
		ArrayNode productPremiumQueryWhereParam = (ArrayNode)productPremiumQueryDetails.get(LifeQuoteConstants.PARAM_MIN_SA).get(LifeQuoteConstants.PARAM);
		String productPremiumQuery = productPremiumQueryDetails.get(LifeQuoteConstants.QUERY).asText();

		/* prepare an array for parameterized query */
		JsonArray paramArrayLower = JsonArray.create();
		paramArrayLower.add(this.carrierId);
		paramArrayLower.add(this.productId);

		for(int i = 0; i < productPremiumQueryWhereParam.size(); i++){
			JsonNode defaultValue = productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE);
			if(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
				if(defaultValue.isTextual()){
					paramArrayLower.add(defaultValue.textValue());
				}else{
					paramArrayLower.add(defaultValue.intValue());
				}
			}else{
				JsonNode actualValue = quoteParamNode.get(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_NAME).textValue());
				if(actualValue == null){
					if(defaultValue.isTextual()){
						paramArrayLower.add(defaultValue.textValue());
					}else{
						paramArrayLower.add(defaultValue.intValue());
					}
				}else{
					if(actualValue.isTextual()){
						paramArrayLower.add(actualValue.textValue());
					}else if(actualValue.isInt()){
						paramArrayLower.add(actualValue.intValue());
					}else if(actualValue.isDouble()){
						paramArrayLower.add(actualValue.doubleValue());
					}else if(actualValue.isBoolean()){
						paramArrayLower.add(actualValue.booleanValue());
					}
				}
			}
		}

		this.log.info("productLowerPremiumQuery : " + productPremiumQuery);
		this.log.info("productLowerPremiumQueryWhereParam values : " + paramArrayLower);

		// find product level premium rate
		List<JsonObject> prodPremiumRatesLower = productService.executeConfigParamArrQuery(productPremiumQuery, paramArrayLower);
		this.log.info("Inside getProductLowerPremiumRateDetails query result : " + prodPremiumRatesLower);

		if(prodPremiumRatesLower.size() > 0){
			this.log.info("Product lower premium rate found in DB");
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.LOWER_PREMIUM_RATE, prodPremiumRatesLower.get(0).getDouble(LifeQuoteConstants.PREMIUM_RATE));
		}else{
			this.log.info("Product lower premium rate list is empty");
		}
	}

	public void getProductPremiumRateListDetails(JsonNode quoteParamNode, JsonNode product, CBService productService, int SumAssuredValue,int incrementalValue){
		this.log.info("Inside getProductPremiumRateListDetails.");

		JsonNode productPremiumQueryDetails = this.lifePremiumRateConfig.get(LifeQuoteConstants.PREMIUM_CONFIG).get(LifeQuoteConstants.QUERY_DETAILS);
		ArrayNode productPremiumQueryWhereParam = (ArrayNode)productPremiumQueryDetails.get(LifeQuoteConstants.PARAM_ABS_SA).get(LifeQuoteConstants.PARAM);
		String productPremiumQuery = productPremiumQueryDetails.get(LifeQuoteConstants.QUERY).asText();

		ObjectNode premiumDetailsNode = objectMapper.createObjectNode();
		
		/* prepare an array for parameterized query */
		JsonArray paramArray = JsonArray.create();
		paramArray.add(this.carrierId);
		paramArray.add(this.productId);

		for(int i = 0; i < productPremiumQueryWhereParam.size(); i++){
			JsonNode defaultValue = productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE);
			if(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
				if(defaultValue.isTextual()){
					paramArray.add(defaultValue.textValue());
				}else{
					paramArray.add(defaultValue.intValue());
				}
			}else{
				JsonNode actualValue = quoteParamNode.get(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_NAME).textValue());
				if(actualValue == null){
					if(defaultValue.isTextual()){
						paramArray.add(defaultValue.textValue());
					}else{
						paramArray.add(defaultValue.intValue());
					}
				}else{
					if(actualValue.isTextual()){
						paramArray.add(actualValue.textValue());
					}else if(actualValue.isInt()){
						paramArray.add(actualValue.intValue());
					}else if(actualValue.isDouble()){
						paramArray.add(actualValue.doubleValue());
					}else if(actualValue.isBoolean()){
						paramArray.add(actualValue.booleanValue());
					}
				}
			}
		}
		paramArray.add(SumAssuredValue);

		this.log.info("productPremiumQuery getProductPremiumRateListDetails: " + productPremiumQuery);
		this.log.info("productPremiumQueryWhereParam values getProductPremiumRateListDetails: " + paramArray);

		// find product level premium rate
		List<JsonObject> prodPremiumRates = productService.executeConfigParamArrQuery(productPremiumQuery.trim(), paramArray);
		this.log.info("Inside getProductPremiumRateDetails query result getProductPremiumRateListDetails : " + prodPremiumRates);
		
		int inputSelectedSA = quoteParamNode.get(LifeQuoteConstants.DROOLS_SUM_INSURED).intValue();
		
		if(prodPremiumRates.size() > 0){
			this.log.info("Product premium rate found in DB");
			if(SumAssuredValue > inputSelectedSA)
			{
				int diffSumInsured= inputSelectedSA - incrementalValue;
				((ObjectNode)premiumDetailsNode).put("sumAssured", diffSumInsured);
				((ObjectNode)premiumDetailsNode).put("premiumRate", prodPremiumRates.get(0).getDouble(LifeQuoteConstants.PREMIUM_RATE));
			}
			else
			{
				((ObjectNode)premiumDetailsNode).put("sumAssured", SumAssuredValue);
				((ObjectNode)premiumDetailsNode).put("premiumRate", prodPremiumRates.get(0).getDouble(LifeQuoteConstants.PREMIUM_RATE));
				
			}
		}else{
			this.log.info("Product premium rate list is empty");
		}
		premiumListArrayNode.add(premiumDetailsNode);	
		log.info("premiumDetailsNode Info"+premiumDetailsNode);
		log.info("premiumListArrayNode Info:"+premiumListArrayNode);
		
		((ObjectNode)quoteParamNode).put(LifeQuoteConstants.PREMIUM_RATE_LIST, premiumListArrayNode);
		
	}

	
	
	
	public void getRiderDetails(JsonNode quoteParamNode, JsonNode product, CBService productService) throws Exception {
		this.log.info("Inside getRiderDetails.");
		log.info("quoteParamNode in RiderDetails: "+quoteParamNode);
		
		log.info("product in RiderDetails: "+product);
		
		log.info("productService in RiderDetails: "+productService);

		JsonNode productPremiumQueryDetails = this.lifePremiumRateConfig.get(LifeQuoteConstants.RIDER_CONFIG).get(LifeQuoteConstants.QUERY_DETAILS);
		String productPremiumQuery = productPremiumQueryDetails.get(LifeQuoteConstants.QUERY).asText();
		long selectedSA = quoteParamNode.get(LifeQuoteConstants.DROOLS_SUM_INSURED).intValue();

		if(quoteParamNode.has(LifeQuoteConstants.RIDERS)){
			ArrayNode riderArray = objectMapper.createArrayNode();
			JsonNode riderNode = quoteParamNode.get(LifeQuoteConstants.RIDERS);
			for(JsonNode riders : riderNode){
				// add parameters for parameterized query to find rider premium
				JsonArray riderPremArray = JsonArray.create();
				riderPremArray.add(this.carrierId);
				riderPremArray.add(this.productId);
				riderPremArray.add(riders.get(LifeQuoteConstants.RIDER_ID).intValue());
				
				JsonNode riderParamDetails = productPremiumQueryDetails.get(riders.get(LifeQuoteConstants.RIDER_ID).asText());
				if(riderParamDetails != null){
					ArrayNode productPremiumQueryWhereParam = (ArrayNode) riderParamDetails.get(LifeQuoteConstants.PARAM);
					for(int i = 0; i < productPremiumQueryWhereParam.size(); i++){
						JsonNode defaultValue = productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE);
						if(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
							if(defaultValue.isTextual()){
								riderPremArray.add(defaultValue.textValue());
							}else{
								riderPremArray.add(defaultValue.intValue());
							}
						}else{
							JsonNode actualValue = quoteParamNode.get(productPremiumQueryWhereParam.get(i).get(LifeQuoteConstants.PARAM_NAME).textValue());
							if(actualValue == null){
								if(defaultValue.isTextual()){
									riderPremArray.add(defaultValue.textValue());
								}else{
									riderPremArray.add(defaultValue.intValue());
								}
							}else{
								if(actualValue.isTextual()){
									riderPremArray.add(actualValue.textValue());
								}else if(actualValue.isInt()){
									riderPremArray.add(actualValue.intValue());
								}else if(actualValue.isDouble()){
									riderPremArray.add(actualValue.doubleValue());
								}else if(actualValue.isBoolean()){
									riderPremArray.add(actualValue.booleanValue());
								}
							}
						}
					}

					int policyTerm = quoteParamNode.get(LifeQuoteConstants.POLICY_TERM).intValue();
					JsonNode riderPolicyTermDetails = productPremiumQueryDetails.get(riders.get(LifeQuoteConstants.RIDER_ID).asText()).get(LifeQuoteConstants.PARAM_POLICY_TERM);
					if(riderPolicyTermDetails.get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
						riderPremArray.add(riderPolicyTermDetails.get(LifeQuoteConstants.PARAM_DEFAULT_VALUE).asInt());
					}else{
						if(policyTerm > riderPolicyTermDetails.get(LifeQuoteConstants.PARAM_MAX_TERM).asInt()){
							riderPremArray.add(riderPolicyTermDetails.get(LifeQuoteConstants.PARAM_MAX_TERM).asInt());
						}else{
							riderPremArray.add(policyTerm);
						}
					}

					JsonNode riderSADetails = productPremiumQueryDetails.get(riders.get(LifeQuoteConstants.RIDER_ID).asText()).get(LifeQuoteConstants.PARAM_SA);
					if(riderSADetails.get(LifeQuoteConstants.PARAM_DEFAULT_VALUE_STATUS).asBoolean()){
						riderPremArray.add(riderSADetails.get(LifeQuoteConstants.PARAM_DEFAULT_VALUE).asLong());
						riderPremArray.add(riderSADetails.get(LifeQuoteConstants.PARAM_DEFAULT_VALUE).asLong());
					}else{
						long minSA = -1;
						long maxSA = -1;

						ArrayNode riderSAList = (ArrayNode) riderSADetails.get(LifeQuoteConstants.PARAM_LIST);
						for(int i = 0; i < riderSAList.size(); i++){
							if(riderSAList.get(i).asLong() == selectedSA){
								minSA = maxSA = selectedSA;
								break;
							}else if(riderSAList.get(i).asLong() < selectedSA){
								minSA = riderSAList.get(i).asLong();
							}else{
								maxSA = riderSAList.get(i).asLong();
								break;
							}
						}
						riderPremArray.add(minSA);
						riderPremArray.add(maxSA);
					}

					this.log.info("getRiderDetailsQuery : " + productPremiumQuery);
					this.log.info("getRiderDetailsQueryWhereParam values : " + riderPremArray);

					List<JsonObject> riderDetails = productService.executeConfigParamArrQuery(productPremiumQuery, riderPremArray);
					this.log.info("Inside getRiderDetails for rider id (" + riders.get(LifeQuoteConstants.RIDER_ID).intValue() + ") for carrier id (" + this.carrierId + ") query result : " + riderDetails);

					if(riderDetails.size() > 0){
						this.log.info("Product rider premium details found in DB");
						// attach premium rate to rider array node
						((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_ALLOW_SA, riderDetails.get(0).getLong(LifeQuoteConstants.RIDER_MAX_ALLOW_SA));
						((ObjectNode)riders).put(LifeQuoteConstants.RIDER_SA_PERCENTAGE, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_SA_PERCENTAGE));
						((ObjectNode)riders).put(LifeQuoteConstants.RIDER_PREMIUM_DISCOUNT, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_PREMIUM_DISCOUNT));
						((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_ALLOW_PREMIUM, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_MAX_ALLOW_PREMIUM));
						((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_ALLOW_BASEPREMIUM_PERCENTAGE, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_MAX_ALLOW_BASEPREMIUM_PERCENTAGE));

						if(riderDetails.size() == 2){
							if(riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_SA) < riderDetails.get(1).getDouble(LifeQuoteConstants.RIDER_SA)){
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_ABS_SA, selectedSA);
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MIN_SA, riderDetails.get(0).getLong(LifeQuoteConstants.RIDER_SA));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_SA, riderDetails.get(1).getLong(LifeQuoteConstants.RIDER_SA));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_PREMIUM_RATE, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MIN_SA_PREMIUM, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_SA_PREMIUM, riderDetails.get(1).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
							}else{
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_ABS_SA, selectedSA);
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MIN_SA, riderDetails.get(1).getLong(LifeQuoteConstants.RIDER_SA));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_SA, riderDetails.get(0).getLong(LifeQuoteConstants.RIDER_SA));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_PREMIUM_RATE, riderDetails.get(1).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MIN_SA_PREMIUM, riderDetails.get(1).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
								((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_SA_PREMIUM, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
							}
						}else{
							((ObjectNode)riders).put(LifeQuoteConstants.RIDER_ABS_SA, riderDetails.get(0).getLong(LifeQuoteConstants.RIDER_SA));
							((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MIN_SA, -1);
							((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_SA, -1);
							((ObjectNode)riders).put(LifeQuoteConstants.RIDER_PREMIUM_RATE, riderDetails.get(0).getDouble(LifeQuoteConstants.RIDER_PREMIUM_RATE));
							((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MIN_SA_PREMIUM, -1.0);
							((ObjectNode)riders).put(LifeQuoteConstants.RIDER_MAX_SA_PREMIUM, -1.0);
						}
						riderArray.add(riders);
					}else{
						this.log.info("Product rider premium details list is empty");
					}
				}
			}

			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.RIDERS, riderArray);
		}
	}
}