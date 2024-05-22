 package com.idep.carquote.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class TransformIDVRequest
   implements Processor {
   Logger log = Logger.getLogger(TransformIDVRequest.class);
   
   ObjectMapper mapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       ObjectNode requestTransNode = this.mapper.createObjectNode();
       JsonNode requestNode = this.mapper.readTree(exchange.getIn().getBody().toString());
       requestTransNode.put("lob", requestNode.get("productInfo").get("businessLineId"));
       requestTransNode.put("stage", "IDV");
       requestTransNode.put("request", requestNode);
       exchange.getIn().getHeaders().clear();
       exchange.getIn().setHeader("CamelHttpMethod", "POST");
       exchange.getIn().setHeader("content-type", "application/json");
       exchange.setProperty("webserviceType", requestNode.get("webserviceType").asText());
       exchange.setProperty("stage", "IDV");
       if (requestNode.get("webserviceType").asText().equals("REST")) {
         exchange.getIn().setHeader("REST", "Yes");
         exchange.getIn().setHeader("webserviceType", "REST");
         exchange.getIn().setHeader("requestURL", "http://localhost:8181/cxf/restapiservice/config/restcall/integrate/invoke?httpClient.soTimeout=10000");
         exchange.getIn().setBody(requestTransNode);
       } else if (requestNode.get("webserviceType").asText().equals("SOAP")) {
         exchange.getIn().setHeader("REST", "No");
         exchange.getIn().setHeader("webserviceType", "REST");
         exchange.getIn().setHeader("requestURL", "http://localhost:8181/cxf/soapservice/calculate/soapresponse/integrate/invoke?httpClient.soTimeout=10000");
         exchange.getIn().setBody(requestTransNode);
       } else {
         this.log.info("web service type not found in the idv request :");
       }
     
     } catch (Exception e) {
       e.printStackTrace();
       this.log.error("Exception in transform idv request processor");
     } 
   }
 }


