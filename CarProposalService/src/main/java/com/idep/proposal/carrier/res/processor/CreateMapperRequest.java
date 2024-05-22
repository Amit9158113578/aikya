 package com.idep.proposal.carrier.res.processor;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class CreateMapperRequest implements Processor {
   Logger log = Logger.getLogger(CreateMapperRequest.class);
   
   public void process(Exchange exchange) throws Exception {
     try {
       String response = exchange.getIn().getBody().toString();
       JsonNode responseNode = Utils.mapper.readTree(response);
       this.log.info("responseNode :" + responseNode);
       if (response.contains("No service was found."))
         exchange.getIn().setBody((new ExceptionResponse()).invokeServiceDown()); 
       if (responseNode.has("responseCode")) {
         if (responseNode.get("responseCode").asText().equals(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText())) {
           if (responseNode.has("data"))
             exchange.getIn().setBody(responseNode.get("data")); 
         } else {
           exchange.getIn().setHeader("invokeservice", "False");
           exchange.getIn().setBody((new ExceptionResponse()).properResponseNotFound(responseNode));
         } 
       } else {
         exchange.getIn().setBody(responseNode);
       } 
     } catch (JsonParseException jp) {
       exchange.getIn().setBody((new ExceptionResponse()).parseException(jp.getMessage()));
     } catch (Exception e) {
       exchange.getIn().setBody((new ExceptionResponse()).failure("error in proposal validate response processor :" + e));
       throw new ExecutionTerminator();
     } 
   }
 }


