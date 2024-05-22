package com.idep.restapi.utils;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.idep.request.validate.impl.ValidateJsonImpl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class CarrierRequestValidator implements Processor {
  String docId = null;
  
  Logger log = Logger.getLogger(CarrierRequestValidator.class.getName());
  
  public void process(Exchange exchange) {
    int carrierId = 0;
	int productId;
    try {
      String request = (String)exchange.getIn().getBody(String.class);
      JsonNode reqNode = RestAPIConstants.objectMapper.readTree(request);
      JsonDocument loadedDoc = null;
      JsonNode inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      this.log.info("input request in CarrierRequestValidator : " + reqNode);
      int lob = inputRequest.findValue("lob").asInt();
      carrierId = inputRequest.findValue("carrierId").asInt();
      if (lob == 4) {
			productId = inputRequest.findValue("planId").asInt();
		} else {
			productId = inputRequest.findValue("productId").asInt();
		}
      String stage = inputRequest.findValue("stage").asText();
      JsonNode policyType = inputRequest.findValue("policyType");
      if (policyType != null) {
        this.docId = "SchemaValidation-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-" + policyType.asText();
        loadedDoc = RestAPIConstants.serverConfig.getDocBYId(this.docId);
      } else {
        this.docId = "SchemaValidation-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
        loadedDoc = RestAPIConstants.serverConfig.getDocBYId(this.docId);
      } 
      if (loadedDoc != null) {
        this.log.info("inside schemaValidation");
        ValidateJsonImpl carrierValidations = new ValidateJsonImpl();
        JsonNode resNode = carrierValidations.parseCarrierJson(reqNode, ((JsonObject)loadedDoc.content()).toString());
        this.log.info("schemaValidation completed");
        if (resNode.has("responseCode") && resNode.get("responseCode").asText().equals("P365RES100")) {
          exchange.getIn().setBody(reqNode);
        } else {
          this.log.info("not A valid response");
          exchange.getIn().setHeader("successRes", "False");
          exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
        } 
      } else {
        exchange.getIn().setBody(reqNode);
      } 
    } catch (Exception e) {
      this.log.error("Exception at CarrierRequestValidator : " + e);
      exchange.getIn().setHeader("successRes", "False");
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(carrierId));
    } 
  }
}
