 package com.idep.proposal.carrier.res.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class MapperResponseHandler implements Processor {
   Logger log = Logger.getLogger(MapperResponseHandler.class.getName());
   
   public void process(Exchange exchange) throws Exception {
     try {
       String carrierRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode carrierReqNode = Utils.mapper.readTree(carrierRequest);
       if (carrierReqNode.has("carrierRequestForm")) {
         ObjectNode responseNode = Utils.mapper.createObjectNode();
         responseNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText());
         responseNode.put("status", "success");
         responseNode.put("data", Utils.mapper.readTree(carrierReqNode.get("carrierRequestForm").toString()));
         responseNode.put("message", "success");
         exchange.getIn().setBody(responseNode);
       } else {
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("carrier request information not found for response :" + carrierReqNode);
         exchange.getIn().setBody(failure);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in handling response from mapper for carrier :" + e.toString());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


