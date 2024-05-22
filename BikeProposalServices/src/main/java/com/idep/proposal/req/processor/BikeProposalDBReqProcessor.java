 package com.idep.proposal.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class BikeProposalDBReqProcessor implements Processor {
   Logger log = Logger.getLogger(BikeProposalDBReqProcessor.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode requestNode = Utils.mapper.readTree(exchange.getIn().getBody().toString());
       if (requestNode.has("responseCode")) {
         exchange.getIn().setBody(requestNode);
         throw new ExecutionTerminator();
       } 
       exchange.getIn().setHeader("documentId", "Policies365-BikeProposalRequest");
       JsonNode loadedDoc = Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("Policies365-BikeProposalRequest").content()).toString());
       if (loadedDoc != null) {
         ObjectNode object = Utils.mapper.createObjectNode();
         object.put("inputRequest", requestNode);
         object.put("configuration", loadedDoc.get("configuration"));
         exchange.getIn().setBody(object);
       } else {
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("Configuration Document Not Found for docId :Policies365-BikeProposalRequest");
         exchange.getIn().setBody(failure);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Bike Proposal DB Request Processor :" + e.toString());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


