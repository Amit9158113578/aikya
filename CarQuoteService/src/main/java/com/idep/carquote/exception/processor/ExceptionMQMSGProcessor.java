 package com.idep.carquote.exception.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import java.io.IOException;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ExceptionMQMSGProcessor implements Processor {
   Logger log = Logger.getLogger(ExceptionMQMSGProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   JsonNode errorNode;
   
   public void process(Exchange exchange) throws IOException {
     this.log.error("quote service Terminated");
     exchange.getIn().setHeader("JMSCorrelationID", exchange.getProperty("correlationId"));
     try {
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
     } catch (Exception e) {
       ObjectNode objectNode = this.objectMapper.createObjectNode();
       objectNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorCode").asInt());
       objectNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       objectNode.put("data", this.errorNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
     } 
   }
 }


