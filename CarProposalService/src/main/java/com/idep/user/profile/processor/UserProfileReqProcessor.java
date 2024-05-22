 package com.idep.user.profile.processor;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import com.idep.user.profile.impl.UserProfileBuilder;
 import java.io.IOException;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class UserProfileReqProcessor implements Processor {
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   Logger log = Logger.getLogger(UserProfileReqProcessor.class.getName());
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   UserProfileBuilder profileService = new UserProfileBuilder();
   
   public void process(Exchange exchange) throws Exception {
     try {
       JsonNode userProfileDataNode = null;
       String data = (String)exchange.getIn().getBody(String.class);
       JsonNode policyDataNode = this.objectMapper.readTree(data);
       if (policyDataNode.has("responseCode") && policyDataNode.get("responseCode").asText().equalsIgnoreCase(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseCodeSuccess").asText())) {
         policyDataNode = removeNotRequiredDataForDB(policyDataNode);
         userProfileDataNode = this.profileService.buildUserProfile(policyDataNode.get("data"));
       } 
       if (userProfileDataNode != null) {
         policyDataNode = removeNotRequiredDataForDB(policyDataNode);
         ((ObjectNode)policyDataNode.get("data")).put("profileResponse", userProfileDataNode);
         policyDataNode = sendPolicyEmail(policyDataNode, exchange);
         exchange.getIn().setBody(this.objectMapper.writeValueAsString(policyDataNode));
       } else {
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("USER PROFILE ERROR : user profile creation failed : " + data);
         exchange.getIn().setBody(failure);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("USERPROREQPRO|ERROR|user profile processing failed :" + e);
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
   
   public JsonNode removeNotRequiredDataForDB(JsonNode dbRequest) {
     try {
       if (dbRequest.get("data").has("base64data")) {
         ((ObjectNode)dbRequest).put("base64data", dbRequest.get("data").get("base64data").asText());
         ((ObjectNode)dbRequest.get("data")).remove("base64data");
         return dbRequest;
       } 
       if (dbRequest.has("base64data")) {
         ((ObjectNode)dbRequest.get("data")).put("base64data", dbRequest.get("base64data").asText());
         ((ObjectNode)dbRequest).remove("base64data");
         return dbRequest;
       } 
     } catch (Exception e) {
       this.log.info("error in removeNotRequiredDataForDB method :" + e.getStackTrace());
     } 
     return dbRequest;
   }
   
   public JsonNode sendPolicyEmail(JsonNode obj, Exchange exchange) throws JsonProcessingException, IOException {
     try {
       if (exchange.getProperty("configDoc") != null) {
         JsonNode configNode = Utils.mapper.readTree(exchange.getProperty("configDoc").toString());
         if (obj.get("data").has("carrierId")) {
           JsonNode carrierInfo = configNode.get(obj.get("data").get("carrierId").asText());
           if (carrierInfo.has("sendPolicyEmail") && carrierInfo.get("sendPolicyEmail").asText().equalsIgnoreCase("Y"))
             exchange.getIn().setHeader("sendPolicyEmail", "Y"); 
           if (carrierInfo.has("createPolicyDocument") && carrierInfo.get("createPolicyDocument").asText().equalsIgnoreCase("Y"))
             exchange.getIn().setHeader("createPolicyDocument", "Y"); 
         } 
       } 
       return obj;
     } catch (Exception e) {
       this.log.error("exception in send policy email methods :");
       return obj;
     } 
   }
 }


