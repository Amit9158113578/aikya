package com.idep.lifequote.req.form;

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
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.lifequote.service.impl.ProductRatingPicker;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeDroolReqFromProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeDroolReqFromProcessor.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();;
	CBService productService = CBInstanceProvider.getProductConfigInstance();;
	String validationConfig = null;
	//JsonObject responseConfigNode = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.RESPONSE_CONFIG_DOC);
	JsonNode droolURL = null;
	JsonNode errorNode;
	JsonNode lifePremiumRateConfig=null;

	public void process(Exchange exchange)throws Exception{
		this.log.debug("Inside LifeDroolReqFromProcessor.");
		try{
			if(this.droolURL == null){
				this.droolURL = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.DROOL_URL_CONFIG).content().toString());
			}

			String quotedata = exchange.getIn().getBody().toString();
			JsonNode reqNode = this.objectMapper.readTree(quotedata);
			JsonNode quoteParamNode = reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM);
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.SERVICE_QUOTE_TYPE, 1);
			String validateQuoteType = LifeQuoteConstants.QUOTE_TYPE + quoteParamNode.get(LifeQuoteConstants.SERVICE_QUOTE_TYPE).intValue();
			JsonNode productInfoNode = reqNode.get(LifeQuoteConstants.PRODUCT_INFO);

			this.log.info("-------Life Quote : " + productInfoNode.get("carrierId") + " : " + productInfoNode.get("carrierName") + "-----------------");
			this.log.info("UI Input quoteParam node : " + quoteParamNode);
			this.log.info("UI Input productInfo node : " + productInfoNode);
			this.log.info("-----------------------------------------------------------------------------");
			//****** if frequency is required at drool calculation then put frequency ="y" in productInfo (lifeProduct-53-1)
				if(!productInfoNode.has("frequency")){
					((ObjectNode)reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM)).remove("frequency");
					((ObjectNode)quoteParamNode).remove("frequency");
					exchange.getIn().setBody(reqNode);
					this.log.info("Frequency removed from quoteParam node : " + quoteParamNode);
				}
				
				
				//****** following block will remove mobileNumber from drool request ******
				//written by shreyas mete on request from Rajat 	
					{
						if(((ObjectNode)quoteParamNode).has("mobileNumber"))
								{
									((ObjectNode)quoteParamNode).remove("mobileNumber");
									this.log.info("mobileNumber removed from quoteParam node : " + quoteParamNode);
								}
						if(((ObjectNode)reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM)).has("mobileNumber")){
							((ObjectNode)reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM)).remove("mobileNumber");
							exchange.getIn().setBody(reqNode);
							this.log.info("mobileNumber removed from req node " );
							
						}
						
					}
					
				
			// form quote request
			FormLifeQuoteRequest lifequoteRequest = new FormLifeQuoteRequest();
			//ProductRatingPicker productRatingPicker = new ProductRatingPicker();
			String quoteEngineRequest = lifequoteRequest.formRequest(quoteParamNode, productInfoNode, this.serverConfig, this.productService);

			if(quoteEngineRequest.equals(LifeQuoteConstants.EXCEPTION)){
				this.log.error("Life DroolEngine Req not formed hence setting request flag to False ");
				this.log.error("Life quoteEngineRequest : " + quoteEngineRequest);
				exchange.getIn().setHeader(LifeQuoteConstants.REQUESTFLAG, LifeQuoteConstants.FALSE);
				ObjectNode objectNode = this.objectMapper.createObjectNode();
				
				objectNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_CODE).asInt());
				objectNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_MSG).asText());
				objectNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
				exchange.getIn().setBody(objectNode);
			}else{
				/* find product ratings */
				/*List<Map<String, Object>> productRatingList = productRatingPicker.getProductRatingDetails(quoteParamNode, productService);
				log.debug("LifeQuote Rating Picked");

				// put dummy ratings if not found in database
				if(productRatingList.isEmpty()){
					ObjectNode dummyRating = objectMapper.createObjectNode();
					dummyRating.put("", "");
					dummyRating.put("", "");
					exchange.setProperty(LifeQuoteConstants.RATINGS, dummyRating);
				}else{
					exchange.setProperty(LifeQuoteConstants.RATINGS, this.objectMapper.valueToTree(productRatingList.get(0)));
				}
				*/
				this.log.debug("Life Quote request formed and sent to drool : " + quoteEngineRequest);

				// set carrier transformed request in property
				exchange.setProperty("carrierTransformedReq",quoteEngineRequest);
				exchange.setProperty(LifeQuoteConstants.PRODUCT_INFO, productInfoNode);
				exchange.getIn().setHeader(LifeQuoteConstants.REQUESTFLAG, LifeQuoteConstants.TRUE);
				exchange.getIn().setHeader(LifeQuoteConstants.QUOTE_URL, this.droolURL.get("LifeQuote-"+productInfoNode.get("carrierId").asInt()+"-"+productInfoNode.get("productId").asInt()).asText());
				exchange.getIn().setHeader(LifeQuoteConstants.SERVICE_QUOTE_TYPE, validateQuoteType);
				exchange.getIn().setHeader(LifeQuoteConstants.DROOLS_AUTH_HEADER, LifeQuoteConstants.DROOLS_AUTH_DETAILS);
				exchange.getIn().setHeader(LifeQuoteConstants.DROOLS_CONTENT_TYPE_HEADER, LifeQuoteConstants.DROOLS_CONTENT_TYPE);
				exchange.getIn().setHeader(LifeQuoteConstants.CAMEL_HTTP_METHOD, LifeQuoteConstants.DROOLS_HTTP_METHOD);
				exchange.getIn().removeHeader(LifeQuoteConstants.CAMEL_HTTP_PATH);
				exchange.getIn().setHeader(LifeQuoteConstants.CAMEL_ACCEPT_CONTENT_TYPE, LifeQuoteConstants.DROOLS_ACCEPT_TYPE);
				
				if(productInfoNode.has("minAllowedPremium"))
				{
					exchange.setProperty(LifeQuoteConstants.MIN_ALLOWED_PREMIUM,productInfoNode.get("minAllowedPremium").asInt());
				}
				else
				{
					exchange.setProperty(LifeQuoteConstants.MIN_ALLOWED_PREMIUM,0);
				}
				
				exchange.getIn().setBody(quoteEngineRequest);
			}
		}catch(NullPointerException e){
			this.log.error("please check input request values provided, seems one of value is missing : " + e);
			exchange.getIn().setHeader(LifeQuoteConstants.REQUESTFLAG, LifeQuoteConstants.FALSE);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			
			
			objectNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_CODE).asInt());
			objectNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_MSG).asText());
			objectNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);
		}catch(Exception e){
			this.log.error("Exception occurred, while preparing request for drool : " + e);
			exchange.getIn().setHeader(LifeQuoteConstants.REQUESTFLAG, LifeQuoteConstants.FALSE);
			ObjectNode objectNode = this.objectMapper.createObjectNode();
			objectNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_CODE).asInt());
			objectNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_MSG).asText());
			objectNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(objectNode);
		}
	}

	public JsonNode validatePolicyTerm(JsonNode quoteParamNode){
		int policyTerm = quoteParamNode.findValue(LifeQuoteConstants.POLICY_TERM).intValue();
		if(policyTerm > 20){
			((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, 20);
		}else{
			int diff = policyTerm % 5;
			if(diff >= 3){
				int count = 5 - diff;
				policyTerm += count;
				((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, policyTerm);
			}else{
				policyTerm -= diff;
				((ObjectNode)quoteParamNode).put(LifeQuoteConstants.POLICY_TERM, policyTerm);
			}
		}
		this.log.debug("modified quote param node : " + quoteParamNode);
		return quoteParamNode;
	}

	@SuppressWarnings(LifeQuoteConstants.UNCHECKED)
	public ArrayNode validateRequestParam(String quoteType, JsonNode reqNode, String validationConfig){
		ArrayNode arrayNode = this.objectMapper.createArrayNode();
		try{
			JsonNode validateNode = this.objectMapper.readTree(validationConfig);
			if(validateNode.findValue(LifeQuoteConstants.VALIDATION_ACTIVE).textValue().equals(LifeQuoteConstants.Y)){
				JsonNode quoteTypeNode = validateNode.findValue(quoteType);
				Map<String, String> fieldsMap = objectMapper.readValue(quoteTypeNode.toString(), Map.class);
				for (Map.Entry<String, String> field : fieldsMap.entrySet()){
					if(((String)field.getValue()).equals(LifeQuoteConstants.Y)){
						try{
							JsonNode s = null;
							s = reqNode.findValue((String)field.getKey());
							if(s == null){
								this.log.debug("field " + (String)field.getKey() + " missing in request");
								arrayNode.add((String)field.getKey());
							}else{
								this.log.debug("field " + (String)field.getKey() + " exist in request");
							}
						}catch(NullPointerException e){
							this.log.error("field is missing");
						}
					}else{
						this.log.debug("field level validation skipped");
					}
				}
			}else{
				this.log.debug("validation skipped");
			}
		}catch(JsonProcessingException e){
			this.log.error("JsonProcessingException at LifeDroolReqFromProcessor : validateRequestParam : ", e);
		}catch(IOException e){
			this.log.error("IOException at LifeDroolReqFromProcessor : validateRequestParam : ", e);
		}catch(Exception e){
			this.log.error("JsonProcessingException at LifeDroolReqFromProcessor : validateRequestParam : ", e);
		}
		return arrayNode;
	}
}