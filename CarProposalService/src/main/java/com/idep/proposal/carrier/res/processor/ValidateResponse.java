 package com.idep.proposal.carrier.res.processor;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.SHA512Encryption;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.camel.RuntimeCamelException;
 import org.apache.log4j.Logger;
 
 public class ValidateResponse implements Processor {
   Logger log = Logger.getLogger(ValidateResponse.class);
   
   public void process(Exchange exchange) throws Exception {
     String response = exchange.getIn().getBody().toString();
     JsonNode responseNode = Utils.mapper.readTree(response);
     try {
       if (response.contains("No service was found."))
         exchange.getIn().setBody((new ExceptionResponse()).invokeServiceDown()); 
       if (responseNode.has("responseCode")) {
         JsonNode configDoc = Utils.mapper.readTree(exchange.getProperty("configDoc").toString());
         if (responseNode.get("responseCode").asText().equals(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText())) {
           if (exchange.getIn().getHeader("checkInspectionStatus") != null) {
             if (exchange.getIn().getHeader("checkInspectionStatus").toString().equalsIgnoreCase("Y"))
               if (responseNode.get("data").has("status") && responseNode.get("data").get("status").asText().equalsIgnoreCase("Recommended")) {
                 this.log.info("Your Inspection was Recommended");
                 exchange.getIn().removeHeader("checkInspectionStatus");
                 exchange.getIn().setBody(exchange.getProperty("PostInspectionRequest"));
               } else {
                 this.log.info("Your inspection Status is pending");
                 exchange.getIn().removeHeader("checkInspectionStatus");
                 ((ObjectNode)responseNode).put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseInspectionError").asText());
                 ((ObjectNode)responseNode).put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("InspectionPendingStatus").asText());
                 exchange.getIn().setBody(responseNode.toString());
                 throw new RuntimeCamelException();
               }  
           } else {
             JsonNode inputRequest = Utils.mapper.readTree(exchange.getProperty("request").toString());
             JsonNode SHA512HASHKEY = responseNode.findValue("SHA512HASHKEY");
             if (SHA512HASHKEY != null) {
               String checksum = SHA512Encryption.encryptThisString(SHA512HASHKEY.asText());
               this.log.info("checksum generated :" + checksum);
               ((ObjectNode)responseNode.get("data")).put("hashCode", checksum);
             } 
             if (Integer.parseInt(exchange.getProperty("CamelLoopIndex").toString()) < Integer.parseInt(exchange.getIn().getHeader("noOfServiceInvoke").toString()) - 1) {
               this.log.info("index loop N0 for updating proposal : " + (Integer.parseInt(exchange.getIn().getHeader("noOfServiceInvoke").toString()) - 1));
               responseNode = updateProposalReqDetails(responseNode, inputRequest, configDoc);
             } 
             exchange.getIn().setBody(responseNode.toString());
           } 
         } else {
           exchange.getIn().setHeader("invokeservice", "False");
           responseNode = validateCarrierErrorResponse(responseNode, configDoc);
           exchange.getIn().setBody(responseNode);
         } 
       } else {
         exchange.getIn().setHeader("invokeservice", "False");
         exchange.getIn().setBody((new ExceptionResponse()).properResponseNotFound(responseNode));
       } 
     } catch (JsonParseException jp) {
       exchange.getIn().setBody((new ExceptionResponse()).parseException(jp.getMessage()));
     } catch (Exception e) {
       exchange.getIn().setBody(responseNode.toString());
       throw new RuntimeCamelException();
     } 
   }
   
   public JsonNode validateCarrierErrorResponse(JsonNode failureNode, JsonNode docConfig) throws Exception {
     try {
       if (!failureNode.has("carrierId"))
         return failureNode; 
       JsonNode responseDoc = docConfig.get(failureNode.get("carrierId").asText());
       if (responseDoc.has("carrierErrorResponse")) {
         ObjectNode dataNode = Utils.mapper.createObjectNode();
         ArrayNode carrierErrorResponse = (ArrayNode)responseDoc.get("carrierErrorResponse");
         JsonNode data = failureNode.get("data");
         for (JsonNode key : carrierErrorResponse) {
           JsonNode value = data.findValue(key.asText());
           String nodeType = value.getClass().getSimpleName();
           if (nodeType.equalsIgnoreCase("ArrayNode")) {
             dataNode.put(key.asText(), value);
             continue;
           } 
           String valueStr = data.findValue(key.asText()).asText();
           dataNode.put(key.asText(), valueStr);
         } 
         ((ObjectNode)failureNode).put("data", (JsonNode)dataNode);
       } else {
         return failureNode;
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("error in proposal validate response processor :");
       (new ExecutionTerminator()).execution(failure);
     } 
     return failureNode;
   }
   
   public JsonNode updateProposalReqDetails(JsonNode response, JsonNode inputRequest, JsonNode docConfig) throws Exception {
     ObjectNode updateProposalRequest = Utils.mapper.createObjectNode();
     try {
       updateProposalRequest.put("proposalId", inputRequest.findValue("proposalId").asText());
       updateProposalRequest.put("carrierId", inputRequest.findValue("carrierId").asText());
       updateProposalRequest.put("productId", inputRequest.findValue("productId").asText());
       updateProposalRequest.put("productId", inputRequest.findValue("productId").asText());
       updateProposalRequest.put("QUOTE_ID", inputRequest.findValue("QUOTE_ID").asText());
       updateProposalRequest.put("encryptedProposalId", inputRequest.findValue("encryptedProposalId").asText());
       updateProposalRequest.put("proposalResponse", response.get("data"));
       if (docConfig.get(inputRequest.findValue("carrierId").asText()) != null) {
         if (docConfig.get(inputRequest.findValue("carrierId").asText()).has("proposalUpdateReqDetails")) {
           ArrayNode proposalUpdateReqDetails = (ArrayNode)docConfig.get(inputRequest.findValue("carrierId").asText()).get("proposalUpdateReqDetails");
           for (JsonNode key : proposalUpdateReqDetails)
             updateProposalRequest.put(key.asText(), inputRequest.findValue(key.asText())); 
         } else {
           ((ObjectNode)inputRequest.get("request")).put("proposalResponse", response.get("data"));
           return inputRequest;
         } 
         ObjectNode responseNode = Utils.mapper.createObjectNode();
         responseNode.put("lob", inputRequest.findValue("businessLineId").asInt());
         responseNode.put("request", (JsonNode)updateProposalRequest);
         return (JsonNode)responseNode;
       } 
       ((ObjectNode)inputRequest.get("request")).put("proposalResponse", response.get("data"));
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in update Proposal Req Details  :" + e.toString());
       (new ExecutionTerminator()).execution(failure);
     } 
     return inputRequest;
   }
 }


