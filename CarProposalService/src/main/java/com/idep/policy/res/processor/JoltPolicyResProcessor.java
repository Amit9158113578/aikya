 package com.idep.policy.res.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class JoltPolicyResProcessor implements Processor {
   Logger log = Logger.getLogger(JoltPolicyResProcessor.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   public void process(Exchange exchange) throws Exception {
     String reqBody = (String)exchange.getIn().getBody(String.class);
     JsonNode reqNode = Utils.mapper.readTree(reqBody);
     JsonDocument policyResConfig = this.serverConfig.getDocBYId("JOLTResponse-Policy-" + reqNode.findValue("businessLineId").asInt() + "-" + reqNode.findValue("carrierId").asInt() + "-" + reqNode.findValue("productId").asInt() + "-" + reqNode.findValue("policyType").asText());
     if (policyResConfig != null) {
       JsonNode loadedDoc = Utils.mapper.readTree(((JsonObject)policyResConfig.content()).toString());
       ObjectNode object = Utils.mapper.createObjectNode();
       object.put("inputRequest", reqNode);
       object.put("configuration", loadedDoc.get("configuration"));
       exchange.getIn().setBody(object);
     } else {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Configuration Document Not Found for docId :JOLTResponse-Policy-" + reqNode.findValue("businessLineId").asInt() + "-" + reqNode.findValue("carrierId").asInt() + "-" + reqNode.findValue("productId").asInt() + "-" + reqNode.findValue("policyType").asText());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


