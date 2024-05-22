 package com.idep.policydoc.carrier.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class KOTAKPolicyDocumentProcessor
   implements Processor
 {
   Logger log = Logger.getLogger(KOTAKPolicyDocumentProcessor.class.getName());
   ObjectMapper objectMapper = new ObjectMapper();
   CBService serverConfig = null;
   JsonNode serviceURLConfig = null;
   JsonNode hostConfig = null;
 
 
 
   
   public void process(Exchange exchange) throws Exception {
     try {
       if (this.serverConfig == null) {
         
         try {
           
           this.serverConfig = CBInstanceProvider.getServerConfigInstance();
           this.serviceURLConfig = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("ServiceURLConfig").content()).toString());
           this.hostConfig = this.objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("WEBHOSTConfig").content()).toString());
         }
         catch (Exception e) {
           
           this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "KOTAKPOLICYDOCPRO|ERROR|" + "unable to load configuration documents from db : ServiceURLConfig,WEBHOSTConfig", e);
         } 
       }
 
 
 
       
       JsonNode userPolicyProfileNode = this.objectMapper.readTree(exchange.getProperty("userPolicyProfileData").toString());
 
       
       ObjectNode keyDataNode = this.objectMapper.createObjectNode();
       keyDataNode.put("uKey", userPolicyProfileNode.get("uKey").asText());
       keyDataNode.put("pKey", userPolicyProfileNode.get("pKey").asText());
       exchange.getIn().removeHeader("CamelHttpPath");
       exchange.getIn().removeHeader("CamelHttpUri");
       exchange.getIn().setHeader("CamelHttpMethod", "POST");
       exchange.getIn().setHeader("Content-Type", "application/json");
       exchange.getIn().setHeader("CamelAcceptContentType", "application/json");
       exchange.getIn().setHeader("Origin", this.hostConfig.get("serviceOrigin").asText());
       exchange.getIn().setHeader("requestURL", this.serviceURLConfig.get("masterService").asText());
       exchange.getIn().setHeader("policyDocumentService", this.serviceURLConfig.get("masterService").asText());
 
       
       ObjectNode headerNode = this.objectMapper.createObjectNode();
       headerNode.put("transactionName", "viewPolicyDoc");
       headerNode.put("deviceId", this.objectMapper.readTree(exchange.getProperty("deviceId").toString()));
       ObjectNode requestNode = this.objectMapper.createObjectNode();
       requestNode.put("header", (JsonNode)headerNode);
       requestNode.put("body", (JsonNode)keyDataNode);
 
       
       exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestNode));
     
     }
     catch (Exception e) {
       
       this.log.error(String.valueOf(String.valueOf(exchange.getProperty("logReq").toString())) + "KOTAKPOLICYDOCPRO" + "|ERROR|" + "Exception at KOTAKPolicyDocumentProcessor", e);
     } 
   }
 }


