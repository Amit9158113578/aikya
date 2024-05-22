 package com.idep.policy.payment.res.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class PaymentResponseValidator implements Processor {
   Logger log = Logger.getLogger(PaymentResponseValidator.class);
   
   ObjectMapper objectMapper = new ObjectMapper();
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   CBService service = CBInstanceProvider.getServerConfigInstance();
   
   JsonNode masterPaymentConfigNode = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       String inputReq = (String)exchange.getIn().getBody(String.class);
       JsonNode inputReqNode = this.objectMapper.readTree(inputReq.toString());
       if (inputReqNode.get("transactionStausInfo").get("transactionStatusCode").asInt() == 0) {
         this.log.error("PAYMENTRES|FAIL|Payment Failed : " + inputReqNode);
         throw new ExecutionTerminator();
       } 
       if (this.masterPaymentConfigNode == null)
         try {
           JsonDocument masterPaymentConfigDoc = this.service.getDocBYId("MasterPaymentConfigDetails");
           if (masterPaymentConfigDoc != null) {
             this.masterPaymentConfigNode = this.objectMapper.readTree(((JsonObject)masterPaymentConfigDoc.content()).toString());
           } else {
             this.log.error("MasterPaymentConfigDetails document is missing");
           } 
         } catch (Exception e) {
           this.log.error("failed to read MasterPaymentConfigDetails document ", e);
         }  
       if (this.masterPaymentConfigNode != null)
         if (inputReqNode.get("transactionStausInfo").has("apPreferId")) {
           JsonNode appPreferNode = inputReqNode.get("transactionStausInfo").get("apPreferId");
           if (appPreferNode != null) {
             try {
               JsonDocument paymentDoc = this.transService.getDocBYId(appPreferNode.textValue());
               if (paymentDoc != null) {
                 JsonNode paymentDocNode = this.objectMapper.readTree(((JsonObject)paymentDoc.content()).toString());
                 if (paymentDocNode.has("carrier") && paymentDocNode.has("lob")) {
                   JsonNode carrierPayConfigNode = this.masterPaymentConfigNode.get(String.valueOf(String.valueOf(String.valueOf(paymentDocNode.get("carrier").asText()))) + paymentDocNode.get("lob").asText());
                   if (carrierPayConfigNode != null) {
                     boolean paymentStatusValidate = false;
                     JsonNode paymentStatusArray = carrierPayConfigNode.get("params").get("paymentStatus");
                     for (JsonNode paymentStatus : paymentStatusArray) {
                       if (paymentStatus.has("transStatusField") && paymentStatus.has("successValue")) {
                         JsonNode paymentNode = paymentDocNode.get("clientResponse");
                         if (paymentNode.get(paymentStatus.get("transStatusField").asText()).asText()
                           .equalsIgnoreCase(paymentStatus.get("successValue").asText())) {
                           paymentStatusValidate = true;
                           exchange.getIn().setHeader("reqFlag", "True");
                           exchange.getIn().setBody(inputReqNode);
                           break;
                         } 
                         if (paymentStatus.get("successValue").asText().equalsIgnoreCase("NA")) {
                           paymentStatusValidate = true;
                           exchange.getIn().setHeader("reqFlag", "True");
                           exchange.getIn().setBody(inputReqNode);
                           break;
                         } 
                         continue;
                       } 
                       this.log.error("MasterPaymentConfigDetails does not have transStatusField and successValue for this carrier");
                       exchange.getIn().setHeader("reqFlag", "False");
                     } 
                     if (!paymentStatusValidate) {
                       this.log.error("Payment Failed. Carrier payment response and create policy request car : " + inputReqNode);
                       exchange.getIn().setHeader("reqFlag", "False");
                     } 
                   } else {
                     exchange.getIn().setHeader("reqFlag", "False");
                     this.log.error("MasterPaymentConfigDetails does not have payment configuration for this carrier");
                   } 
                 } else {
                   exchange.getIn().setHeader("reqFlag", "False");
                   this.log.error("carrier or lob is missing in apPreferId : " + appPreferNode.textValue());
                 } 
               } else {
                 this.log.error("Payment Failed. apPreferId not created in database : " + appPreferNode.textValue());
                 exchange.getIn().setHeader("reqFlag", "False");
               } 
             } catch (Exception e) {
               this.log.error("failed to read payment response document : " + appPreferNode.textValue(), e);
               exchange.getIn().setHeader("reqFlag", "False");
               throw new ExecutionTerminator();
             } 
           } else {
             this.log.error("apPreferId document is not found in database :" + appPreferNode);
             ObjectNode obj = this.objectMapper.createObjectNode();
             exchange.getIn().setHeader("reqFlag", "False");
             obj.put("responseCode", 1010);
             obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
             obj.put("data", "");
             exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
           } 
         } else {
           this.log.error("apPreferId is missing in create policy request. hence terminating policy creation process :" + inputReqNode);
           ObjectNode obj = this.objectMapper.createObjectNode();
           exchange.getIn().setHeader("reqFlag", "False");
           obj.put("responseCode", 1010);
           obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
           obj.put("data", "");
           exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
         }  
       if (inputReqNode.get("transactionStausInfo").has("proposalId")) {
         String proposalId = inputReqNode.get("transactionStausInfo").get("proposalId").textValue();
         JsonDocument proposalIdDoc = this.transService.getDocBYId(proposalId);
         if (proposalIdDoc != null) {
           JsonNode proposalIdNode = this.objectMapper.readTree(((JsonObject)proposalIdDoc.content()).toString());
           if (proposalIdNode.has("carPolicyResponse") && 
             proposalIdNode.get("carPolicyResponse").get("transactionStatusCode").asInt() == 1) {
             ObjectNode responseNode = this.objectMapper.createObjectNode();
             responseNode.put("proposalId", proposalIdNode.get("carPolicyResponse").get("proposalId").textValue());
             responseNode.put("policyNo", proposalIdNode.get("carPolicyResponse").get("policyNo").asText());
             ObjectNode obj = this.objectMapper.createObjectNode();
             obj.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successCode").asInt());
             obj.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
             obj.put("data", (JsonNode)responseNode);
             exchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
           } 
         } else {
           this.log.error("failed to read proposal document from database : " + proposalId);
           throw new ExecutionTerminator();
         } 
       } else {
         this.log.error("proposal Id Not found In create Policy request : " + inputReqNode);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       this.log.error("Exception at PaymentResponseValidator", e);
       throw new ExecutionTerminator();
     } 
   }
 }


