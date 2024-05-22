package com.idep.healthquote.form.req;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.healthquote.exception.processor.ExecutionTerminator;
import com.idep.healthquote.util.HealthQuoteConstants;

public class HealthDroolReqFromProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthDroolReqFromProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = null;
	JsonNode droolSumInsuredConfig=null;
	JsonNode droolURL = null;
	JsonNode errorNode;
	
	 @Override
	public void process(Exchange exchange)throws ExecutionTerminator, JsonProcessingException, IOException{
		 
		    try
		    {
		    	
		      if (this.serverConfig == null) {
		        	
		          this.serverConfig = CBInstanceProvider.getServerConfigInstance();
		          this.log.info("ServerConfig bucket instance created");
		          this.droolURL = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.DROOL_URL_CONFIG).content().toString());
		          this.log.info("Drool URL configuration Loaded");
		          this.productService = CBInstanceProvider.getProductConfigInstance();
		          this.log.info("ProductData bucket instance created");
		          this.droolSumInsuredConfig = this.objectMapper.readTree(this.productService.getDocBYId(HealthQuoteConstants.DROOL_SUMINSURED_CONFIG).content().toString());
		          
		      }	
		       
		      String quotedata = exchange.getIn().getBody().toString();
		      JsonNode reqNode = this.objectMapper.readTree(quotedata);
		      JsonNode quoteParamNode = reqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM);
		      String validateQuoteType = "DroolQuoteType" + quoteParamNode.get(HealthQuoteConstants.SERVICE_QUOTE_TYPE).intValue();
		      JsonNode productInfoNode = reqNode.get(HealthQuoteConstants.PRODUCT_INFO);
		      int carrierId = productInfoNode.get(HealthQuoteConstants.DROOLS_CARRIERID).intValue();
		      int planId = productInfoNode.get(HealthQuoteConstants.DROOLS_PLANID).intValue();
		      
		      JsonNode planDocumentNode = this.objectMapper.readTree(this.productService.getDocBYId(HealthQuoteConstants.CARRIER_PLAN+carrierId+"-"+planId).content().toString());
		      long minsumInsured = sumInsuredCalculation(productInfoNode.get("sumInsured").longValue(),
		    		  									droolSumInsuredConfig.get("Carrier-"+carrierId+"-"+planId));
		      //ArrayNode userSelectedRiders  = (ArrayNode)quoteParamNode.get("riders");
		      ArrayNode ridersListNode = null;
		      ridersListNode = (ArrayNode)productInfoNode.get("riderList");
		    
		      if(ridersListNode!=null)
		      {
		    	  log.info("ridedrList node is not null");
		    	  ArrayNode ridersList = this.objectMapper.createArrayNode();
		    	  ArrayNode planRidersList = this.objectMapper.createArrayNode();
		    	/*
		    	 * isRiderIteration is flag added in HealthPlan-60-68 and HealthPlan-28-80 in productData document.
		    	*/
		    	  log.info("planDocumentNode : "+planDocumentNode.has("isRiderIteration"));
		    	 if(planDocumentNode.has("isRiderIteration"))
		    	 {
		    		  log.info("productInfo node has adityaBirla diamond Plan");
		    		  
		    		  for(JsonNode rider:ridersListNode)
			    	  {
			    		  ObjectNode riderData =  this.objectMapper.createObjectNode();
			    		  if(rider.get("riderType").textValue().equals("R")&&
		    				  rider.get("applicable").textValue().equals("I"))
			    		  {
			    			  riderData.put("riderId", rider.get("riderId").intValue());
			    			  riderData.put("riderName", rider.get("riderName").textValue());
			    			  ridersList.add(riderData);
			    		  }
			    		  else if(rider.get("riderType").textValue().equals("PR")&&
			    				  rider.get("applicable").textValue().equals("I"))
			    		  {
			    			  riderData.put("riderId", rider.get("riderId").intValue());
			    			  riderData.put("riderName", rider.get("riderName").textValue());
			    			  ridersList.add(riderData);
			    		  }
			    	  }
		    	 }
		    	  else
		    	  {
		    	  for(JsonNode rider:ridersListNode)
		    	  {
		    		  ObjectNode riderData =  this.objectMapper.createObjectNode();
		    		   if(productInfoNode.has("isHCB")&& productInfoNode.get("isHCB").asText().equalsIgnoreCase("Y") && quoteParamNode.has("riders") && quoteParamNode.get("riders").size()>0){
			    		ArrayNode riderListFromUI = (ArrayNode)quoteParamNode.get("riders");
			    		JsonNode ridersUI = riderListFromUI.get(0);
					    
			    		  if(quoteParamNode.has("dailyCashLimit") && quoteParamNode.has("noOfDays") && rider.get("riderType").textValue().equals("PR")&&
			    				 rider.get("applicable").textValue().equals("I")){
					    		 riderData.put("riderId", rider.get("riderId").intValue());
				    			 riderData.put("riderName", rider.get("riderName").textValue());
					    		 riderData.put("dailyCashLimit", quoteParamNode.get("dailyCashLimit").intValue());
				    			 riderData.put("noOfDays", quoteParamNode.get("noOfDays").intValue());
				    			 planRidersList.add(riderData);
			    		  }
			    		  else if(productInfoNode.has("dailyCashLimit")&& productInfoNode.has("noOfDays") && rider.get("riderType").textValue().equals("PR")&&
			    				 rider.get("applicable").textValue().equals("I")){
					    		 riderData.put("riderId", rider.get("riderId").intValue());
				    			 riderData.put("riderName", rider.get("riderName").textValue());
					    		 riderData.put("dailyCashLimit", productInfoNode.get("dailyCashLimit").intValue());
				    			 riderData.put("noOfDays", productInfoNode.get("noOfDays").intValue());
				    			 planRidersList.add(riderData);
			    		  }
			    		  
		 		     }
		    		  
		    		  else if(rider.get("riderType").textValue().equals("R")&&
		    				  rider.get("applicable").textValue().equals("I"))
		    		  {
		    			  riderData.put("riderId", rider.get("riderId").intValue());
		    			  riderData.put("riderName", rider.get("riderName").textValue());
		    			  ridersList.add(riderData);
		    		  }
		    		    
		    		  else if(rider.get("riderType").textValue().equals("PR")&&
		    				  rider.get("applicable").textValue().equals("I"))
		    		  {
		    			  riderData.put("riderId", rider.get("riderId").intValue());
		    			  riderData.put("riderName", rider.get("riderName").textValue());
		    			  planRidersList.add(riderData);
		    		  }
		    	  	}
		    	 }
		    	
		    	  	  // attach riders to drool request
		    	    if(ridersList.size()>0)
		    	    {
					 
		    	    	((ObjectNode)quoteParamNode).put(HealthQuoteConstants.RIDER_LIST,ridersList);
					  //((ObjectNode)quoteParamNode).put(HealthQuoteConstants.IS_RIDER,productInfoNode.get(HealthQuoteConstants.IS_RIDER).booleanValue());
					  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.IS_RIDER,true);
					  
		    	    }
		    	    if(planRidersList.size()>0)
		    	    {
					  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.PLAN_RIDER_LIST,planRidersList);
					  //((ObjectNode)quoteParamNode).put(HealthQuoteConstants.IS_RIDER,productInfoNode.get(HealthQuoteConstants.IS_RIDER).booleanValue());
					  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.IS_RIDER,true);
		    	    }
		    	    
				 
		      }
		    
		      if(productInfoNode.has("secondEopinion"))
	    	  {
		    	  int secondEopinion = productInfoNode.get(HealthQuoteConstants.DROOLS_SECONDEOPINION).intValue();
		    	  log.info("secondEopinion is present in productInfoNode :"+secondEopinion);
		    	  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_SECONDEOPINION,secondEopinion);
	    		  
	    	  }
		      if(productInfoNode.has("wellnessCoach"))
		      {
		    	  int wellnessCoach = productInfoNode.get(HealthQuoteConstants.DROOLS_WELLNESSCOUCH).intValue();
		    	  log.info("wellnessCoach is present in productInfoNode :"+wellnessCoach);
		    	  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_WELLNESSCOUCH,wellnessCoach);
		      }
		      
		      if(quoteParamNode.has("dailyCashLimit") && quoteParamNode.has("noOfDays")){
		      ((ObjectNode)quoteParamNode).remove("dailyCashLimit");
		      ((ObjectNode)quoteParamNode).remove("noOfDays");
		      log.info("quoteParamNode after removing dailyCashLimit and noOfDays :: "+quoteParamNode);
		      }
		      String quoteEngineRequest=null;
		    
		      /**
		       * remove riders from quote param
		       * 
		       */
		    
		      if(quoteParamNode.has("riders")){
		      ((ObjectNode)quoteParamNode).remove("riders");
		      }
		      
		   
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_CARRIER, productInfoNode.get(HealthQuoteConstants.CARRIER_NAME).textValue());
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_CARRIERID, carrierId);
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_PLAN_TYPE, productInfoNode.get(HealthQuoteConstants.DROOLS_PLAN_TYPE).textValue());
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_PLANID, planId);
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_PLAN_NAME, productInfoNode.get(HealthQuoteConstants.DROOLS_PLAN_NAME).textValue());
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_INSURER_INDEX,productInfoNode.get(HealthQuoteConstants.DROOLS_INSURER_INDEX).doubleValue());
			  ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_SUM_INSURED, minsumInsured);
			 
			  	  exchange.setProperty(HealthQuoteConstants.FEATURES,productInfoNode.get(HealthQuoteConstants.FEATURES));
			  	  quoteEngineRequest = HealthQuoteConstants.DROOLS_HLTHQUOTE_REQUEST_PART1+ quoteParamNode.toString() + HealthQuoteConstants.DROOLS_HLTHQUOTE_REQUEST_PART2;
			  	  quoteEngineRequest = HealthQuoteConstants.DROOLS_HLTHQUOTE_REQUEST_PART3+ quoteEngineRequest + HealthQuoteConstants.DROOLS_HLTHQUOTE_REQUEST_PART4;
			      
			   //   this.log.info("HealthQuote DroolEngine Request : "+quoteEngineRequest);
			      exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.TRUE);
			      exchange.getIn().setHeader(HealthQuoteConstants.QUOTE_URL, this.droolURL.findValue(validateQuoteType).textValue());
			      exchange.getIn().setHeader(HealthQuoteConstants.SERVICE_QUOTE_TYPE, validateQuoteType);
			      exchange.getIn().setHeader(HealthQuoteConstants.DROOLS_AUTH_HEADER, HealthQuoteConstants.DROOLS_AUTH_DETAILS);
			      exchange.getIn().setHeader(HealthQuoteConstants.DROOLS_CONTENT_TYPE_HEADER, HealthQuoteConstants.DROOLS_CONTENT_TYPE);
			      exchange.getIn().setHeader(HealthQuoteConstants.CAMEL_HTTP_METHOD, HealthQuoteConstants.DROOLS_HTTP_METHOD);
			      exchange.getIn().removeHeader(HealthQuoteConstants.CAMEL_HTTP_PATH);
			      exchange.getIn().setHeader(HealthQuoteConstants.CAMEL_ACCEPT_CONTENT_TYPE, HealthQuoteConstants.DROOLS_ACCEPT_TYPE);
			      exchange.setProperty(HealthQuoteConstants.CARRIER_TRANSFORMREQ, quoteEngineRequest);
			      log.info(exchange.getProperty(HealthQuoteConstants.LOG_REQ).toString()+"SERVICEINVOKE|INIT|"+"Drool Quote Engine service invoked : "+quoteEngineRequest);
			      exchange.getIn().setBody(quoteEngineRequest);
		      
		    }
		    catch (NullPointerException e)
		    {
		    	this.log.error("Exception at HealthDroolReqFromProcessor : \tCarrierId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_CARRIERID)+"\tPlanId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_PLANID)+"\tChildPlanId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_CHILDPLANID),e);
		    	exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);		            
		    	ObjectNode objectNode = this.objectMapper.createObjectNode();
			    objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
			    objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
			    objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			    exchange.getIn().setBody(objectNode);
			    throw new ExecutionTerminator();
		    }
		    catch (Exception e)
		    {
		    	this.log.error("Exception at HealthDroolReqFromProcessor : \tCarrierId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_CARRIERID)+"\tPlanId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_PLANID)+"\tChildPlanId : "+exchange.getProperty(HealthQuoteConstants.PRODUCT_CHILDPLANID),e);
		    	exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);		            
		    	ObjectNode objectNode = this.objectMapper.createObjectNode();
			    objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
			    objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(HealthQuoteConstants.RESPONSE_CONFIG_DOC).get(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
			    objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			    exchange.getIn().setBody(objectNode);
			    throw new ExecutionTerminator();
		    }
	  }
	 
	 public long sumInsuredCalculation(long sumInsured,JsonNode sumInsuredConfig)
	 {
		 JsonNode sumInsuredArr = sumInsuredConfig.get("sumInsuredDetails");
		 for(JsonNode node : sumInsuredArr)
		 {
			 if(sumInsured>=node.get("startValue").longValue()&&
					   sumInsured<=node.get("endValue").longValue())
					{
				 		sumInsured = node.get("absoluteValue").longValue();
						break;
					}
		 }
		 
		 return sumInsured;
	 }
	 
	 
	 @SuppressWarnings(HealthQuoteConstants.UNCHECKED)
	 public ArrayNode validateRequestParam(String quoteType, JsonNode reqNode, String validationConfig)
	  {
	    ArrayNode arrayNode = this.objectMapper.createArrayNode();
	    try
	    {
	      JsonNode validateNode = this.objectMapper.readTree(validationConfig);
	      if (validateNode.get(HealthQuoteConstants.VALIDATION_ACTIVE).textValue().equals(HealthQuoteConstants.Y))
	      {
	        this.log.info("validating request");
	        JsonNode quoteTypeNode = validateNode.findValue(quoteType);
	        Map<String, String> fieldsMap = objectMapper.readValue(quoteTypeNode.toString(), Map.class);
	        for (Map.Entry<String, String> field : fieldsMap.entrySet()) {
	          if (field.getValue().equals(HealthQuoteConstants.Y)) {
	            try
	            {
	              JsonNode s = null;
	              s = reqNode.findValue(field.getKey());
	              if (s == null)
	              {
	                this.log.info("field " + field.getKey() + " missing in request");
	                arrayNode.add(field.getKey());
	              }
	              else
	              {
	                this.log.info("field " + field.getKey() + " exist in request");
	              }
	            }
	            catch (NullPointerException e)
	            {
	              this.log.error("field is missing");
	            }
	          } else {
	            this.log.info("field level validation skipped");
	          }
	        }
	      }
	      else
	      {
	        this.log.info("validation skipped");
	      }
	    }
	    catch (JsonProcessingException e)
	    {
	    	this.log.error("JsonProcessingException at HealthDroolReqFromProcessor : validateRequestParam : ", e);
	    }
	    catch (IOException e)
	    {
	    	this.log.error("IOException at HealthDroolReqFromProcessor : validateRequestParam : ", e);
	    }
	    catch (Exception e)
	    {
	    	this.log.error("Exception at HealthDroolReqFromProcessor : validateRequestParam : ", e);
	    }
	    return arrayNode;
	  }
	 
	 

}
