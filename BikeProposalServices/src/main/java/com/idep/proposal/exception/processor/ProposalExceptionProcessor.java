 package com.idep.proposal.exception.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ProposalExceptionProcessor implements Processor {
   Logger log = Logger.getLogger(ProposalExceptionProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   CBService service = null;
   
   JsonNode responseConfigNode;
   
   JsonNode errorNode;
   
   public void process(Exchange exchange) throws Exception {
     try {
       if (this.service == null) {
         this.service = CBInstanceProvider.getServerConfigInstance();
         this.responseConfigNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("ResponseMessages").content()).toString());
       } 
       JsonNode failure = this.objectMapper.readTree(exchange.getIn().getBody().toString());
       if (failure.has("responseCode")) {
         exchange.getIn().setBody(failure);
       } else {
         exchange.getIn().setBody((new ExceptionResponse()).failure());
       } 
     } catch (Exception e) {
       exchange.getIn().setBody((new ExceptionResponse()).failure());
     } 
   }
 }


