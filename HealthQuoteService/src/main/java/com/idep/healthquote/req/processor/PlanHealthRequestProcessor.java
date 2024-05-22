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
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.healthquote.form.req.FormHealthQuoteRequest;
import com.idep.healthquote.util.CorrelationKeyGenerator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class PlanHealthRequestProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PlanHealthRequestProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = null;
	JsonNode errorNode;
	JsonNode responseConfigNode = null;
	JsonNode healthSumCalc = null;
	JsonNode HealthCarrierReqQNode=null;
	JsonNode HealthCarrierResQNode=null;
	ObjectNode productCacheList = null;
	
	@Override
	public void process(Exchange exchange) {
		 
		 try
		 {
			 
	         if (this.serverConfig == null) { 
				
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
			    // fetch all products applicable for input request
			    FormHealthQuoteRequest healthquoteRequest = new FormHealthQuoteRequest();
			    ArrayNode reqQListNode = objectMapper.createArrayNode();
			    log.info("PlanHealthQuote : "+reqNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue() +"  : "+reqNode.get("planId").intValue());
			    JsonNode productsList = null;
			    ObjectNode diseaseList = this.objectMapper.createObjectNode();
			    JsonNode product =  this.objectMapper.readTree(this.productService.getDocBYId("HealthPlan-"+reqNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()+"-"+reqNode.get("planId").intValue()).content().toString());
			    long minsumInsured = 0;
			    long maxsumInsured = 0;
			    ObjectNode productNode = objectMapper.createObjectNode();
			    /**
			     * check if hospitalization limit is provided in UI request
			     */
			    if(reqNode.has("sumInsured")){
			    ((ObjectNode)productNode).put("sumInsured",reqNode.get("sumInsured"));
			    }else if(personalInfoNode.has(HealthQuoteConstants.MIN_HOSP_LIMT)&&
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
				 sumInsuredNode.put("minsumInsured", minsumInsured);
				 sumInsuredNode.put("maxsumInsured", maxsumInsured);
				 this.log.debug("minsumInsured : "+minsumInsured + " : maxsumInsured : " +maxsumInsured);
				
				 // find products ratings
				 List<Map<String, Object>> healthProductRatingList = healthquoteRequest.getProductRatingDetails(quoteParamNode,ratingParamNode,productService);
				 JsonNode ratingsNode = this.objectMapper.valueToTree(healthProductRatingList.get(0));
				 JsonNode defaultRiskType = ratingsNode.get(HealthQuoteConstants.DEFAULT_RISK_TYPE);
				 JsonNode prodRatingNode = ratingsNode.get(HealthQuoteConstants.PRODUCTS);
				 ObjectNode ratingDataNode = this.objectMapper.createObjectNode();
				 ratingDataNode.put(HealthQuoteConstants.DEFAULT_RISK_TYPE, defaultRiskType);
				 ObjectNode filteredRating = this.objectMapper.createObjectNode();
				 for (JsonNode ratingNode : prodRatingNode) {
			            if ((product.get(HealthQuoteConstants.DROOLS_PLANID).intValue() == ratingNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue()) && 
			            	(product.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue() == ratingNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue()))
			            {
			            	filteredRating.put(product.get(HealthQuoteConstants.DROOLS_PLANID).asText(), ratingNode.get(HealthQuoteConstants.CATEGORY_MAP));
			            	break;
			            }
			      }
				 
				 
				 
				 
			    // create quote id
				  String healthQuoteId = DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.DOCID_CONFIG).get("healthQuoteId").asText()+this.serverConfig.updateDBSequence("SEQHEALTHQUOTE");;
				  
				  		log.debug("PlanHealthQuote Product : "+product);
				  		if(reqNode.has("childPlanId")){
				  		ArrayNode planList = (ArrayNode)product.get("plans");	
				  		log.debug("PlanHealthQuote Product :"+planList);
				  		for(JsonNode plans : planList){
				  				if(plans.get("childPlanId").asInt()==reqNode.get("childPlanId").asInt()){
				  					productNode.put("carrierName", plans.get("planName").textValue());
									productNode.put("insurerIndex", 0);
									productNode.put("carrierId", product.get("carrierId").intValue());
									productNode.put("planId", product.get("planId").intValue());
									productNode.put("planType", product.get("planType").textValue());
									productNode.put("Features", product.get("Features"));
									productNode.putAll((ObjectNode)plans);
				  				}
				  		}
				  		log.debug("PlanHealthQuote Final productNode :"+productNode);
				  		
				  		}else{
				  			productNode.put("carrierName", product.get("plans").get(0).get("planName").textValue());
							productNode.put("insurerIndex", 0);
							productNode.put("carrierId", product.get("carrierId").intValue());
							productNode.put("planId", product.get("planId").intValue());
							productNode.put("planType", product.get("planType").textValue());
							productNode.put("Features", product.get("Features"));
							productNode.putAll((ObjectNode)product.get("plans").get(0));		
				  		}
				  		
			    		((ObjectNode)reqNode).put(HealthQuoteConstants.PRODUCT_INFO, productNode);
						  ObjectNode objectNode = objectMapper.createObjectNode();
						  String carrierReqQName = HealthCarrierReqQNode.get(product.get(HealthQuoteConstants.DROOLS_CARRIERID).asText()).textValue();
						  objectNode.put(HealthQuoteConstants.INPUT_MESSAGE, reqNode);
						  
						  String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
						  objectNode.put(HealthQuoteConstants.UNIQUE_KEY, correlationKey);
						  // attach product rating to Q message
						  ratingDataNode.put(HealthQuoteConstants.RATINGS_LIST,filteredRating.get(product.get(HealthQuoteConstants.DROOLS_PLANID).asText()));
						  objectNode.put(HealthQuoteConstants.HEALTH_RATINGS, ratingDataNode);
						  objectNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId );
						 
						  
						  String uri = "activemq:queue:"+carrierReqQName;
						  exchange.getIn().setBody(objectNode.toString());
						  exchange.setPattern(ExchangePattern.InOnly); // set exchange spattern
						  template.send(uri, exchange);

						  ObjectNode resultNode = objectMapper.createObjectNode();
						  resultNode.put(HealthQuoteConstants.QNAME, HealthCarrierResQNode.get(product.get(HealthQuoteConstants.DROOLS_CARRIERID).asText()).textValue());
					      resultNode.put(HealthQuoteConstants.MESSAGE_ID, correlationKey);
					      resultNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId);
						  resultNode.put(HealthQuoteConstants.DROOLS_CARRIERID, product.get(HealthQuoteConstants.DROOLS_CARRIERID).asInt());
						  resultNode.put(HealthQuoteConstants.STATUS, 0);
					      reqQListNode.add(resultNode);
						  
					  					  
					  ObjectNode finalresultNode = objectMapper.createObjectNode();
					  finalresultNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_CODE).intValue());
					  finalresultNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.SUCC_CONFIG_MSG).textValue());
					  finalresultNode.put(HealthQuoteConstants.QUOTE_RES_DATA, reqQListNode);
					  finalresultNode.put(HealthQuoteConstants.QUOTE_ID, healthQuoteId);
					  exchange.getIn().setBody(finalresultNode);
			 }
			 
			 catch(Exception e)
			 {
				 this.log.error("Exception at HealthRequestProcessor ",e);
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
