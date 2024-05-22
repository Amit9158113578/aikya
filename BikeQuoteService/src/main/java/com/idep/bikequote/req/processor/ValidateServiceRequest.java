package com.idep.bikequote.req.processor;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.response.ResponseQueueProcessor;
import com.idep.bikequote.util.BikeQuoteConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ValidateServiceRequest implements Processor {
  Logger log = Logger.getLogger(ValidateServiceRequest.class);
  
  ObjectMapper mapper = new ObjectMapper();
  
  static JsonDocument docBYId = null;
  
  static {
    CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
    docBYId = serverConfig.getDocBYId("CarrierRequestConfiguration");
  }
  
  public void process(Exchange exchange) throws Exception {
    try {
      JsonNode requestNode = this.mapper.readTree(exchange.getIn().getBody().toString());
      if (docBYId == null) {
        ObjectNode createObjectNode = this.mapper.createObjectNode();
        createObjectNode.put(BikeQuoteConstants.DROOLS_CARRIERID, requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asInt());
        createObjectNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES102");
        createObjectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "configuration document not found ");
        createObjectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, "");
        Exchange exchange2 = null;
        exchange2.getIn().setBody(createObjectNode);
        (new ResponseQueueProcessor()).process(exchange2);
      } else {
        JsonNode config_Document = this.mapper.readTree(((JsonObject)docBYId.content()).toString());
        if (!config_Document.has(requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText())) {
          ObjectNode createObjectNode = this.mapper.createObjectNode();
          createObjectNode.put(BikeQuoteConstants.DROOLS_CARRIERID, requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asInt());
          createObjectNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES102");
          createObjectNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "configuration details not found for carrierId " + requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText());
          createObjectNode.put(BikeQuoteConstants.QUOTE_RES_DATA, "");
          Exchange exchange2 = null;
          exchange2.getIn().setBody(createObjectNode);
          (new ResponseQueueProcessor()).process(exchange2);
        } else {
          JsonNode carrier_config_details = config_Document.get(requestNode.findValue(BikeQuoteConstants.DROOLS_CARRIERID).asText());
          ObjectNode requestTransNode = this.mapper.createObjectNode();
          exchange.getIn().getHeaders().clear();
          exchange.getIn().setHeader("CamelHttpMethod", "POST");
          exchange.getIn().setHeader("content-type", "application/json");
          if (requestNode.has("isPolicyRenewed") && requestNode.get("isPolicyRenewed").asBoolean()) {
            this.log.info("Policy Renewal flow" + requestNode);
            String formatedRegistration = createFormatedRegNumber(requestNode.get("vehicleInfo").get("registrationNumber").asText());
            ((ObjectNode)requestNode.get("vehicleInfo")).put("registrationNumberFormated", formatedRegistration);
          } 
          if (carrier_config_details.get(BikeQuoteConstants.NO_SERVICE_INVOKE).get("quote").asInt() > 0) {
              exchange.getIn().setHeader(BikeQuoteConstants.SERVICE_INVOKE, "True");
              exchange.setProperty("configDoc", config_Document);
              exchange.getIn().setHeader(BikeQuoteConstants.NO_SERVICE_INVOKE, Integer.valueOf(carrier_config_details.get(BikeQuoteConstants.NO_SERVICE_INVOKE).get("quote").asInt()));
              requestTransNode.put("lob", requestNode.get("productInfo").get("businessLineId"));
              requestTransNode.put(BikeQuoteConstants.REQUEST_DATA, requestNode);
              exchange.getIn().setBody(requestTransNode);
            }
          else
          {
        	  exchange.getIn().setHeader(BikeQuoteConstants.SERVICE_INVOKE, "False");
        	  exchange.getIn().setBody(requestNode);
          }
          exchange.setProperty(BikeQuoteConstants.UI_BIKEQUOTEREQUEST, this.mapper.writeValueAsString(requestNode));
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("error in validate service request processor :");
    } 
  }
  
  public String createFormatedRegNumber(String registrationNumber) {
    try {
      String[] split = registrationNumber.split("([0-9]+)");
      String[] split2 = registrationNumber.split("([A-Z]+)");
      String formatedRegistration = String.valueOf(split[0]) + "-" + split2[1] + "-" + split[1] + "-" + split2[2];
      return formatedRegistration;
    } catch (Exception e) {
      this.log.error("create formated number method error");
      return registrationNumber;
    } 
  }
}
