 package com.idep.policy.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.concurrent.TimeUnit;
 import org.apache.log4j.Logger;
 
 public class AddRequestInformation
 {
   CBService quotedata = CBInstanceProvider.getBucketInstance("QuoteData");
   
   Logger logger = Logger.getLogger(AddRequestInformation.class);
   
   JsonNode quoteIdDocumentNode = null;
   
   public ExtendedJsonNode addPremiumDetails(ExtendedJsonNode requestNode, ExtendedJsonNode proposalValidationDocConfig) throws Exception {
     try {
       JsonNode premiumDetails = null;
       if (proposalValidationDocConfig.has("premiumDetailsRequired") && proposalValidationDocConfig.get("premiumDetailsRequired").has(requestNode.findValueAsText("carrierId"))) {
         JsonDocument quoteIdDoc = this.quotedata.getDocBYId(requestNode.findValueAsText("QUOTE_ID"));
         if (quoteIdDoc != null) {
           this.quoteIdDocumentNode = Utils.mapper.readTree(((JsonObject)quoteIdDoc.content()).toString());
           if (this.quoteIdDocumentNode.has("bikeQuoteResponse")) {
             JsonNode bikeQuoteResponseArray = this.quoteIdDocumentNode.get("bikeQuoteResponse");
             for (JsonNode bikeQuoteResponse : bikeQuoteResponseArray) {
               if (requestNode.asInt("productId") == bikeQuoteResponse.get("productId").asInt()) {
                 premiumDetails = bikeQuoteResponse;
                 break;
               } 
             } 
             if (premiumDetails != null) {
               requestNode.put("premiumDetails", premiumDetails);
             } else {
               return (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgPremiumInfoNotFound").asText())) + requestNode.getKey("QUOTE_ID"));
             } 
           } else {
             return (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgPremiumInfoNotFound").asText())) + requestNode.getKey("QUOTE_ID"));
           } 
         } else {
           return (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgQuoteInfoNotFound").asText())) + requestNode.getKey("QUOTE_ID"));
         } 
       } 
     } catch (Exception e) {
       return (new ExceptionResponse()).failure();
     } 
     return requestNode;
   }
   
   public ExtendedJsonNode addBikeQuoteRequest(ExtendedJsonNode requestNode, ExtendedJsonNode proposalValidationDocConfig) throws Exception {
     try {
       if (proposalValidationDocConfig.has("quoteRequestRequired") && proposalValidationDocConfig.get("quoteRequestRequired").has(requestNode.findValueAsText("carrierId"))) {
         JsonDocument quoteIdDoc = this.quotedata.getDocBYId(requestNode.findValueAsText("QUOTE_ID"));
         if (quoteIdDoc != null)
           this.quoteIdDocumentNode = Utils.mapper.readTree(((JsonObject)quoteIdDoc.content()).toString()); 
         if (this.quoteIdDocumentNode != null) {
           JsonNode quoteRequest = this.quoteIdDocumentNode.get("bikeQuoteRequest");
           if (quoteRequest != null) {
             if (quoteRequest.findValue("planType").asText().equals("OD-1") && 52 == requestNode.findValueAsInt("carrierId")) {
               
               String constant = "~";
               int noOfClaim = 0;
               SimpleDateFormat tpFormate = new SimpleDateFormat("dd-MM-yyyy");
               SimpleDateFormat expiryDateFormate = new SimpleDateFormat("dd/MM/yyyy");
               SimpleDateFormat optSdf = new SimpleDateFormat("dd-MMM-YYYY");
               if (quoteRequest.findValue("previousClaim").asBoolean())
                 noOfClaim = 1; 
               String bajajExtcol36 = String.valueOf(optSdf.format(expiryDateFormate.parse(quoteRequest.findValue("PreviousPolicyExpiryDate").textValue()))) + constant + requestNode.findValueAsInt("TPPolicyInsurer") + constant + requestNode.findValueAsText("insurerName") + constant + requestNode.findValueAsText("TPPolicyNumber") + constant + optSdf.format(tpFormate.parse(quoteRequest.findValue("TPPolicyExpiryDate").textValue())) + constant + noOfClaim + constant + '\005' + constant + optSdf.format(tpFormate.parse(quoteRequest.findValue("TPPolicyStartDate").textValue()));
               ((ObjectNode)quoteRequest.get("vehicleInfo")).put("bajajExtcol36", bajajExtcol36);
             } 
             requestNode.put("bikeQuoteRequest", quoteRequest);
           } else {
             
             return (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgQuoteRequestInfoNotFound").asText())) + requestNode.getKey("QUOTE_ID"));
           } 
         } else {
           return (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgQuoteInfoNotFound").asText())) + requestNode.getKey("QUOTE_ID"));
         } 
       } 
     } catch (Exception e) {
       return (new ExceptionResponse()).failure();
     } 
     return requestNode;
   }
   
   public ExtendedJsonNode carrierTransformRequest(ExtendedJsonNode requestNode, ExtendedJsonNode ProposalValidationDocConfig) throws Exception {
     if (ProposalValidationDocConfig.has("carrierRequestRequired") && ProposalValidationDocConfig.get("carrierRequestRequired").has(requestNode.findValueAsText("carrierId")))
       if (this.quoteIdDocumentNode != null) {
         JsonNode carrierTransformedReqNode = this.quoteIdDocumentNode.get("carrierTransformedReq").get(requestNode.getKey("productId"));
         requestNode.put("carrierTransformedReqNode", carrierTransformedReqNode);
       } else {
         return (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgQuoteInfoNotFound").asText())) + requestNode.getKey("QUOTE_ID"));
       }  
     return requestNode;
   }
   
   public ExtendedJsonNode validatePolicyDates(ExtendedJsonNode requestNode) throws Exception {
     if (requestNode.findValueAsText("policyType").equals("renew")) {
       SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
       Calendar cal = new GregorianCalendar();
       String today = dateFormat.format(cal.getTime());
       Date prePolicyExpDateString = dateFormat.parse(requestNode.findValueAsText("sysPolicyStartDate"));
       Date todays = dateFormat.parse(today);
       if (prePolicyExpDateString.before(todays)) {
         ExtendedJsonNode quoteInfoNotFound = (new ExceptionResponse()).quoteInfoNotFound(String.valueOf(String.valueOf(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("ResponseMsgPrePolicyExp").asText())) + requestNode.findValueAsText("sysPolicyStartDate"));
         return quoteInfoNotFound;
       } 
       requestNode = checkPolicyExpired(requestNode);
     } 
     return requestNode;
   }
   
   public ExtendedJsonNode checkPolicyExpired(ExtendedJsonNode requestNode) throws ParseException {
     try {
       int expirydays = (int)calculatePreviousPolicyExpiry(requestNode.findValueAsText("PreviousPolicyExpiryDate"));
       if (expirydays >= 90) {
         requestNode.get("insuranceDetails").put("isNCB", "N");
       } else {
         requestNode.get("insuranceDetails").put("isNCB", "Y");
       } 
     } catch (Exception e) {
       this.logger.error("check Policy Expired days method get error :");
       return requestNode;
     } 
     return requestNode;
   }
   
   public long calculatePreviousPolicyExpiry(String previousPolicyExpiryDate) throws ParseException {
     try {
       SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
       Date expiryDate = sdf.parse(previousPolicyExpiryDate);
       Date date = new Date();
       String format = sdf.format(date);
       date = sdf.parse(format);
       long days = date.getTime() - expiryDate.getTime();
       return TimeUnit.DAYS.convert(days, TimeUnit.MILLISECONDS);
     } catch (Exception e) {
       this.logger.error(" calculate previous policy expity get error :" + previousPolicyExpiryDate, e);
       return 0L;
     } 
   }
   
   public ExtendedJsonNode addProposalInfo(ExtendedJsonNode requestNode) throws Exception {
     CBService transService = CBInstanceProvider.getPolicyTransInstance();
     String proposalId = requestNode.findValueAsText("proposalId");
     JsonDocument proposalDoc = transService.getDocBYId(proposalId);
     if (proposalDoc != null) {
       ExtendedJsonNode proposalInfoNode = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)proposalDoc.content()).toString()));
       requestNode.put("proposalInfo", proposalInfoNode);
       return requestNode;
     } 
     return (new ExceptionResponse()).quoteInfoNotFound("proposal information document not found in database for Id :" + proposalId);
   }
   
   public ExtendedJsonNode addPaymentResponse(ExtendedJsonNode requestNode, ExtendedJsonNode ProposalValidationDocConfig) throws Exception {
     if (ProposalValidationDocConfig.has("payResponseRequired") && ProposalValidationDocConfig.get("payResponseRequired").has(requestNode.findValueAsText("carrierId"))) {
       CBService transService = CBInstanceProvider.getPolicyTransInstance();
       String apPreferId = requestNode.get("transactionStausInfo").getKey("apPreferId");
       JsonDocument apPreferIdDoc = transService.getDocBYId(apPreferId);
       if (apPreferIdDoc != null) {
         ExtendedJsonNode paymentResponseNode = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)apPreferIdDoc.content()).toString()));
         requestNode.put("paymentResponse", paymentResponseNode);
         return requestNode;
       } 
       return (new ExceptionResponse()).quoteInfoNotFound("payment response information document not found in database for Id :" + apPreferId);
     } 
     return requestNode;
   }
   
   public ExtendedJsonNode validatePayResponseForPolicy(ExtendedJsonNode requestNode) throws Exception {
     try {
       if (requestNode.get("transactionStausInfo").asInt("transactionStatusCode") == 1) {
         CBService transService = CBInstanceProvider.getPolicyTransInstance();
         JsonDocument masterPaymentConfigDoc = Utils.serverConfig.getDocBYId("MasterPaymentConfigDetails");
         if (masterPaymentConfigDoc != null) {
           JsonNode masterDetailsNode = Utils.mapper.readTree(((JsonObject)masterPaymentConfigDoc.content()).toString());
           String appPreferId = requestNode.get("transactionStausInfo").getKey("apPreferId");
           JsonDocument appPreferIdDoc = transService.getDocBYId(appPreferId);
           if (appPreferIdDoc != null) {
             ExtendedJsonNode payresNode = new ExtendedJsonNode(Utils.mapper.readTree(((JsonObject)appPreferIdDoc.content()).toString()));
             return validatePayAndMasterConfig(payresNode, masterDetailsNode);
           } 
           ExtendedJsonNode extendedJsonNode1 = (new ExceptionResponse()).failure("validating carrier PAYRES response document not found in database :" + appPreferId);
           return extendedJsonNode1;
         } 
         ExtendedJsonNode extendedJsonNode = (new ExceptionResponse()).failure("validating carrier response document not found in database : MasterPaymentConfigDetails");
         return extendedJsonNode;
       } 
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("payment response failure ..! validate transaction status code is not one (1) :");
       return failure;
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in validatePayResponseForPolicy method because of :" + e.getMessage());
       return failure;
     } 
   }
   
   public ExtendedJsonNode validatePayAndMasterConfig(ExtendedJsonNode appPreferNode, JsonNode masterConfigDetailsNode) throws Exception {
     try {
       JsonNode carrierPayConfigNode = masterConfigDetailsNode.get(String.valueOf(String.valueOf(appPreferNode.getKey("carrier"))) + appPreferNode.getKey("lob"));
       if (carrierPayConfigNode != null) {
         JsonNode paymentStatusArray = carrierPayConfigNode.get("params").get("paymentStatus");
         for (JsonNode paymentStatus : paymentStatusArray) {
           if (paymentStatus.has("transStatusField") && paymentStatus.has("successValue")) {
             ExtendedJsonNode paymentNode = appPreferNode.get("clientResponse");
             if (paymentNode.getKey(paymentStatus.get("transStatusField").asText()).equalsIgnoreCase(paymentStatus.get("successValue").asText()))
               return appPreferNode; 
             if (paymentStatus.get("successValue").asText().equals("NA"))
               return appPreferNode; 
             continue;
           } 
           ExtendedJsonNode failure = (new ExceptionResponse()).failure("validate payment carrier response transStatusField and successValue details not found : " + paymentStatus);
           return failure;
         } 
       } else {
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("carrier payment response validate details not found in MasterPaymentConfigDetails document :" + appPreferNode.getKey("carrier") + appPreferNode.getKey("lob"));
         return failure;
       } 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in validatePayAndMasterConfig method in AddRequestInformation class  :" + e.getMessage());
       return failure;
     } 
     return null;
   }
 }


