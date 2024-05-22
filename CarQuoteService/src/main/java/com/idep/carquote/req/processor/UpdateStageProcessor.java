 package com.idep.carquote.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.carquote.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class UpdateStageProcessor
   implements Processor
 {
   Logger log = Logger.getLogger(UpdateStageProcessor.class);
   ObjectMapper mapper = new ObjectMapper();
 
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode requestNode = this.mapper.readTree(exchange.getIn().getBody().toString());
       JsonNode configDoc = this.mapper.readTree(exchange.getProperty("configDoc").toString());
       String loopIndex = exchange.getProperty("CamelLoopIndex").toString();
       String stage = null;
       if (configDoc != null) {
         stage = configDoc.get(requestNode.findValue("carrierId").asText()).get("quoteStages").get(loopIndex).asText();
       } else {
         
         this.log.error("error in carrierrequestconfiguration property details not present.");
       } 
       
       if (stage.isEmpty() || stage == null || stage.equalsIgnoreCase("NA")) {
         ObjectNode createObjectNode = this.mapper.createObjectNode();
         createObjectNode.put("carrierId", requestNode.findValue("carrierId").asInt());
         createObjectNode.put("responseCode", "P365RES102");
         createObjectNode.put("message", "configuration document not found ");
         createObjectNode.put("data", "");
         exchange.getIn().setBody(createObjectNode);
         throw new ExecutionTerminator();
       } 
       ((ObjectNode)requestNode).put("stage", stage);
       exchange.setProperty("stage", stage);
       if (configDoc.get(requestNode.findValue("carrierId").asText()).get("webserviceType").asText().equals("REST")) {
         exchange.getIn().setHeader("REST", "Yes");
         exchange.getIn().setHeader("webserviceType", "REST");
         exchange.getIn().setHeader("requestURL", "http://localhost:1201/cxf/restapiservice/config/restcall/integrate/invoke?httpClient.soTimeout=10000");
         exchange.getIn().setBody(requestNode);
       }
       else if (configDoc.get(requestNode.findValue("carrierId").asText()).get("webserviceType").asText().equals("SOAP")) {
         exchange.getIn().setHeader("REST", "No");
         exchange.getIn().setHeader("webserviceType", "SOAP");
         exchange.getIn().setHeader("requestURL", "http://localhost:1201/cxf/soapservice/calculate/soapresponse/integrate/invoke?httpClient.soTimeout=10000");
         exchange.getIn().setBody(requestNode);
       } 
       exchange.setProperty("request", requestNode);
     
     }
     catch (Exception e) {
       this.log.error("error in UpdateStageProcessor class." + e.getMessage());
       throw new ExecutionTerminator();
     } 
   }
 }


