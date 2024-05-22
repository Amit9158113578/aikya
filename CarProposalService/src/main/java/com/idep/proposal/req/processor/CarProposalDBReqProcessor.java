 package com.idep.proposal.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CarProposalDBReqProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarProposalDBReqProcessor.class.getName());
   
   CBService service = null;
   
   JsonNode serviceConfigNode = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       String proposalRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode reqNode = Utils.mapper.readTree(proposalRequest);
       if (reqNode.has("responseCode")) {
         exchange.getIn().setBody(reqNode);
         throw new ExecutionTerminator();
       } 
       exchange.getIn().setHeader("documentId", "Policies365-CarProposalRequest");
       exchange.getIn().setBody(reqNode);
       JsonNode loadedDoc = Utils.mapper.readTree(((JsonObject)Utils.serverConfig.getDocBYId("Policies365-CarProposalRequest").content()).toString());
       if (loadedDoc != null) {
         ObjectNode object = Utils.mapper.createObjectNode();
         object.put("inputRequest", reqNode);
         object.put("configuration", loadedDoc.get("configuration"));
         exchange.getIn().setBody(object);
       } else {
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("Configuration Document Not Found for docId :Policies365-CarProposalRequest");
         exchange.getIn().setBody(failure);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString()))) + "CARPRODBREQPROCESS" + "|ERROR|" + "car proposal db request failed :", e);
       throw new ExecutionTerminator();
     } 
   }
 }


