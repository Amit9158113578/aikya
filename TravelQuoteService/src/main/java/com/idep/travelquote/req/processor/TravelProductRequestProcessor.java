package com.idep.travelquote.req.processor;

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
import com.idep.travelquote.util.CorrelationKeyGenerator;
import com.idep.travelquote.util.TravelQuoteConstants;

/**
 * 
 * @author yogesh.shisode
 *
 */
public class TravelProductRequestProcessor implements Processor{
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(TravelProductRequestProcessor.class.getName());
	CBService serverConfig = null;
	CBService policyTransService = CBInstanceProvider.getPolicyTransInstance();
	CBService productService = CBInstanceProvider.getProductConfigInstance();
	JsonNode errorNode;
	//JsonNode responseConfigNode = null;
	JsonNode TravelCarrierReqQNode=null;
	JsonNode TravelCarrierResQNode=null;
	ObjectNode productCacheList = null;
	String encQuoteId; 

	//JsonObject documentIdConfig = DocumentIDConfigLoad.getDocumentIDConfig().getObject(TravelQuoteConstants.DOCID_CONFIG);

	@Override
	public void process(Exchange exchange) throws Exception{
		this.log.info("Inside of TravelProductRequestProcessor");
		try{
			if(this.serverConfig == null){ 
				this.serverConfig = CBInstanceProvider.getServerConfigInstance();
				this.TravelCarrierReqQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.TRAVEL_CARRIER_Q_LIST).content().getObject(TravelQuoteConstants.REQ_Q_LIST).toString());
				this.TravelCarrierResQNode = this.objectMapper.readTree(this.serverConfig.getDocBYId(TravelQuoteConstants.TRAVEL_CARRIER_Q_LIST).content().getObject(TravelQuoteConstants.RES_Q_LIST).toString());
			}

			// create producer template to send messages to Q
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();

			String quotedata = exchange.getIn().getBody().toString();
			this.log.info("Travel Quote Data :" + quotedata);
			JsonNode reqNode = this.objectMapper.readTree(quotedata);

			// create quote id
			//String travelQuoteId = this.documentIdConfig.getString("travelQuoteId")+this.serverConfig.updateSequence("travelquoteId");
			String travelQuoteId = DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.DOCID_CONFIG).get(TravelQuoteConstants.TRAVEL_QUOTE_ID).asText()+this.serverConfig.updateDBSequence(TravelQuoteConstants.TRAVEL_QUOTE_SEQ);
			JsonDocument productDetails =  this.productService.getDocBYId(TravelQuoteConstants.TRAVEL_PRODUCT + "-" + reqNode.get(TravelQuoteConstants.CARRIER_ID).intValue() + "-" + reqNode.get(TravelQuoteConstants.PRODUCT_ID).intValue());
			ArrayNode reqQListNode = objectMapper.createArrayNode();

			try{
				JsonObject product = productDetails.content();
				ObjectNode objectNode = objectMapper.createObjectNode();
				JsonNode keyConfigDoc = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
		    	encQuoteId = GenrateEncryptionKey.GetEncryptedKey(travelQuoteId, keyConfigDoc.get("encryptionKey").asText());
		    	log.info("Encrypted QUOTE_ID :" + encQuoteId);
				((ObjectNode)reqNode).put(TravelQuoteConstants.PRODUCT_INFO, this.objectMapper.readTree(product.toString()));
				String carrierReqQName = TravelCarrierReqQNode.get(product.getInt(TravelQuoteConstants.DROOLS_CARRIERID).toString()).textValue();
				objectNode.put(TravelQuoteConstants.INPUT_MESSAGE, reqNode);
				String correlationKey = new CorrelationKeyGenerator().getUniqueKey().toString();
				objectNode.put(TravelQuoteConstants.UNIQUE_KEY, correlationKey );
				objectNode.put(TravelQuoteConstants.QUOTE_ID, travelQuoteId );
			    ((ObjectNode)reqNode).put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId );
				objectNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);

				String uri = TravelQuoteConstants.ACTIVEMQ_QUEUE + carrierReqQName;
				exchange.getIn().setBody(objectNode.toString());
				exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
				template.send(uri, exchange);

				ObjectNode resultNode = objectMapper.createObjectNode();
				resultNode.put(TravelQuoteConstants.QNAME, TravelCarrierResQNode.get(product.getInt(TravelQuoteConstants.DROOLS_CARRIERID).toString()).textValue());
				resultNode.put(TravelQuoteConstants.MESSAGE_ID, correlationKey);
				resultNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
				reqQListNode.add(resultNode);

			}catch(NullPointerException e){
				this.log.error("NullPointerException at TravelProductRequestProcessor : " + e);
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_CODE).intValue());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_MESSAGE).textValue());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, reqQListNode);
				exchange.getIn().setBody(finalresultNode);
			}

			if(reqQListNode.size()>0){
				 if(reqNode.findValue("PROF_QUOTE_ID")!=null){
						log.info("sending request to update PBQ document & request ");
						PBQUpdateRequest.sendPBQUpdateRequest(reqNode, exchange);
				  }
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_CODE).intValue());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.SUCC_CONFIG_MSG).textValue());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, reqQListNode);
				finalresultNode.put(TravelQuoteConstants.QUOTE_ID, travelQuoteId);
				finalresultNode.put(TravelQuoteConstants.ENCRYPT_QUOTE_ID, encQuoteId);
				exchange.getIn().setBody(finalresultNode);
				this.log.info("finalresultNode to UI : " + finalresultNode);
			}else{
				ObjectNode finalresultNode = objectMapper.createObjectNode();
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_CODE).intValue());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.TRAVEL_QUOTE_DATA_ERROR_MESSAGE).textValue());
				finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, reqQListNode);
				exchange.getIn().setBody(finalresultNode);
			}
		}catch(Exception e){
			this.log.error("Exception at TravelProductRequestProcessor ",e);
			ObjectNode finalresultNode = objectMapper.createObjectNode();
			finalresultNode.put(TravelQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_CODE).intValue());
			finalresultNode.put(TravelQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(TravelQuoteConstants.RESPONSE_CONFIG_DOC).get(TravelQuoteConstants.ERROR_CONFIG_MSG).textValue());
			finalresultNode.put(TravelQuoteConstants.QUOTE_RES_DATA, this.errorNode);
			exchange.getIn().setBody(finalresultNode);
		}
	}
}