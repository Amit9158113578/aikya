package com.idep.restapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class RESTXMLRequestFormatter implements Processor {
  Logger log = Logger.getLogger(RESTXMLRequestFormatter.class.getName());
  
  JsonNode inputRequest;
  
  public void process(Exchange exchange) {
    try {
      this.inputRequest = RestAPIConstants.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      String joltResponse = (String)exchange.getIn().getBody(String.class);
      joltResponse = joltResponse.replace("<o>", "").replace("</o>", "");
      log.info("exchange.getProperty(\"RESTXMLConfiguration\") :"+exchange.getProperty("RESTXMLConfiguration"));
      if (exchange.getProperty("RESTXMLConfiguration") != null) {
       JsonNode objectNode1=new ObjectMapper().createObjectNode();
       JsonNode configurations = RestAPIConstants.objectMapper.readTree(exchange.getProperty("RESTXMLConfiguration").toString());
       if (configurations.has(RestAPIConstants.HEADERS)) {
    	   objectNode1 = configurations.get(RestAPIConstants.HEADERS);
        } else {
          ((ObjectNode)objectNode1).put("Content-Type", "application/xml");
          ((ObjectNode)objectNode1).put("CamelHttpMethod", "POST");
          ((ObjectNode)objectNode1).put("CamelAcceptContentType", "application/xml");
        } 
       if(configurations.findValue(RestAPIConstants.CARRIER_ID).asInt()==47)
       {
    	   joltResponse= replaceRequestDetails(joltResponse);
       }
        ObjectNode object = RestAPIConstants.objectMapper.createObjectNode();
        object.put("carrierData", joltResponse.toString());
        object.put("subStage", "Request");
        object.put("inputRequest", this.inputRequest);
        object.put("url", configurations.get("URL").asText());
        object.put(RestAPIConstants.HEADERS, objectNode1);
        exchange.getIn().setBody(object.toString());
      } else {
        this.log.error("InputRequest property is not set");
        exchange.getIn().setHeader("configDocumentFound", "False");
        exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(this.inputRequest.findValue(RestAPIConstants.CARRIER_ID).asInt()));
      } 
    } catch (Exception e) {
      this.log.error("Exception in RESTXMLRequestFormatter", e);
      exchange.getIn().setHeader("configDocumentFound", "False");
      exchange.getIn().setBody(ResponseMessageProcessor.returnConfigDocResponse(this.inputRequest.findValue(RestAPIConstants.CARRIER_ID).asInt()));
    } 
  }
  
	public String replaceRequestDetails(String replaceCarrierData) {
         String replace = replaceCarrierData.replace("<PolicyDetails>", "<PolicyDetails xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
         return replace.replace("<PolicyTenure/>", "<PolicyTenure xsi:nil=\"true\"/>").replace("<IsExistingRGICustomer/>", "<IsExistingRGICustomer xsi:nil=\"true\"/>").replace("<PolicyTenure nil=\"true\"/>", "<PolicyTenure xsi:nil=\"true\"/>").replace("<root>","").replace("</root>","");
	}
	
	
}
