package com.idep.bikequote.req.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.bikequote.util.BikeQuoteConstants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class TransformQuoteRequest implements Processor {
  Logger log = Logger.getLogger(TransformQuoteRequest.class);
  
  ObjectMapper mapper = new ObjectMapper();
  
  public void process(Exchange exchange) throws Exception {
    try {
      ObjectNode requestTransNode = this.mapper.createObjectNode();
      JsonNode requestNode = this.mapper.readTree(exchange.getIn().getBody().toString());
      requestTransNode.put("lob", requestNode.get("productInfo").get("businessLineId"));
      requestTransNode.put("stage", "Quote");
      requestTransNode.put(BikeQuoteConstants.REQUEST_DATA, requestNode);
      exchange.getIn().getHeaders().clear();
      exchange.getIn().setHeader("CamelHttpMethod", "POST");
      exchange.getIn().setHeader("content-type", "application/json");
      exchange.setProperty("webserviceType", requestNode.get("webserviceType").asText());
      exchange.setProperty("stage", "Quote");
      exchange.setProperty(BikeQuoteConstants.UI_BIKEQUOTEREQUEST, requestNode);
      if (requestNode.get("webserviceType").asText().equals("REST")) {
        exchange.getIn().setHeader("webserviceType", "REST");
        exchange.getIn().setHeader("requestURL", "http://localhost:8181/cxf/restapiservice/config/restcall/integrate/invoke?httpClient.soTimeout=10000");
        exchange.getIn().setBody(requestTransNode);
      } else if (requestNode.get("webserviceType").asText().equals("SOAP")) {
        exchange.getIn().setHeader("webserviceType", "REST");
        exchange.getIn().setHeader("requestURL", "http://localhost:8181/cxf/soapservice/calculate/soapresponse/integrate/invoke?httpClient.soTimeout=10000");
        exchange.getIn().setBody(requestTransNode);
      } else {
        this.log.info("web service type not found in the quote request :");
      } 
    } catch (Exception e) {
      e.printStackTrace();
      this.log.error("Exception in transform quote request processor");
    } 
  }
}
