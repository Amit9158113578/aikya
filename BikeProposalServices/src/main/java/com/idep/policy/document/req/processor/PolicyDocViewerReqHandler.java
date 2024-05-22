 package com.idep.policy.document.req.processor;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PolicyDocViewerReqHandler implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(PolicyDocViewerReqHandler.class.getName());
   
   public void process(Exchange exchange) {
     try {
       String docViewRequest = (String)exchange.getIn().getBody(String.class);
       JsonNode docViewRequestNode = this.objectMapper.readTree(docViewRequest);
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(docViewRequestNode));
     } catch (Exception e) {
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "POLICYDOCVIEWMAPPER|ERROR|Exception at PolicyDocViewerReqHandler:");
     } 
   }
 }


