package com.idep.bikequote.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.util.BikeQuoteConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.data.searchconfig.cache.DocumentDataConfig;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.crypto.Data;

import org.apache.log4j.Logger;

public class BikeQuoteIDVReqProcessor {
  ObjectMapper objectMapper = new ObjectMapper();
  
  Logger log = Logger.getLogger(BikeQuoteIDVReqProcessor.class.getName());
  
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  JsonNode errorNode;
  
  String encQuoteId;
  
  CBService quoteData = CBInstanceProvider.getBucketInstance("QuoteData");
  
  public JsonNode process(JsonNode reqNode, double userIDV, JsonNode bikeQuoteResponses, JsonNode product) throws Exception {
    try {
      if (bikeQuoteResponses != null) {
        double systemIDV = 0.0D;
        for (JsonNode quoteResponse : bikeQuoteResponses) {
          if (product.get(BikeQuoteConstants.DROOLS_CARRIERID).intValue() == quoteResponse.get(BikeQuoteConstants.DROOLS_CARRIERID).intValue()) {
            if (userIDV <= quoteResponse.get("minIdvValue").asDouble()) {
              systemIDV = quoteResponse.get("minIdvValue").asLong();
            } else if (userIDV >= quoteResponse.get("maxIdvValue").asDouble()) {
              systemIDV = quoteResponse.get("maxIdvValue").asLong();
            } else if (userIDV >= quoteResponse.get("minIdvValue").asDouble() && 
              userIDV <= quoteResponse.get("maxIdvValue").asDouble()) {
              systemIDV = userIDV;
            } 
            ((ObjectNode)reqNode.get("vehicleInfo")).put("IDV", systemIDV);
            if(29==product.get(BikeQuoteConstants.DROOLS_CARRIERID).intValue())
            {
            	calculateICICIIDV(reqNode);
            }
            return reqNode;
          } 
        } 
      } else {
        return reqNode;
      } 
    } catch (Exception e) {
      this.log.error("Exception at BikeQuoteIDVReqProcessor ", e);
      ObjectNode objectNode = this.objectMapper.createObjectNode();
      objectNode.put(BikeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(BikeQuoteConstants.RESPONSE_CONFIG_DOC).get(BikeQuoteConstants.ERROR_CONFIG_CODE).asInt());
      objectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(BikeQuoteConstants.RESPONSE_CONFIG_DOC).get(BikeQuoteConstants.ERROR_CONFIG_MSG).asText());
      objectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
    } 
    return reqNode;
  }
  
  private JsonNode calculateICICIIDV(JsonNode reqNode) throws ParseException {
	  final DecimalFormat decfor = new DecimalFormat("0"); 
	  double idv=reqNode.get("vehicleInfo").get("IDV").asInt()/0.7;
	   log.info("icici calculated idv :"+decfor.format(idv));
	  ((ObjectNode)reqNode.get("vehicleInfo")).put("IDV", decfor.format(idv));
	   return reqNode;
	
}

public ArrayNode getBestQuoteIdDetails(JsonNode reqNode) throws Exception {
    ArrayNode bikeQuoteResponses = null;
    try {
      if (reqNode.get("vehicleInfo").get("IDV").asInt() > 0)
        if (reqNode.get("vehicleInfo").has("best_quote_id") && !reqNode.get("vehicleInfo").get("best_quote_id").asText().isEmpty()) {
          JsonDocument quoteDoc = this.quoteData.getDocBYId(reqNode.get("vehicleInfo").get("best_quote_id").textValue());
          if (quoteDoc == null)
            this.log.error("best_quote_id document details not found in database :" + reqNode.get("vehicleInfo").get("best_quote_id").textValue()); 
          bikeQuoteResponses = (ArrayNode)this.objectMapper.readTree(((JsonObject)quoteDoc.content()).toString()).get("bikeQuoteResponse");
        }  
    } catch (Exception e) {
      this.log.error("Exception at getBestQuoteIdDetails ", e);
      ObjectNode objectNode = this.objectMapper.createObjectNode();
      objectNode.put(BikeQuoteConstants.QUOTE_RES_CODE, DocumentDataConfig.getConfigDocList().get(BikeQuoteConstants.RESPONSE_CONFIG_DOC).get(BikeQuoteConstants.ERROR_CONFIG_CODE).asInt());
      objectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, DocumentDataConfig.getConfigDocList().get(BikeQuoteConstants.RESPONSE_CONFIG_DOC).get(BikeQuoteConstants.ERROR_CONFIG_MSG).asText());
      objectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, this.errorNode);
    } 
    return bikeQuoteResponses;
  }
}
