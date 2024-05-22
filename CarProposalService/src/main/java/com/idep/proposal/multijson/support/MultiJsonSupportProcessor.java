 package com.idep.proposal.multijson.support;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.mapper.multijson.MultiJsonMapper;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 
 
 
 public class MultiJsonSupportProcessor
   implements Processor
 {
   ObjectMapper objectMapper = new ObjectMapper();
   Logger log = Logger.getLogger(MultiJsonSupportProcessor.class.getName());
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
 
 
 
   
   public void process(Exchange exchange) throws Exception {
     try {
       String quoteRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode modQuoteReq = this.objectMapper.readTree(quoteRequest);
       
       int carrierId = modQuoteReq.get("carrierId").intValue();
       int productId = modQuoteReq.get("productId").intValue();
       String insuranceType = modQuoteReq.get("insuranceType").textValue();
       JsonNode configNode = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("CarPROPOSALJSON-" + carrierId + "-" + productId + "-" + insuranceType).content()).toString());
       
       MultiJsonMapper mulJsonMapper = new MultiJsonMapper();
       ObjectNode objectNode = mulJsonMapper.getMultiJSonFormat(modQuoteReq, configNode);
       this.log.info(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "SERVICEINVOKE" + "|SUCCESS|" + "carrier updated proposal request service invoked : " + objectNode);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(objectNode));
     }
     catch (Exception e) {
       
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "CARMULTJSONSUPP" + "|ERROR|" + "Exception in MultiJsonSupportProcessor :", e);
       throw new ExecutionTerminator();
     } 
   }
 }


