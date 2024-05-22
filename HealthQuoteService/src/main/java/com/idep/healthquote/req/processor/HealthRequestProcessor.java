package com.idep.healthquote.req.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PBRating.Health.CalculateHealthRating;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.healthquote.form.req.FormHealthQuoteRequest;
import com.idep.healthquote.util.CorrelationKeyGenerator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class HealthRequestProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthRequestProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = null;
	JsonNode errorNode;
	JsonNode responseConfigNode = null;
	JsonNode healthSumCalc = null;
	JsonNode HealthCarrierReqQNode=null;
	JsonNode HealthCarrierResQNode=null;
	static ObjectNode productCacheList = null;
	JsonNode HDFCPinCodeList=null;
	ObjectNode finalresultNode = objectMapper.createObjectNode();
	CalculateHealthRating calculateHealthRating=new CalculateHealthRating();
	static{
		try {
		Logger.getLogger(HealthRequestProcessor.class.getName()).info("Health Plan Cahcing Started......");
		productCacheList = ProductCacheImpl.getProductsList();
	
			Thread.sleep(10000);
		
		Logger.getLogger(HealthRequestProcessor.class.getName()).info("Health Plan Cahcing Completed.....");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void process(Exchange exchange) {
		 
		 try
		 {
			 
	         if (this.serverConfig == null) { 
					if(productCacheList==null){
					productCacheList = ProductCacheImpl.getProductsList();	
					}
				  
				  this.serverConfig = CBInstanceProvider.getServerConfigInstance();
		          this.log.info("ServerConfig bucket instance created");
		          this.HealthCarrierReqQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.HEALTH_CARRIERS_Q).content().getObject(HealthQuoteConstants.REQ_Q_LIST).toString());
		          this.log.info("HealthCarrierReqQNode configuration Loaded");
		          this.HealthCarrierResQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.HEALTH_CARRIERS_Q).content().getObject(HealthQuoteConstants.RES_Q_LIST).toString());
		          this.log.info("HealthCarrierResQNode configuration Loaded");
		          this.responseConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
		          this.log.info("ResponseConfig messages loaded");
		          this.healthSumCalc = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.HEALTH_SUM_ASSURED_CALC).content().toString());
		          this.productService = CBInstanceProvider.getProductConfigInstance();
		          this.log.info("ProductData bucket instance created");
		        //added for HDFC Location wise validation
		          this.HDFCPinCodeList = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.HDFC_PIN_CODES).content().toString());
		          
			}
				/**
				 *  create producer template to send messages to Q
				 */
				CamelContext camelContext = exchange.getContext();
				ProducerTemplate template = camelContext.createProducerTemplate();
				
				String quotedata = exchange.getIn().getBody().toString();
			    // convert input request to JSON node  
			    JsonNode reqNode = this.objectMapper.readTree(quotedata);
			    JsonNode quoteParamNode = reqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM);
			    JsonNode personalInfoNode = reqNode.get(HealthQuoteConstants.SERVICE_PERSINFO_PARAM);
			    JsonNode ratingParamNode = reqNode.get(HealthQuoteConstants.SERVICE_HEALTH_RATING_PARAM);
			    // fetch all products rating applicable for input request
			    FormHealthQuoteRequest healthquoteRequest = new FormHealthQuoteRequest();
			    if(personalInfoNode.has("pincode")){
			    	((ObjectNode)HDFCPinCodeList).put("pinCode",personalInfoNode.get("pincode").asText());
			    	 log.info("HDFCPinCodeList after adding UI Pin Code : "+HDFCPinCodeList);
			    }
			    JsonNode productsList = null;
			    ObjectNode diseaseList = this.objectMapper.createObjectNode();
			    log.info("Heatlth product list before validations : "+productCacheList.size());
			    // if(quoteParamNode.has("preExistingDisease")) commented as getting default
			    if(quoteParamNode.get("preExistingDisease").size() > 0)
			    {
			    	productsList = productCacheList.get("preExistDiseaseProducts");
			    	// get all the disease id's
			    	for(JsonNode node : quoteParamNode.get("preExistingDisease"))
			  	   	{
			  		   diseaseList.put(node.asText(), node.asInt());
			  	   	}
			    }
			    else if(quoteParamNode.has("riders") && quoteParamNode.get("riders").size() > 0)
			    {
			    	productsList = productCacheList.get("Riders");
			    }
			    else
			    {
			    	productsList = productCacheList.get("AllProducts");
			    }
			    
			    long minsumInsured = 0;
			    long maxsumInsured = 0;
			    
			    /**
			     * check if hospitalization limit is provided in UI request
			     */
			    if(personalInfoNode.has(HealthQuoteConstants.MIN_HOSP_LIMT)&&
			       personalInfoNode.has(HealthQuoteConstants.MAX_HOSP_LIMT))
			    {
			    	minsumInsured = personalInfoNode.get(HealthQuoteConstants.MIN_HOSP_LIMT).longValue();
			    	maxsumInsured = personalInfoNode.get(HealthQuoteConstants.MAX_HOSP_LIMT).longValue();
			    }
			    else
			    {
			    	minsumInsured = healthSumCalc.get(HealthQuoteConstants.MIN_BASIC_PREMIUM).longValue() + 
					        healthSumCalc.get(HealthQuoteConstants.ADULT_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.ADULT_COUNT).longValue() + 
					        healthSumCalc.get(HealthQuoteConstants.CHILD_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.CHILD_COUNT).longValue();
					      
			     	maxsumInsured = healthSumCalc.get(HealthQuoteConstants.MAX_BASIC_PREMIUM).longValue() + 
					        healthSumCalc.get(HealthQuoteConstants.ADULT_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.ADULT_COUNT).longValue() + 
					        healthSumCalc.get(HealthQuoteConstants.CHILD_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.CHILD_COUNT).longValue();		        	 
			    }
			    
				 // add sumInsured node to input request
				 ObjectNode sumInsuredNode = objectMapper.createObjectNode();
				 String healthQuoteId = DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.DOCID_CONFIG).get("healthQuoteId").asText()+this.serverConfig.updateDBSequence("SEQHEALTHQUOTE");
				 JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
				 String encQuoteId = GenrateEncryptionKey.GetEncryptedKey(healthQuoteId, keyConfigDoc.get("encryptionKey").asText());
				 log.info("Encrypted Health QUOTE_ID :" + encQuoteId);
				 sumInsuredNode.put("minsumInsured", minsumInsured);
				 sumInsuredNode.put("maxsumInsured", maxsumInsured);
				 this.log.info("Health Produt minsumInsured : "+minsumInsured + " : maxsumInsured : " +maxsumInsured);
				 /*this.log.info("Health List of Products :: "+productsList.toString());*/
				 ArrayNode finalProductList =  this.objectMapper.createArrayNode();
				 this.log.info("Health product validation initiated");
				 for(JsonNode product : productsList)
				 {
					 ProductValidationImpl productValidator = new ProductValidationImpl();
					 this.log.debug("Validation Statred for planId : "+product.get("planId").intValue());
					 boolean validateFlag = productValidator.validateProduct(product,quoteParamNode,sumInsuredNode,diseaseList,HDFCPinCodeList);
					 if(validateFlag)
					 {
						 this.log.info("sumInsured for product : "+productValidator.sumInsured+" : planId : "+product.get("planId").intValue());
						 ((ObjectNode)product).put("sumInsured",productValidator.sumInsured);
						 finalProductList.add(product);
						 
					 }
					 else
					 {
						 log.info("validation failed for plan id : "+product.get("planId").intValue());
					 }
				 }
				 this.log.info("Health total products applicable after validawtion : "+finalProductList.size());
				 
				 if(finalProductList.size() > 0){
					 // find products ratings - not need for NEW Website changes - APRIL - 2019
					/* List<Map<String, Object>> healthProductRatingList = null;
					 try
					 {
						healthProductRatingList = healthquoteRequest.getProductRatingDetails(quoteParamNode,ratingParamNode,productService);
					 }
					 catch(Exception e)
					 {
						 log.error("failed to get product ratings : ",e);
					 }
					 
					 // store all products ratings
					  ObjectNode filteredRating = this.objectMapper.createObjectNode();
					 ObjectNode ratingDataNode = this.objectMapper.createObjectNode();
					 
					 if(healthProductRatingList!=null && healthProductRatingList.size()>0)
					 {
						 JsonNode ratingsNode = this.objectMapper.valueToTree(healthProductRatingList.get(0));
						 JsonNode defaultRiskType = ratingsNode.get(HealthQuoteConstants.DEFAULT_RISK_TYPE);
						 JsonNode prodRatingNode = ratingsNode.get(HealthQuoteConstants.PRODUCTS);
						 
						 ratingDataNode.put(HealthQuoteConstants.DEFAULT_RISK_TYPE, defaultRiskType);
						
						 for(JsonNode product:finalProductList)
					      {
								  // get ratings only for applicable products and store it in filteredRating node
								  for (JsonNode ratingNode : prodRatingNode) {
							            if ((product.get(HealthQuoteConstants.DROOLS_PLANID).intValue() == ratingNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()) && 
							            	(product.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue() == ratingNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()))
							            {
							            	filteredRating.put(product.get(HealthQuoteConstants.DROOLS_PLANID).asText(), ratingNode.get(HealthQuoteConstants.CATEGORY_MAP));
							            	break;
							            }
							      }
						  } 
					 }*/
					 /***
					  * cash less hospital rating changes for professional service
					  */
					 
					 JsonNode generateHealthRating = calculateHealthRating.generateHealthRating(reqNode.toString());
					 log.info("cash less hospital count :"+generateHealthRating);
					 // rating logic ends here
						
					 	  // create quote id
						  
	
						  ArrayNode reqQListNode = objectMapper.createArrayNode();
						  for(JsonNode product : finalProductList)
						  {
							  if(generateHealthRating.has("hosptalRating")&& generateHealthRating.get("hosptalRating").size()>0)
							  {
								  JsonNode hosptalRatingArray = generateHealthRating.get("hosptalRating");
								  for (JsonNode hosptalRating : hosptalRatingArray) {
									  if(hosptalRating.get(HealthQuoteConstants.DROOLS_CARRIERID).asInt()==product.get(HealthQuoteConstants.DROOLS_CARRIERID).asInt())
									  {
										  ((ObjectNode)product).put("hospitalIndex",hosptalRating.get("rating").asDouble());
									  }
								}
							  }
							  ((ObjectNode)reqNode).put(HealthQuoteConstants.PRODUCT_INFO, product);
							  ObjectNode objectNode = objectMapper.createObjectNode();
							  log.info("product :"+product);
							  log.info("HealthCarrierReqQNode :"+HealthCarrierReqQNode);
							  
							  String carrierReqQName = HealthCarrierReqQNode.get(product.get(HealthQuoteConstants.DROOLS_CARRIERID).asText()).textValue();
							  objectNode.put(HealthQuoteConstants.INPUT_MESSAGE, reqNode);
							  
							  String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
							  objectNode.put(HealthQuoteConstants.UNIQUE_KEY, correlationKey);
							  // attach product rating to Q message
							 // ratingDataNode.put(HealthQuoteConstants.RATINGS_LIST,filteredRating.get(product.get(HealthQuoteConstants.DROOLS_PLANID).asText()));
							 //objectNode.put(HealthQuoteConstants.HEALTH_RATINGS, ratingDataNode);
							  objectNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId );
							  objectNode.put(HealthQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
							  String uri = "activemq:queue:"+carrierReqQName;
							  exchange.getIn().setBody(objectNode.toString());
							  exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
							  
							 template.send(uri, exchange);
							//  template.asyncSendBody(uri, objectNode.toString());
							  ObjectNode resultNode = objectMapper.createObjectNode();
							  resultNode.put(HealthQuoteConstants.QNAME, HealthCarrierResQNode.get(product.get(HealthQuoteConstants.DROOLS_CARRIERID).asText()).textValue());
						      resultNode.put(HealthQuoteConstants.MESSAGE_ID, correlationKey);
						      resultNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId);
						      resultNode.put(HealthQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
							  resultNode.put(HealthQuoteConstants.DROOLS_CARRIERID, product.get(HealthQuoteConstants.DROOLS_CARRIERID).asInt());
							  resultNode.put(HealthQuoteConstants.STATUS, 0);
						      reqQListNode.add(resultNode);
						      
						      /***
								 * checking if profession quote id present in request then quote request sending to activemqSecondary:queue:pbqupdatereqQ for
								 *  update ProfessionQuoteId (latest car quote id and request.  
								 * **/
								if(reqNode.findValue("PROF_QUOTE_ID")!=null){
											log.info("Health carrier request for update PBQ REQUEST  :"+reqNode);
										((ObjectNode)reqNode).put(HealthQuoteConstants.QUOTE_ID, healthQuoteId );
										((ObjectNode)reqNode).put(HealthQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
											PBQUpdateRequest.sendPBQUpdateRequest(reqNode, exchange);
									}
						  }
						  finalresultNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
						  finalresultNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
						  finalresultNode.put(HealthQuoteConstants.QUOTE_RES_DATA, reqQListNode);
						  finalresultNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId);
						  finalresultNode.put(HealthQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
						  finalresultNode.put(HealthQuoteConstants.BUSINESSLINE_ID, 4);
						  LeadProfileRequest.sendLeadProfileRequest(finalresultNode, exchange);
						  exchange.getIn().setBody(finalresultNode);
					
				 }else{
					 	this.log.error("After product validation, all products failed.");
						finalresultNode.put(HealthQuoteConstants.QUOTE_RES_CODE,this.responseConfigNode.findValue("carQuoteDataErrorCode").intValue());
						finalresultNode.put(HealthQuoteConstants.QUOTE_RES_MSG,  this.responseConfigNode.findValue("carQuoteDataErrorMessage").textValue());
						finalresultNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
						finalresultNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId);
						finalresultNode.put(HealthQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
						exchange.getIn().setBody(finalresultNode);
						
				 }
			 }
			 catch(Exception e)
			 {
				 
				 this.log.error("Exception at HealthRequestProcessor ",e);
				 finalresultNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
				 finalresultNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
				 finalresultNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
				 exchange.getIn().setBody(finalresultNode);
				 log.error(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"QUOTEREQ|ERROR|"+"HealthRequestProcessor : ",e);
			 }
		 }
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public List<JsonObject> replicateProducts(List<JsonObject> productsList,JsonNode prodRatingNode,ObjectNode filteredRating)
		{
			  List<JsonObject> finalProductList = new ArrayList<JsonObject>();
			
			  for(JsonObject product:productsList)
			  {
				  // get ratings only for applicable products and store it in filteredRating node
				  for (JsonNode ratingNode : prodRatingNode) {
			            if ((product.getInt(HealthQuoteConstants.DROOLS_PLANID).intValue() == ratingNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()) && 
			            	(product.getInt(HealthQuoteConstants.DROOLS_CARRIERID).intValue() == ratingNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()))
			            {
			            	filteredRating.put(product.getInt(HealthQuoteConstants.DROOLS_PLANID).toString(), ratingNode.get(HealthQuoteConstants.CATEGORY_MAP));
			            	break;
			            }
			      }
				  // start replication for products having riders
				  if(product.containsKey(HealthQuoteConstants.RIDER_LIST))
				  {
					  // add product with default riders
					  Map<String, Object> productMap= product.toMap();
					  ArrayList riderArray = (ArrayList)productMap.remove(HealthQuoteConstants.RIDER_LIST);
					  //Request without any riders
					  finalProductList.add(JsonObject.from(productMap));
					  productMap.put(HealthQuoteConstants.IS_RIDER, true);
					  JsonArray addRiderArray = JsonArray.create();
					  JsonObject rider = null;
					  for(int i=0;i<riderArray.size();i++)
					  {
						  	  //JsonObject rider = JsonObject.create();
						  	  Map<String,Object> riderMap = (Map<String,Object>)riderArray.get(i);
						  	  if(riderMap.get(HealthQuoteConstants.RIDER_TYPE).toString().equalsIgnoreCase(HealthQuoteConstants.RIDER_TYPE_VALUE))
						  	  {
						  		  rider = JsonObject.from(riderMap);
						  		  JsonArray singleRiderArray = JsonArray.create();
						  		  singleRiderArray.add(rider);
						  		  addRiderArray.add(rider);
						  		  JsonObject obj = JsonObject.from(productMap);
						  		  obj.put(HealthQuoteConstants.RIDER_LIST, singleRiderArray);
						  		  finalProductList.add(obj);
						  		  if(addRiderArray.size() > 1)
						  		  {
						  			  obj = JsonObject.from(productMap);
							  		  obj.put(HealthQuoteConstants.RIDER_LIST, addRiderArray);
							  		  finalProductList.add(obj);
						  		  }
						  	  }
					  }
					  
				  }
				  else
				  {
					  finalProductList.add(product);
				  }
			  }
			
			return finalProductList;
		}
	}
