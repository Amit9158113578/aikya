package com.idep.bikequote.response;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.util.BikeQuoteConstants;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class ValidateIDVServiceResponse implements Processor {
  Logger log = Logger.getLogger(ValidateIDVServiceResponse.class);
  
  ObjectMapper mapper = new ObjectMapper();
  
  public void process(Exchange exchange) throws Exception {
    try {
      String response = exchange.getIn().getBody().toString();
      if (response.contains("No service was found.")) {
        ObjectNode responseNode = this.mapper.createObjectNode();
        responseNode.put(BikeQuoteConstants.QUOTE_RES_CODE, "P365RES103");
        responseNode.put(BikeQuoteConstants.QUOTE_RES_MSG, "invoke service down");
        responseNode.put(BikeQuoteConstants.QUOTE_RES_DATA, "error");
        exchange.getIn().setBody(responseNode);
      } else {
        JsonNode requestNode = this.mapper.readTree(response);
        if (requestNode.has(BikeQuoteConstants.QUOTE_RES_CODE)) {
          if (requestNode.get(BikeQuoteConstants.QUOTE_RES_CODE).asText().equals("P365RES100")) {
            JsonNode bikeQuoteRequest = this.mapper.readTree(exchange.getProperty(BikeQuoteConstants.UI_BIKEQUOTEREQUEST).toString());
            ((ObjectNode)bikeQuoteRequest).put("response_IDV", requestNode.get(BikeQuoteConstants.QUOTE_RES_DATA));
            exchange.getIn().setHeader("Quote", "Yes");
            exchange.getIn().setBody(bikeQuoteRequest);
          } else {
            exchange.getIn().setHeader("Quote", "No");
            CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
            JsonNode configDoc = this.mapper.readTree(((JsonObject)serverConfig.getDocBYId("CarrierRequestConfiguration").content()).toString());
            requestNode = (new ValidateQuoteServiceResponse()).validateCarrierErrorResponse(requestNode, configDoc);
            exchange.getIn().setBody(requestNode);
          } 
        } else {
          this.log.info("response not found from rest/soap service");
          exchange.getIn().setBody(requestNode);
        } 
      } 
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("Exception in transform idv request processor");
    } 
  }
}
