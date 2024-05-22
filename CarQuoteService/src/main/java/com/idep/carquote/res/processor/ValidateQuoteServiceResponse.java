 package com.idep.carquote.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.carquote.exception.processor.ExecutionTerminator;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ValidateQuoteServiceResponse
   implements Processor
 {
   Logger log = Logger.getLogger(ValidateQuoteServiceResponse.class);
   
   ObjectMapper mapper = new ObjectMapper();
   
   public void process(Exchange exchange) throws Exception {
     try {
       String response = exchange.getIn().getBody().toString();
       JsonNode responseNode = this.mapper.readTree(response);
       if (response.contains("No service was found.")) {
         ObjectNode failure = this.mapper.createObjectNode();
         failure.put("responseCode", "P365RES103");
         failure.put("message", "invoke service down");
         failure.put("data", "error");
         exchange.getIn().setBody(responseNode);
       } 
       if (responseNode.has("responseCode")) {
         JsonNode configDoc = this.mapper.readTree(exchange.getProperty("configDoc").toString());
         if (responseNode.get("responseCode").asText().equals(DocumentDataConfig.getConfigDocList()
             .get("ResponseMessages").get("ResponseCodeSuccess").asText())) {
           
           JsonNode inputRequest = this.mapper.readTree(exchange.getProperty("request").toString());
           if (Integer.parseInt(exchange.getProperty("CamelLoopIndex").toString()) < 
             Integer.parseInt(exchange.getIn().getHeader("noOfServiceInvoke").toString()) - 1)
             responseNode = updateQuoteReqDetails(responseNode, inputRequest, configDoc); 
           exchange.getIn().setBody(responseNode);
         } else {
           exchange.getIn().setHeader("invokeservice", "False");
           responseNode = validateCarrierErrorResponse(responseNode, configDoc);
           exchange.getIn().setBody(responseNode);
         } 
       } else {
         exchange.getIn().setHeader("invokeservice", "False");
         ObjectNode failure = this.mapper.createObjectNode();
         failure.put("responseCode", "P365RES103");
         failure.put("message", "invoke service down");
         failure.put("data", "error");
         exchange.getIn().setBody(responseNode);
       } 
     } catch (Exception e) {
       e.printStackTrace();
       this.log.error("Exception in quote response processor");
     } 
   }
 
   
   public JsonNode updateQuoteReqDetails(JsonNode response, JsonNode inputRequest, JsonNode docConfig) throws Exception {
     ObjectNode updateProposalRequest = this.mapper.createObjectNode();
     try {
       updateProposalRequest.put("carrierId", inputRequest.findValue("carrierId").asText());
       updateProposalRequest.put("productId", inputRequest.findValue("productId").asText());
       updateProposalRequest.put("QUOTE_ID", inputRequest.findValue("QUOTE_ID").asText());
       updateProposalRequest.put(String.valueOf(inputRequest.get("stage").asText()) + "Response", response.get("data"));
       if (docConfig != null) {
         
         if (docConfig.get(inputRequest.findValue("carrierId").asText()).has("insurerReqDetails")) {
           ArrayNode proposalUpdateReqDetails = (ArrayNode)docConfig.get(inputRequest.findValue("carrierId").asText()).get("insurerReqDetails");
           for (JsonNode key : proposalUpdateReqDetails)
             updateProposalRequest.put(key.asText(), inputRequest.findValue(key.asText())); 
         } 
         if (docConfig.has("commonQuoteInputReqDetails")) {
           ArrayNode proposalUpdateReqDetails = (ArrayNode)docConfig.get("commonQuoteInputReqDetails");
           for (JsonNode key : proposalUpdateReqDetails)
             updateProposalRequest.put(key.asText(), inputRequest.findValue(key.asText())); 
         } 
         ObjectNode responseNode = this.mapper.createObjectNode();
         responseNode.put("lob", inputRequest.findValue("businessLineId").asInt());
         responseNode.put("request", (JsonNode)updateProposalRequest);
         return (JsonNode)responseNode;
       } 
       ((ObjectNode)inputRequest.get("request")).put(String.valueOf(inputRequest.get("stage").asText()) + "Response", response.get("data"));
     } catch (Exception e) {
       ObjectNode createObjectNode = this.mapper.createObjectNode();
       createObjectNode.put("carrierId", inputRequest.findValue("carrierId").asInt());
       createObjectNode.put("responseCode", "P365RES102");
       createObjectNode.put("message", "exception in update stage request details method ");
       createObjectNode.put("data", e.getMessage());
       throw new ExecutionTerminator();
     } 
     return inputRequest;
   }
   
   public JsonNode validateCarrierErrorResponse(JsonNode failureNode, JsonNode docConfig) throws Exception {
     try {
       if (!failureNode.has("carrierId"))
         return failureNode; 
       if (docConfig.has("carrierErrorResponse")) {
         ObjectNode dataNode = this.mapper.createObjectNode();
         ArrayNode carrierErrorResponse = (ArrayNode)docConfig.get("carrierErrorResponse");
         JsonNode data = failureNode.get("data");
         for (JsonNode key : carrierErrorResponse) {
           if (data.findValue(key.asText()) != null) {
             JsonNode value = data.findValue(key.asText());
             String nodeType = value.getClass().getSimpleName();
             if (nodeType.equalsIgnoreCase("ArrayNode")) {
               dataNode.put(key.asText(), value);
               continue;
             } 
             String valueStr = data.findValue(key.asText()).asText();
             dataNode.put(key.asText(), valueStr);
           } 
         } 
         ((ObjectNode)failureNode).put("data", (JsonNode)dataNode);
       } else {
         return failureNode;
       } 
     } catch (Exception e) {
       this.log.error("Error in validateCarrierErrorResponse method :" + e.toString());
     } 
     return failureNode;
   }
 }


