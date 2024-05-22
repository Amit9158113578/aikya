package com.idep.bikequote.req.processor;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.util.BikeQuoteConstants;
import com.idep.bikequote.util.CorrelationKeyGenerator;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;
import com.idep.listener.core.BikeProductValidation;
import com.idep.listener.core.CalculatePolicyDates;
import com.idep.service.quote.cache.BikeQuoteConfigCache;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

public class BikeQuoteReqProcessor implements Processor {
  static ObjectMapper objectMapper = new ObjectMapper();
  
  static Logger log = Logger.getLogger(BikeQuoteReqProcessor.class.getName());
  
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  JsonNode errorNode;
  
  String encQuoteId;
  
  ObjectNode productCacheList = null;
  
  BikeProductValidation validator = new BikeProductValidation();
  
  CalculatePolicyDates dates = new CalculatePolicyDates();
  
  CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
  
  CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
  
  SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
  
  static ObjectNode createObjectNode = objectMapper.createObjectNode();
  
  static {
    List<Map<String, Object>> executeQueryCouchDB1 = CBInstanceProvider.getServerConfigInstance().executeQuery("select REPLACE(make,' ','') || TOSTRING(REPLACE(model,' ','')) || TOSTRING(REPLACE(variant,' ','')) as displayVehicle,variantId from ServerConfig where documentType='bikeVariant'");
    if (executeQueryCouchDB1 == null)
      executeQueryCouchDB1 = CBInstanceProvider.getServerConfigInstance().executeQuery("select REPLACE(make,' ','') || TOSTRING(REPLACE(model,' ','')) || TOSTRING(REPLACE(variant,' ','')) as displayVehicle,variantId from ServerConfig where documentType='bikeVariant'"); 
    ArrayNode convertValue = (ArrayNode)objectMapper.convertValue(executeQueryCouchDB1, ArrayNode.class);
    for (JsonNode objectNode : convertValue)
      createObjectNode.put(objectNode.get("displayVehicle").asText(), objectNode.get("variantId").asText()); 
  }
  
  public void process(Exchange exchange) throws JsonProcessingException {
    try {
      CamelContext camelContext = exchange.getContext();
      ProducerTemplate template = camelContext.createProducerTemplate();
      String quoteData = exchange.getIn().getBody().toString();
      JsonNode reqNode = objectMapper.readTree(quoteData);
      ArrayNode reqQListNode = objectMapper.createArrayNode();
      reqNode = this.dates.calculateDates(reqNode);
      String displayVehicle = String.valueOf(reqNode.findValue("make").asText()) + reqNode.findValue("model").asText() + reqNode.findValue("variant").asText();
      displayVehicle = displayVehicle.replace(" ", "");
      if (createObjectNode.has(displayVehicle)) {
        ((ObjectNode)reqNode.get("vehicleInfo")).put("variantId", createObjectNode.get(displayVehicle).asText());
        log.info("Bike VarientID Found : " + createObjectNode.get(displayVehicle).asText());
      } else {
        log.info("variant id not found for vehicle :" + displayVehicle);
        ObjectNode finalResultNode = objectMapper.createObjectNode();
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES101");
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "variant id not found for the request");
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
        exchange.getIn().setBody(finalResultNode);
      } 
      ArrayNode finalProductList = objectMapper.createArrayNode();
      for (JsonNode product : BikeQuoteConfigCache.getbikeProductsCache()) {
        String validateFlag = this.validator.validateBikeProduct(product, reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM), reqNode.get("vehicleInfo"));
        if (validateFlag.equals("success"))
          finalProductList.add(product); 
      } 
      long updateDBSequence = this.serverConfig.updateDBSequence("SEQBIKEQUOTE");
      if (updateDBSequence == -1L)
        updateDBSequence = this.serverConfig.updateDBSequence("SEQBIKEQUOTE"); 
      String bikeQuoteId = String.valueOf(DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("bikeQuoteId").asText()) + 
        updateDBSequence;
      try {
        if (reqNode.has("calcQuote") && 
          reqNode.get("calcQuote").asBoolean()) {
          String messageId = reqNode.findValue("messageId").asText();
          String docId = "LeadProfile-" + messageId;
          JsonNode defaultLeadProfileDoc = objectMapper.readTree(((JsonObject)this.policyTransaction.getDocBYId(docId).content()).toString());
          ((ObjectNode)defaultLeadProfileDoc).put("latestQUOTE_ID", bikeQuoteId);
          ((ObjectNode)defaultLeadProfileDoc).put("latestQuoteBusinessLineId", 2);
          JsonObject documentContent = JsonObject.fromJson(defaultLeadProfileDoc.toString());
          this.policyTransaction.replaceDocument(docId, documentContent);
        } 
      } catch (Exception e) {
        log.error("exception in updating lead profile for integrating portal ");
      } 
      ArrayNode bikeQuoteResponses = (new BikeQuoteIDVReqProcessor()).getBestQuoteIdDetails(reqNode);
      double userIDV = reqNode.get("vehicleInfo").get("IDV").doubleValue();
      for (JsonNode product : finalProductList) {
        reqNode = (new BikeQuoteIDVReqProcessor()).process(reqNode, userIDV, (JsonNode)bikeQuoteResponses, product);
        ObjectNode objectNode = objectMapper.createObjectNode();
        ((ObjectNode)reqNode).put("productInfo", product);
        ((ObjectNode)reqNode).put(BikeQuoteConstants.QUOTE_ID, bikeQuoteId);
        JsonNode keyConfigDoc = objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId(
              "encryptionPrivateKeyConfig").content()).toString());
        objectNode.put("inputMessage", reqNode);
        String correlationKey = (new CorrelationKeyGenerator()).getUniqueKey().toString();
        objectNode.put("uniqueKey", correlationKey);
        objectNode.put(BikeQuoteConstants.QUOTE_ID, bikeQuoteId);
        objectNode.put("encryptedQuoteId", this.encQuoteId);
        String uri = "activemq:queue:CarrierBikeReqQ";
        exchange.getIn().setBody(objectNode.toString());
        exchange.setPattern(ExchangePattern.InOnly);
        template.send(uri, exchange);
        ObjectNode resultNode = objectMapper.createObjectNode();
        resultNode.put("qname", BikeQuoteConfigCache.getbikeQuoteDocCache().get("BikeCarrierQList")
            .get("carrierResQ").get(product.get(BikeQuoteConstants.DROOLS_CARRIERID).toString()).textValue());
        resultNode.put("messageId", correlationKey);
        resultNode.put(BikeQuoteConstants.QUOTE_ID, bikeQuoteId);
        resultNode.put("encryptedQuoteId", this.encQuoteId);
        resultNode.put(BikeQuoteConstants.DROOLS_CARRIERID, product.get(BikeQuoteConstants.DROOLS_CARRIERID).asInt());
        resultNode.put("status", 0);
        reqQListNode.add((JsonNode)resultNode);
      } 
      if (reqQListNode.size() > 0) {
        ObjectNode finalResultNode = objectMapper.createObjectNode();
        ObjectNode policyDate = objectMapper.createObjectNode();
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES100");
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "success");
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_DATA, (JsonNode)reqQListNode);
        finalResultNode.put("businessLineId", 2);
        if (reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM).get("policyType").textValue().equals("renew")) {
          policyDate.put("PreviousPolicyStartDate", reqNode.get("vehicleInfo").get("PreviousPolicyStartDate").textValue());
          policyDate.put("PreviousPolicyExpiryDate", reqNode.get("vehicleInfo").get("PreviousPolicyExpiryDate").textValue());
        } 
        policyDate.put("policyStartDate", reqNode.get("systemPolicyStartDate").get("sysPolicyStartDate").textValue());
        policyDate.put("policyEndDate", reqNode.get("systemPolicyStartDate").get("sysPolicyEndDate").textValue());
        finalResultNode.put("policyDate", (JsonNode)policyDate);
        finalResultNode.put(BikeQuoteConstants.QUOTE_ID, bikeQuoteId);
        finalResultNode.put("encryptedQuoteId", this.encQuoteId);
        LeadProfileRequest.sendLeadProfileRequest(finalResultNode, exchange);
        ObjectNode BikeQuoteRequest = objectMapper.createObjectNode();
        BikeQuoteRequest.put("bikeQuoteRequest", reqNode);
        BikeQuoteRequest.put("documentType", "bikeQuoteResults");
        BikeQuoteRequest.put("businessLineId", 2);
        BikeQuoteRequest.put("quoteCreatedDate", this.dateFormat.format(new Date()));
        BikeQuoteRequest.put(BikeQuoteConstants.QUOTE_ID, bikeQuoteId);
        BikeQuoteRequest.put("carrierTransformedReq", (JsonNode)objectMapper.createObjectNode());
        log.info("before creating QUOTE_ID in BikeQuote RequestNode : " + BikeQuoteRequest);
        ((ObjectNode)BikeQuoteRequest.get("bikeQuoteRequest")).remove("productInfo");
        CBService bucketInstance = CBInstanceProvider.getBucketInstance("QuoteData");
        String status = bucketInstance.createDocument(bikeQuoteId, JsonObject.fromJson(BikeQuoteRequest.toString()));
        log.info("QuoteId is created in BikeQuote Request : " + bikeQuoteId + " Status  is : " + status);
        exchange.getIn().setBody(finalResultNode);
      } else {
        ObjectNode finalResultNode = objectMapper.createObjectNode();
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(BikeQuoteConstants.RESPONSE_CONFIG_DOC)
            .get("bikeQuoteDataErrorCode").asText());
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(BikeQuoteConstants.RESPONSE_CONFIG_DOC)
            .get("bikeQuoteDataErrorMessage").asText());
        finalResultNode.put(BikeQuoteConstants.QUOTE_RES_DATA, (JsonNode)reqQListNode);
        exchange.getIn().setBody(finalResultNode);
      } 
    } catch (Exception e) {
      log.error("Exception at BikeQuoteReqProcessor ", e);
      ObjectNode finalResultNode = objectMapper.createObjectNode();
      finalResultNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES101");
      finalResultNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "calculate quote failure for insurer");
      finalResultNode.put(BikeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
      exchange.getIn().setBody(finalResultNode);
    } 
  }
}
