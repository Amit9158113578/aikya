package com.idep.lifequote.req.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.encryption.session.GenrateEncryptionKey;
import com.idep.lifequote.util.CorrelationKeyGenerator;
import com.idep.lifequote.util.LifeQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class LifeProductRequestProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(LifeProductRequestProcessor.class.getName());
	CBService serverConfig = null;
	CBService policyTransService = CBInstanceProvider.getPolicyTransInstance();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	JsonNode errorNode;
	//JsonNode responseConfigNode = null;
	JsonNode LifeCarrierReqQNode=null;
	JsonNode LifeCarrierResQNode=null;
	ObjectNode productCacheList = null;
	String encQuoteId; 
	JsonNode keyConfigDoc;
	//JsonObject documentIdConfig = DocumentIDConfigLoad.getDocumentIDConfig().getObject(LifeQuoteConstants.DOCID_CONFIG);

	@Override
	public void process(Exchange exchange) throws Exception{
		try{
			if(this.serverConfig == null){ 
				this.serverConfig = CBInstanceProvider.getServerConfigInstance();
				this.LifeCarrierReqQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.LIFE_CARRIERS_Q).content().getObject(LifeQuoteConstants.REQ_Q_LIST).toString());
				this.LifeCarrierResQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(LifeQuoteConstants.LIFE_CARRIERS_Q).content().getObject(LifeQuoteConstants.RES_Q_LIST).toString());
				keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
			}

			// create producer template to send messages to Q
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();

			String quotedata = exchange.getIn().getBody().toString();
			this.log.info("Life Quote Data :" + quotedata);
			JsonNode reqNode = this.objectMapper.readTree(quotedata);

			// create quote id
			//String lifeQuoteId = this.documentIdConfig.getString("lifeQuoteId")+this.serverConfig.updateSequence("lifequoteId");
			String lifeQuoteId = DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.DOCID_CONFIG).get("lifeQuoteId").asText()+this.serverConfig.updateDBSequence("SEQCARQUOTE");
			JsonDocument productDetails =  this.productService.getDocBYId("LifeProduct-"+reqNode.get("carrierId").intValue()+"-"+reqNode.get("productId").intValue());
			ArrayNode reqQListNode = objectMapper.createArrayNode();

			try{
				JsonObject product = productDetails.content();
				ObjectNode objectNode = objectMapper.createObjectNode();
				((ObjectNode)reqNode).put(LifeQuoteConstants.PRODUCT_INFO, this.objectMapper.readTree(product.toString()));
				String carrierReqQName = LifeCarrierReqQNode.get(product.getInt(LifeQuoteConstants.DROOLS_CARRIERID).toString()).textValue();
				objectNode.put(LifeQuoteConstants.INPUT_MESSAGE, reqNode);
				String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
				objectNode.put(LifeQuoteConstants.UNIQUE_KEY, correlationKey );
				objectNode.put(LifeQuoteConstants.QUOTE_ID, lifeQuoteId );
				encQuoteId = GenrateEncryptionKey.GetEncryptedKey(lifeQuoteId, keyConfigDoc.get("encryptionKey").asText());
				log.info("Encrypted Life QUOTE_ID :" + encQuoteId);
				objectNode.put(LifeQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
				String uri = "activemq:queue:"+carrierReqQName;
				exchange.getIn().setBody(objectNode.toString());
				exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
				template.send(uri, exchange);

				ObjectNode resultNode = objectMapper.createObjectNode();
				resultNode.put(LifeQuoteConstants.QNAME, LifeCarrierResQNode.get(product.getInt(LifeQuoteConstants.DROOLS_CARRIERID).toString()).textValue());
				resultNode.put(LifeQuoteConstants.MESSAGE_ID, correlationKey);
				reqQListNode.add(resultNode);

			}catch(NullPointerException e){
				this.log.error("NullPointerException at LifeProductRequestProcessor : " + e);
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorCode").intValue());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorMessage").textValue());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, reqQListNode);
				exchange.getIn().setBody(finalresultNode);
			}

			if(reqQListNode.size()>0){
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.SUCC_CONFIG_CODE).intValue());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get(LifeQuoteConstants.SUCC_CONFIG_MSG).textValue());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, reqQListNode);
				finalresultNode.put(LifeQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
				finalresultNode.put(LifeQuoteConstants.QUOTE_ID, lifeQuoteId);
				LeadProfileRequest.sendLeadProfileRequest(finalresultNode, exchange);
				exchange.getIn().setBody(finalresultNode);
				this.log.debug("finalresultNode to UI : " + finalresultNode);
			}else{
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorCode").intValue());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("carQuoteDataErrorMessage").textValue());
				finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, reqQListNode);
				exchange.getIn().setBody(finalresultNode);
			}
		}catch(Exception e){
			this.log.error("Exception at LifeProductRequestProcessor ",e);
			ObjectNode finalresultNode = objectMapper.createObjectNode();
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("errorCode").intValue());
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(LifeQuoteConstants.RESPONSE_CONFIG_DOC).get("errorMessage").textValue());
			finalresultNode.put(LifeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(finalresultNode);
		}
	}
}