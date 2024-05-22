package com.idep.lifequote.req.processor;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.lifequote.service.impl.ProductPicker;
import com.idep.lifequote.service.impl.ProductValidator;
import com.idep.lifequote.util.CorrelationKeyGenerator;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeRequestProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeRequestProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	JsonNode errorNode;
	JsonNode LifeCarrierReqQNode = null;
	JsonNode LifeCarrierResQNode = null;
	JsonNode LifeProductQueryConfig = null;
	JsonNode keyConfigDoc;
	String encQuoteId; 
	//JsonObject responseConfigNode = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.RESPONSE_CONFIG_DOC);
	//JsonObject documentIdConfig = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.DOCID_CONFIG);

	public void process(Exchange exchange) throws Exception{
		log.debug("Inside LifeRequestProcessor");
		try{
			if(this.serverConfig == null){
				this.serverConfig = CBInstanceProvider.getServerConfigInstance();
				this.LifeCarrierReqQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.LIFE_CARRIERS_Q).content().getObject(LifeQuoteConstants.REQ_Q_LIST).toString());
				this.LifeCarrierResQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.LIFE_CARRIERS_Q).content().getObject(LifeQuoteConstants.RES_Q_LIST).toString());
				keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
			}

			// Fetching queries from DB required for Life Quote Calculation.
			this.LifeProductQueryConfig = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.LIFE_PRODUCT_QUERY_CONFIG).content().toString());
			log.debug("Life Product Query Config : " + LifeProductQueryConfig);

			List<JsonObject> productsList = null;
			
			// create producer template to send messages to Q
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			// process input request 
			String quotedata = exchange.getIn().getBody().toString();
			
			ProductPicker productPicker = new ProductPicker();
			ProductValidator productValidator = new ProductValidator();

			log.info("LifeQuote request input : " + quotedata);
			JsonNode reqNode = this.objectMapper.readTree(quotedata);
			JsonNode quoteParamNode = reqNode.get(LifeQuoteConstants.SERVICE_QUOTE_PARAM);
			
			//get product details
			productsList = productPicker.fetchProductsFromDB(quoteParamNode, LifeProductQueryConfig, productService);
			this.log.debug("Products list fetched from DB : " + productsList);

			if(productsList.isEmpty()){
				this.log.error("Products not found");
				productsList=null;
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorCode").asInt());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorCode").asInt());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
				exchange.getIn().setBody(finalresultNode);
			}else{
				// create quote id
				//String lifeQuoteId = this.documentIdConfig.getString("lifeQuoteId")+this.serverConfig.updateSequence("lifequoteId");
				String lifeQuoteId = DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.DOCID_CONFIG).get("lifeQuoteId").asText()+this.serverConfig.updateDBSequence("SEQCARQUOTE");
				this.log.debug("Total products fetched from DB : " + productsList.size());
				this.log.info("Total products fetched from DB : " + productsList.size());
				ArrayNode reqQListNode = objectMapper.createArrayNode();
				ArrayNode finalProductList = productValidator.validateLifeProduct(quoteParamNode, productsList);
				if(finalProductList != null && finalProductList.size() > 0){
					for(JsonNode product : finalProductList){
						try{
							((ObjectNode)reqNode).put(LifeQuoteConstants.PRODUCT_INFO, this.objectMapper.readTree(product.toString()));
							ObjectNode objectNode = objectMapper.createObjectNode();
							String carrierReqQName = LifeCarrierReqQNode.get(product.get(LifeQuoteConstants.CARRIER_ID).asText()).textValue();
							objectNode.put(LifeQuoteConstants.INPUT_MESSAGE, reqNode);
							String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
							objectNode.put(LifeQuoteConstants.UNIQUE_KEY, correlationKey);
							objectNode.put(LifeQuoteConstants.QUOTE_ID, lifeQuoteId);
							encQuoteId = GenrateEncryptionKey.GetEncryptedKey(lifeQuoteId, keyConfigDoc.get("encryptionKey").asText());
							log.info("Encrypted Life QUOTE_ID :" + encQuoteId);
							objectNode.put(LifeQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
							
							String uri = "activemq:queue:"+carrierReqQName;
							exchange.getIn().setBody(objectNode.toString());
							exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
							template.send(uri, exchange);

							ObjectNode resultNode = objectMapper.createObjectNode();
							resultNode.put(LifeQuoteConstants.QNAME, LifeCarrierResQNode.get(product.get(LifeQuoteConstants.CARRIER_ID).asText()).textValue());
							resultNode.put(LifeQuoteConstants.MESSAGE_ID, correlationKey);
							resultNode.put(LifeQuoteConstants.QUOTE_ID, lifeQuoteId);
							resultNode.put(LifeQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
							resultNode.put(LifeQuoteConstants.STATUS, 0);
							resultNode.put(LifeQuoteConstants.CARRIER_ID, product.get(LifeQuoteConstants.CARRIER_ID).asText());
							

							reqQListNode.add(resultNode);
						}catch(NullPointerException e){
							this.log.error("product not configured Exception : " + e);
						}
					}
					 /***
					 * checking if profession quote id present in request then quote request sending to activemq:queue:pbqupdatereqQ for
					 *  update ProfessionQuoteId (latest car quote id and request.  
					 * **/
				    log.info("Life carrier request for update PBQ REQUEST  :"+reqNode);
					if(reqNode.findValue("PROF_QUOTE_ID")!=null){
								log.info("sending request to update PBQ document & request ");
								((ObjectNode)reqNode).put(LifeQuoteConstants.QUOTE_ID, lifeQuoteId);
								PBQUpdateRequest.sendPBQUpdateRequest(reqNode, exchange);
					}
					
					
					
					// prepare UI response
					ObjectNode finalresultNode = objectMapper.createObjectNode();
					finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.SUCC_CONFIG_CODE).asInt());
					finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.SUCC_CONFIG_MSG).asText());
					finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, reqQListNode);
					finalresultNode.put(LifeQuoteConstants.QUOTE_ID, lifeQuoteId);
					finalresultNode.put(LifeQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
					finalresultNode.put(LifeQuoteConstants.BUSINESS_LINE_ID, 1);
					LeadProfileRequest.sendLeadProfileRequest(finalresultNode, exchange);
					exchange.getIn().setBody(finalresultNode);
					this.log.debug("LifeQuote Response sent to UI : "+finalresultNode);
				}else{
					this.log.info("After product validation, all products failed.");
					productsList=null;
					ObjectNode finalresultNode = objectMapper.createObjectNode();
					finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE,DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorCode").asInt());
					finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG,  DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorMessage").asInt() );
					finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
					
					exchange.getIn().setBody(finalresultNode);
				}
			}
		}catch(Exception e){
			this.log.error("Exception at LifeRequestProcessor : ", e);
			ObjectNode finalresultNode = objectMapper.createObjectNode();
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_CODE).asInt());
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.ERROR_CONFIG_MSG).asInt());
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(finalresultNode);
		}
	}
}