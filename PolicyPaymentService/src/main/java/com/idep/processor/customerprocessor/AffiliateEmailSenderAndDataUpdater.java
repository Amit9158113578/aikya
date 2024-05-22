 package com.idep.processor.customerprocessor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.rest.impl.service.SMSEmailImplService;
 import com.idep.service.payment.impl.PaymentDataAccessor;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class AffiliateEmailSenderAndDataUpdater implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(AffiliateEmailSenderAndDataUpdater.class.getName());
   
   CBService service = CBInstanceProvider.getServerConfigInstance();
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   SMSEmailImplService emailService = new SMSEmailImplService();
   
   ObjectNode paramMapNode = this.objectMapper.createObjectNode();
   
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   
   public void process(Exchange exchange) throws Exception {
     String inputReq = exchange.getIn().getBody().toString();
     this.log.info("inputReqNode :" + inputReq);
     JsonNode node = this.objectMapper.readTree(inputReq);
     String proposalId = node.get("body").get("transactionStausInfo").get("proposalId").asText();
     JsonNode proposalDataNode = this.objectMapper.readTree(((JsonObject)this.transService.getDocBYId(proposalId).content()).toString());
     this.log.info("proposalIdDoc node :" + proposalDataNode);
     if (proposalDataNode.findValue("messageId") != null) {
       String messageId = proposalDataNode.findValue("messageId").asText();
       String docId = "LeadProfile-" + messageId;
       JsonDocument leadProfile = this.transService.getDocBYId(docId);
       if (leadProfile != null) {
         JsonNode leadProfileDoc = this.objectMapper.readTree(((JsonObject)leadProfile.content()).toString());
         ((ObjectNode)leadProfileDoc).put("leadStage", "PAYSUCC");
         JsonObject documentContent = JsonObject.fromJson(leadProfileDoc.toString());
         String str = this.transService.replaceDocument(docId, documentContent);
         this.log.info("LeadProfile Doc updation status :" + str + " for docId :" + docId);
       } else {
         this.log.info("Lead Profile Doc not created :" + docId);
       } 
     } else {
       this.log.info("MessageId not present in Proposal Doc :" + proposalId);
     } 
     String deviceId = proposalDataNode.get("deviceId").asText();
     long status = this.paymentDataAccessor.updateSequenceCounter("P365IntegrationList", deviceId, "noOfPolicies");
     if (status == -1L) {
       this.log.info("failed to update Affiliate Id counter:" + deviceId);
     } else {
       this.log.info("Affiliate Id updated : " + deviceId + ":counter is :" + status);
     } 
     String userName = proposalDataNode.get("firstName").asText();
     if (proposalDataNode.has("lastName"))
       userName = String.valueOf(userName) + " " + proposalDataNode.get("lastName").asText(); 
     this.paramMapNode.put("name", userName);
     this.paramMapNode.put("mobile", proposalDataNode.get("mobile"));
     this.paramMapNode.put("email", proposalDataNode.get("emailId").asText());
     JsonNode policyPurchaseConfigNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("AffiliatePolicyPurchaseConfigDoc").content()).toString());
     ObjectNode emailDataNode = this.objectMapper.createObjectNode();
     emailDataNode.put("funcType", policyPurchaseConfigNode.get("funcType").asText());
     JsonNode P365IntegrationListDoc = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("P365IntegrationList").content()).toString());
     this.log.info("P365IntegrationListDoc node :" + P365IntegrationListDoc);
     JsonNode affiliateNode = P365IntegrationListDoc.get(deviceId);
     emailDataNode.put("username", affiliateNode.get("emailId").asText());
     this.log.info("Data in paramMapNode:" + this.paramMapNode);
     emailDataNode.put("paramMap", (JsonNode)this.paramMapNode);
     this.log.info("Request to sendEmailRequest:" + emailDataNode);
     String emailRequest = this.emailService.sendEmailRequest(this.objectMapper.writeValueAsString(emailDataNode));
     this.log.info("Response from sendEmailRequest:" + emailRequest);
     exchange.getIn().setBody(emailRequest);
   }
 }


