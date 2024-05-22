 package com.idep.carquote.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.idep.carquote.exception.processor.ExecutionTerminator;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarRequestQProcessor implements Processor {
   static ObjectMapper objectMapper = new ObjectMapper();
   
   static Logger log = Logger.getLogger(CarRequestQProcessor.class.getName());
   
   static JsonNode docConfigNode = (JsonNode)objectMapper.createObjectNode();
   
   static {
     CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
     try {
       docConfigNode = objectMapper.readTree(((JsonObject)serverConfigService.getDocBYId("LogConfiguration").content()).toString());
     } catch (Exception e) {
       log.info("Failed to load Log Config Document" + e);
     } 
   }
   
   public void process(Exchange exchange) throws ExecutionTerminator {
     try {
       log.info("Inside CarRequestQProcessor");
       String message = exchange.getIn().getBody().toString();
       JsonNode qMsgNode = objectMapper.readTree(message);
       log.info("qMsgNode in CarRequestQProcessor: " + qMsgNode);
       String responseCode = qMsgNode.get("responseCode").asText();
       log.info("responseCode is: " + responseCode);
       exchange.getIn().setHeader("responseCode", responseCode);
       exchange.setProperty("correlationId", qMsgNode.get("uniqueKey").textValue());
       exchange.setProperty("carQuoteInputRequest", qMsgNode);
       if (responseCode.equals("P365RES100")) {
         JsonNode qMsgDataNode = qMsgNode.get("data");
         exchange.getIn().setHeader("clientDataFound", "Yes");
         exchange.getIn().setBody(qMsgDataNode);
         exchange.setProperty("defaultLogData", init(qMsgNode));
         exchange.setProperty("QUOTE_ID", qMsgNode.get("QUOTE_ID").textValue());
         exchange.setProperty("encryptedQuoteId", qMsgNode.get("data").get("encryptedQuoteId").textValue());
         exchange.setProperty("logReq", "Car|" + qMsgNode.findValue("carrierId").asText() + "|QUOTE|" + exchange.getProperty("QUOTE_ID").toString() + "|");
       } else {
         exchange.getIn().setHeader("clientDataFound", "No");
         exchange.getIn().setBody(qMsgNode);
       } 
     } catch (Exception e) {
       log.error(CarRequestQProcessor.class + " - " + Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + Thread.currentThread().getStackTrace()[2].getLineNumber() + "- CarRequestQProcessor : " + e);
       throw new ExecutionTerminator();
     } 
   }
   
   public String init(JsonNode quoteReqNode) {
     String logData = new String();
     ArrayNode logNode = (ArrayNode)docConfigNode.get("logFields");
     String seperator = docConfigNode.get("seperateBy").asText();
     try {
       if (docConfigNode.has("defaultValue")) {
         logData = logData.concat(docConfigNode.get("defaultValue").asText());
         logData = logData.concat(seperator);
       } 
       for (JsonNode node : logNode) {
         if (quoteReqNode.findPath(node.asText()) == null) {
           logData = logData.concat(seperator);
           continue;
         } 
         if (node.asText().equalsIgnoreCase("quoteType")) {
           if (docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText()) != null) {
             logData = logData.concat(docConfigNode.get("businessList").get(quoteReqNode.findPath(node.asText()).asText()).asText());
             logData = logData.concat(seperator);
           } 
           continue;
         } 
         logData = logData.concat(quoteReqNode.findPath(node.asText()).asText());
         logData = logData.concat(seperator);
       } 
     } catch (Exception e) {
       log.error("Error occurred while processing logging details ", e);
     } 
     return logData;
   }
 }


