package com.idep.healthquote.test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.healthquote.form.req.FormHealthQuoteRequest;
import com.idep.healthquote.form.req.HealthDroolReqFromProcessor;
import com.idep.healthquote.util.HealthQuoteConstants;

public class ExternalServiceReqProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthDroolReqFromProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = null;
	CBService policyTransService = null;
	String validationConfig = null;
	JsonNode responseConfigNode = null;
	JsonNode droolURL = null;
	JsonNode errorNode;
	JsonNode healthSumCalc = null;
	JsonNode HealthCarrierQNode=null;
	
	 @Override
	public void process(Exchange exchange) {
		 
		log.info("External : Health QuoteCalculation request process started");
		JsonNode reqNode=null;
		long lStartTime = System.currentTimeMillis();
		    
		    try
		    {
		    	
		      if (this.serverConfig == null) {
		        	
		          this.serverConfig = CBInstanceProvider.getServerConfigInstance();
		          this.log.info("ServerConfig bucket instance created");
		          this.validationConfig = this.serverConfig.getDocBYId(HealthQuoteConstants.QUOTE_VALIDATIONS).content().toString();
		          this.log.info("Quote validationConfig : " + this.validationConfig);
		          this.responseConfigNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.RESPONSE_CONFIG_DOC).content().toString());
		          this.log.info("ResponseMessages configuration loaded");
		          this.droolURL = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.DROOL_URL_CONFIG).content().toString());
		          this.log.info("Drool URL configuration Loaded");
		          this.HealthCarrierQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.HEALTH_CARRIERS_Q).content().toString());
		          this.log.info("HealthCarrierQNode configuration Loaded");
		          this.healthSumCalc = this.objectMapper.readTree(this.serverConfig.getDocBYId(HealthQuoteConstants.HEALTH_SUM_ASSURED_CALC).content().toString());
		          this.productService = CBInstanceProvider.getProductConfigInstance();
		          this.log.info("ProductData bucket instance created");
		          
		      }	
		        
		      String quotedata = exchange.getIn().getBody().toString();
		      log.info("HealthQuote request input : " + quotedata);
		      reqNode = this.objectMapper.readTree(quotedata);
		    
		      JsonNode quoteParamNode = reqNode.get(HealthQuoteConstants.SERVICE_QUOTE_PARAM);
		      JsonNode ratingParamNode = reqNode.get(HealthQuoteConstants.SERVICE_HEALTH_RATING_PARAM);
		      log.info("quoteParamNode : "+quoteParamNode);
		      String validateQuoteType = HealthQuoteConstants.QUOTE_TYPE + quoteParamNode.findValue(HealthQuoteConstants.SERVICE_QUOTE_TYPE).intValue();
		      log.info("validateQuoteType : "+validateQuoteType);
		      ArrayNode arrNode = validateRequestParam(validateQuoteType, reqNode, this.validationConfig);
		      
		      if (arrNode.size() > 0) {

			      exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);
			      ObjectNode objectNode = this.objectMapper.createObjectNode();
			      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.QUOTE_DATA_MISSING_CODE).intValue());
			      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.QUOTE_DATA_MISSING_MSG).textValue());
			      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, arrNode);
			      exchange.getIn().setBody(objectNode);
		        
		      }
		      
		      else {
		    	  
		          FormHealthQuoteRequest healthquoteRequest = new FormHealthQuoteRequest();
		          List<Map<String, Object>> productList=null;
		          
		          try {
		    	   
		      	      long minsumInsured = 0L;
		      	      long maxsumInsured = 0L;
		      	      
			          String carrier = exchange.getProperty(HealthQuoteConstants.SOURCEQUEUENAME).toString();
			          String carrierName = carrier.substring(8);
			          int carrierId = HealthCarrierQNode.get(HealthQuoteConstants.REQ_Q_LIST).get(carrierName).intValue();
		      	      
				      minsumInsured = healthSumCalc.get(HealthQuoteConstants.MIN_BASIC_PREMIUM).longValue() + 
						        healthSumCalc.get(HealthQuoteConstants.ADULT_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.ADULT_COUNT).longValue() + 
						        healthSumCalc.get(HealthQuoteConstants.CHILD_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.CHILD_COUNT).longValue();
						      
				      maxsumInsured = healthSumCalc.get(HealthQuoteConstants.MAX_BASIC_PREMIUM).longValue() + 
						        healthSumCalc.get(HealthQuoteConstants.ADULT_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.ADULT_COUNT).longValue() + 
						        healthSumCalc.get(HealthQuoteConstants.CHILD_PREMIUM).longValue() * quoteParamNode.get(HealthQuoteConstants.CHILD_COUNT).longValue();		        	 
					  /* get product details */
					 productList = healthquoteRequest.getProductDetails(quoteParamNode,carrierId,minsumInsured,maxsumInsured,this.productService);
					  
					  if (productList.isEmpty())
		        	  {
		        		  this.log.info("No Matching products found");
		        		  productList=null;
		        	  }

		        	  else
		        	  {
		        		  ObjectNode featuresList = this.objectMapper.createObjectNode();
		        		  
					      for (Map<String, Object> product : productList)
					      {
					    	// set product features  
					    	featuresList.put(product.get(HealthQuoteConstants.DROOLS_PLANID).toString(), this.objectMapper.valueToTree(product.get(HealthQuoteConstants.FEATURES)));
					    	  
					        ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_CARRIER, product.get(HealthQuoteConstants.CARRIER_NAME).toString());
					        ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_CARRIERID, ((Number)product.get(HealthQuoteConstants.DROOLS_CARRIERID)).intValue());
					        ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_PLAN_TYPE, product.get(HealthQuoteConstants.DROOLS_PLAN_TYPE).toString());
					        ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_PLANID, ((Number)product.get(HealthQuoteConstants.DROOLS_PLANID)).intValue());
					        ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_INSURER_INDEX, ((Number)product.get(HealthQuoteConstants.DROOLS_INSURER_INDEX)).doubleValue());
					        ((ObjectNode)quoteParamNode).put(HealthQuoteConstants.DROOLS_SUM_INSURED, minsumInsured);
					      }
				           
					      exchange.setProperty(HealthQuoteConstants.FEATURES,featuresList);
					      
			      
		        	  }
		          }
		          catch (Exception e)
		          {
		        	  this.log.error("Exception at ExternalServiceReqProcessor : ", e);
		        	  productList=null;
		          }
		          
		          
		          if ((productList.isEmpty()) || productList==null) {
		        	  
		            this.log.info("HealthQuote Req not formed hence setting request flag to False");
		            exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);
		            ObjectNode objectNode = this.objectMapper.createObjectNode();
		            objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.PRD_NOT_FOUND_CODE).intValue());
		            objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.PRD_NOT_FOUND_MSG).textValue());
		            objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		            exchange.getIn().setBody(objectNode);
		            
		          }
		         
		          else
		          {
		          
		          /* find product ratings */
			      List<Map<String, Object>> healthProductRatingList = healthquoteRequest.getProductRatingDetails_old(quoteParamNode,ratingParamNode,productService);
			      log.info("HealthQuote Rating List : " + healthProductRatingList.toString());
		          
		    	  exchange.setProperty(HealthQuoteConstants.QUOTE_REQ_PROPERTY, this.objectMapper.readTree(quotedata));
			      exchange.setProperty(HealthQuoteConstants.SERVICE_QUOTE_TYPE, quoteParamNode.findValue(HealthQuoteConstants.SERVICE_QUOTE_TYPE).intValue());
			      exchange.setProperty(HealthQuoteConstants.RATINGS, this.objectMapper.valueToTree(healthProductRatingList.get(0)));
			      
			      this.log.info("HealthQuote External Service Req is sent for client specific transformation");

			      exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.TRUE);
			      //exchange.getIn().setHeader("quoteURL", this.droolURL.findValue(validateQuoteType).textValue());

			      /* write code to gather all attributes to send to sutrrMapper for transforming request */
			      exchange.getIn().setBody(quotedata);
			      
			      long lEndTime = System.currentTimeMillis();
			      this.log.info("External : HealthQuote Req Formation Elapsed milliseconds : " + (lEndTime - lStartTime));
		          
		          }
			      
		      }
		      
		      
		    }
		    catch (NullPointerException e)
		    {
		      e.printStackTrace();
		      this.log.error("HealthQuote Error Message : "+e.getMessage());
		      this.log.error("HealthQuote : please check input request values provided, seems one of value is missing");
		      this.log.info("HealthQuote DroolEngine Req not formed hence setting request flag to False");
		      exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);
		    }
		    catch (Exception e)
		    {
		      e.printStackTrace();
		      this.log.error("HealthQuote Error Message : "+e.getMessage());
		      this.log.error("HealthQuote Exception occurred, please analyze request");
		      this.log.info("HealthQuote DroolEngine Req not formed hence setting request flag to False");
		      exchange.getIn().setHeader(HealthQuoteConstants.REQUESTFLAG, HealthQuoteConstants.FALSE);
		      ObjectNode objectNode = this.objectMapper.createObjectNode();
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_CODE, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_CODE).intValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_MSG, this.responseConfigNode.findValue(HealthQuoteConstants.ERROR_CONFIG_MSG).textValue());
		      objectNode.put(HealthQuoteConstants.QUOTE_RES_DATA, this.errorNode);
		      exchange.getIn().setBody(objectNode);
		    }
	  }
	 
	 
	 @SuppressWarnings(HealthQuoteConstants.UNCHECKED)
	 public ArrayNode validateRequestParam(String quoteType, JsonNode reqNode, String validationConfig)
	  {
	    ArrayNode arrayNode = this.objectMapper.createArrayNode();
	    try
	    {
	      JsonNode validateNode = this.objectMapper.readTree(validationConfig);
	      if (validateNode.findValue(HealthQuoteConstants.VALIDATION_ACTIVE).textValue().equals(HealthQuoteConstants.Y))
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
	      this.log.error("JsonProcessingException at ExternalServiceReqProcessor : validateRequestParam", e);
	    }
	    catch (IOException e)
	    {
	    	this.log.error("IOException at ExternalServiceReqProcessor : validateRequestParam", e);
	    }
	    catch (Exception e)
	    {
	    	this.log.error("Exception at ExternalServiceReqProcessor : validateRequestParam", e);	
	    }
	    return arrayNode;
	  }

}
