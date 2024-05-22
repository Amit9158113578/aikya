package com.idep.bikequote.ext.form.req;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.exception.processor.ExecutionTerminator;
import com.idep.bikequote.util.BikeQuoteConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class BikeExtServiceReqProcessor implements Processor {
  ObjectMapper objectMapper = new ObjectMapper();
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

  Logger log = Logger.getLogger(BikeExtServiceReqProcessor.class.getName());
  
  JsonNode errorNode;
  
  public void process(Exchange exchange) throws ExecutionTerminator {
    try {
      String quotedata = exchange.getIn().getBody().toString();
      JsonNode reqNode = this.objectMapper.readTree(quotedata);
      JsonNode productInfoNode = reqNode.get(BikeQuoteConstants.PRODUCT_INFO);
      if (reqNode.has(BikeQuoteConstants.SERVICE_QUOTE_PARAM) && reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM).has(BikeQuoteConstants.REQUEST_RIDERS)) {
        ArrayNode finalRiders = this.objectMapper.createArrayNode();
        ArrayNode selectedRiders = (ArrayNode)reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM).get(BikeQuoteConstants.REQUEST_RIDERS);
        double vehicleAge = reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM).get("vehicleAge").doubleValue();
        ObjectNode productriderList = this.objectMapper.createObjectNode();
        for (JsonNode priders : productInfoNode.get("riderDetails"))
          productriderList.put(priders.get(BikeQuoteConstants.RIDER_ID).asText(), priders); 
        ObjectNode processedRiderList = this.objectMapper.createObjectNode();
        ArrayNode eligibleRiders = this.objectMapper.createArrayNode();
        ArrayNode nonEligibleRiders = this.objectMapper.createArrayNode();
        for (JsonNode selRider : selectedRiders) {
          if (productriderList.has(selRider.get(BikeQuoteConstants.RIDER_ID).asText())) {
            JsonNode prodRider = productriderList.get(selRider.get(BikeQuoteConstants.RIDER_ID).asText());
            if (vehicleAge <= prodRider.get("allowedVehicleAge").doubleValue()) {
              eligibleRiders.add(selRider);
              continue;
            } 
            nonEligibleRiders.add(selRider);
            continue;
          } 
          nonEligibleRiders.add(selRider);
        } 
        for (JsonNode riderNode : eligibleRiders) {
          if (processedRiderList.has(riderNode.get(BikeQuoteConstants.RIDER_ID).asText())) {
            this.log.info("rider skipped as it is already processed");
            continue;
          } 
          JsonNode prodRiders = productriderList.get(riderNode.get(BikeQuoteConstants.RIDER_ID).asText());
          processedRiderList.put(riderNode.get(BikeQuoteConstants.RIDER_ID).asText(), "Y");
          finalRiders.add(riderNode);
          ArrayNode dependentRiders = (ArrayNode)prodRiders.get("dependant");
          if (dependentRiders != null)
            for (JsonNode dRiders : dependentRiders) {
              if (processedRiderList.has(dRiders.get(BikeQuoteConstants.RIDER_ID).asText())) {
                this.log.info("dependent rider skipped as it is already processed");
                continue;
              } 
              JsonNode prodRider = productriderList.get(dRiders.get(BikeQuoteConstants.RIDER_ID).asText());
              if (vehicleAge <= prodRider.get("allowedVehicleAge").doubleValue()) {
                ObjectNode newRiders = this.objectMapper.createObjectNode();
                newRiders.put(BikeQuoteConstants.RIDER_ID, dRiders.get(BikeQuoteConstants.RIDER_ID).intValue());
                newRiders.put("riderName", dRiders.get("riderName").textValue());
                newRiders.put("riderAmount", 0);
                processedRiderList.put(dRiders.get(BikeQuoteConstants.RIDER_ID).asText(), "Y");
                finalRiders.add((JsonNode)newRiders);
                continue;
              } 
              nonEligibleRiders.add(dRiders);
            }  
        } 
        ((ObjectNode)reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM)).put(BikeQuoteConstants.REQUEST_RIDERS, (JsonNode)finalRiders);
        ((ObjectNode)reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM)).put("UIRiders", (JsonNode)eligibleRiders);
        ((ObjectNode)reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM)).put("nonEligibleUIRiders", (JsonNode)nonEligibleRiders);
      } 
      if(29==productInfoNode.get(BikeQuoteConstants.DROOLS_CARRIERID).asInt())
      {
    	  if(reqNode.get("vehicleInfo").get("TPPolicyStartDate").textValue()!=null)
    	  {		 
    		  SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    		  SimpleDateFormat optSdf = new SimpleDateFormat("YYYY-MM-dd");
    		  Date startDate = sdf.parse(reqNode.get("vehicleInfo").get("TPPolicyStartDate").textValue());
    		  Date EndDate = sdf.parse(reqNode.get("vehicleInfo").get("TPPolicyExpiryDate").textValue());
    		  ((ObjectNode)reqNode.get("vehicleInfo")).put("TPPolicyStartDate", optSdf.format(startDate));
    		  ((ObjectNode)reqNode.get("vehicleInfo")).put("TPPolicyExpiryDate",  optSdf.format(EndDate));
    	  }
      }
      
      if(25==productInfoNode.get(BikeQuoteConstants.DROOLS_CARRIERID).asInt() && ("OD-TP-2".equals(reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM).get("planType").textValue()) || "OD-TP-3".equals(reqNode.get(BikeQuoteConstants.SERVICE_QUOTE_PARAM).get("planType").textValue())))
      {
    	    String naiplan = this.serverConfig.getDocBYId("CP2-"+productInfoNode.get(BikeQuoteConstants.DROOLS_CARRIERID).asInt()).content().toString();
    	    ArrayNode cp2Plan = (ArrayNode)objectMapper.convertValue(objectMapper.readTree(naiplan).get("NIAPlans"), ArrayNode.class);
    	    ((ObjectNode)reqNode).put("covers",cp2Plan);
      }
      
      exchange.getIn().setBody(this.objectMapper.writeValueAsString(reqNode));
    } catch (NullPointerException e) {
      this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "BIKEEXTSERREQPROCE" + "|ERROR|" + " Exception at BikeExtServiceReqProcessor  :");
      throw new ExecutionTerminator();
    } catch (Exception e) {
      this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "BIKEEXTSERREQPROCE" + "|ERROR|" + " Exception at BikeExtServiceReqProcessor  :", e);
      throw new ExecutionTerminator();
    } 
  }
}
