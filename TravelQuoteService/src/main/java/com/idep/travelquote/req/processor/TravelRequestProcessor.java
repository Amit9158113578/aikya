package com.idep.travelquote.req.processor;

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
import com.idep.travelquote.service.impl.ProductPicker;
import com.idep.travelquote.service.impl.ProductValidator;
import com.idep.travelquote.util.CorrelationKeyGenerator;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelRequestProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelRequestProcessor.class.getName());
	CBService serverConfig = null;
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	JsonNode errorNode;
	JsonNode TravelCarrierReqQNode = null;
	JsonNode TravelCarrierResQNode = null;
	JsonNode TravelProductQueryConfig = null;
	String encQuoteId; 

	public void process(Exchange exchange) throws Exception{
		log.info("Inside TravelRequestProcessor");
		try{
			if(this.serverConfig == null){
				this.serverConfig = CBInstanceProvider.getServerConfigInstance();
				this.TravelCarrierReqQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.TRAVEL_CARRIER_Q_LIST).content().getObject(TravelQuoteConstants.REQ_Q_LIST).toString());
				log.info("this.TravelCarrierReqQNode:::::::::::::"+this.TravelCarrierReqQNode);
				this.TravelCarrierResQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.TRAVEL_CARRIER_Q_LIST).content().getObject(TravelQuoteConstants.RES_Q_LIST).toString());
			}

			// Fetching queries from DB required for Travel Quote Calculation.
			this.TravelProductQueryConfig = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.TRAVEL_PRODUCT_QUERY_CONFIG).content().toString());
			log.info("Travel Product Query Config : " + TravelProductQueryConfig);

			List<JsonObject> productsList = null;
			
			// create producer template to send messages to Q
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			// process input request 
			String quotedata = exchange.getIn().getBody().toString();
			
			ProductPicker productPicker = new ProductPicker();
			ProductValidator productValidator = new ProductValidator();

			JsonNode reqNode = this.objectMapper.readTree(quotedata);
			JsonNode quoteParamNode = reqNode.get(TravelQuoteConstants.SERVICE_QUOTE_PARAM);
			//get product details
			//productsList = productPicker.fetchProductsFromDB(quoteParamNode, TravelProductQueryConfig, productService);
			productsList = productPicker.fetchProductsFromDB(reqNode, TravelProductQueryConfig, productService);

			if(productsList.isEmpty()){
				this.log.error("Products not found");
				productsList=null;
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_CODE).asInt());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_MESSAGE).asText());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
				exchange.getIn().setBody(finalresultNode);
			}else{
				// create quote id
				String travelQuoteId = DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.DOCID_CONFIG).get(TravelQuoteConstants.TRAVEL_QUOTE_ID).asText()+this.serverConfig.updateDBSequence(TravelQuoteConstants.TRAVEL_QUOTE_SEQ);
				log.info("travelQuoteId::::::::::::::::::::::"+travelQuoteId);
				this.log.info("Total products fetched from DB : " + productsList.size());
				ArrayNode reqQListNode = objectMapper.createArrayNode();
				ArrayNode finalProductList = productValidator.validateTravelProduct(reqNode, productsList);
				if(finalProductList != null && finalProductList.size() > 0){
					for(JsonNode product : finalProductList){
						try{
							log.info("Finalize product : "+product);
							JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
					    	encQuoteId = GenrateEncryptionKey.GetEncryptedKey(travelQuoteId, keyConfigDoc.get("encryptionKey").asText());
					    	log.info("Encrypted QUOTE_ID :" + encQuoteId);
							((ObjectNode)reqNode).put(TravelQuoteConstants.PRODUCT_INFO, this.objectMapper.readTree(product.toString()));
							ObjectNode objectNode = objectMapper.createObjectNode();
							String carrierReqQName = TravelCarrierReqQNode.get(product.get(TravelQuoteConstants.CARRIER_ID).asText()).textValue();
							objectNode.put(TravelQuoteConstants.INPUT_MESSAGE, reqNode);
							String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
							objectNode.put(TravelQuoteConstants.UNIQUE_KEY, correlationKey);
							objectNode.put(TravelQuoteConstants.QUOTE_ID, travelQuoteId);
						    ((ObjectNode)reqNode).put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
							objectNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
							String uri = TravelQuoteConstants.ACTIVEMQ_QUEUE + carrierReqQName;
							exchange.getIn().setBody(objectNode.toString());
							exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
							template.send(uri, exchange);

							ObjectNode resultNode = objectMapper.createObjectNode();
							resultNode.put(TravelQuoteConstants.QNAME, TravelCarrierResQNode.get(product.get(TravelQuoteConstants.CARRIER_ID).asText()).textValue());
							resultNode.put(TravelQuoteConstants.MESSAGE_ID, correlationKey);
							resultNode.put(TravelQuoteConstants.QUOTE_ID, travelQuoteId);
							resultNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
							resultNode.put(TravelQuoteConstants.STATUS, 0);
							resultNode.put(TravelQuoteConstants.CARRIER_ID, product.get(TravelQuoteConstants.CARRIER_ID).asText());
							reqQListNode.add(resultNode);
						}catch(NullPointerException e){
							this.log.error("product not configured Exception : " + e);
						}catch(Exception e){
							log.error("product not configured Exception : " ,e);
						}
					}

					// prepare UI response
					 if(reqNode.findValue("PROF_QUOTE_ID")!=null){
							log.info("sending request to update PBQ document & request ");
							PBQUpdateRequest.sendPBQUpdateRequest(reqNode, exchange);
					  }
					ObjectNode finalresultNode = objectMapper.createObjectNode();
					finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_CODE).asInt());
					finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_MSG).asText());
					finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, reqQListNode);
					finalresultNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
					finalresultNode.put(TravelQuoteConstants.QUOTE_ID, travelQuoteId);
					exchange.getIn().setBody(finalresultNode);
					this.log.info("TravelQuote Response sent to UI : "+finalresultNode);
				}else{
					this.log.info("After product validation, all products failed.");
					productsList=null;
					ObjectNode finalresultNode = objectMapper.createObjectNode();
					finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE,DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_CODE).asInt());
					finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG,  DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_MESSAGE).asText());
					finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
					exchange.getIn().setBody(finalresultNode);
				}
			}
		}catch(Exception e){
			this.log.error("Exception at TravelRequestProcessor : ", e);
			ObjectNode finalresultNode = objectMapper.createObjectNode();
			finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_CODE).asInt());
			finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_MSG).asText());
			finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(finalresultNode);
		}
	}
}