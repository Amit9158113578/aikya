 package com.idep.policy.document.req.processor;
 
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import com.idep.user.profile.impl.UserProfileServices;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 public class PolicyDocViewerReqProcessor
   implements Processor {
   Logger log = Logger.getLogger(PolicyDocViewerReqProcessor.class.getName());
   
   CBService service = null;
   
   JsonNode responseConfigNode = null;
   
   CBService transService = CBInstanceProvider.getPolicyTransInstance();
   
   JsonNode errorNode = null;
   
   UserProfileServices profileServices = new UserProfileServices();
   
   public void process(Exchange exchange) throws Exception {
     try {
       if (this.service == null) {
         this.service = CBInstanceProvider.getServerConfigInstance();
         this.responseConfigNode = Utils.mapper.readTree(((JsonObject)this.service.getDocBYId("ResponseMessages").content()).toString());
       } 
       String docViewReq = (String)exchange.getIn().getBody(String.class);
       JsonNode docViewReqNode = Utils.mapper.readTree(docViewReq);
       exchange.setProperty("userPolicyKeys", docViewReqNode);
       String userSecretKey = docViewReqNode.findValue("uKey").textValue();
       String policySecretKey = docViewReqNode.findValue("pKey").textValue();
       ExtendedJsonNode finalDataNode = new ExtendedJsonNode((JsonNode)Utils.mapper.createObjectNode());
       ObjectNode userDataNode = Utils.mapper.createObjectNode();
       JsonNode userProfileDetailsNode = this.profileServices.getUserProfileByUkey(userSecretKey);
       if (userProfileDetailsNode == null) {
         this.log.info("user profile not found.. retry after 10 seconds");
         Thread.sleep(20000L);
         userProfileDetailsNode = this.profileServices.getUserProfileByUkey(userSecretKey);
       } 
       if (userProfileDetailsNode != null) {
         JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");
         exchange.setProperty("userProfileData", userPersonalInfo);
         JsonNode policyNode = this.profileServices.getPolicyRecordByPkey(userPersonalInfo.get("mobile").asText(), policySecretKey);
         JsonNode proposalDataNode = Utils.mapper.readTree(((JsonObject)this.transService.getDocBYId(policyNode.get("proposalId").asText()).content()).toString());
         ExtendedJsonNode extendedJsonNode = new ExtendedJsonNode(proposalDataNode);
         String apPreferId = extendedJsonNode.get("paymentResponse").getKey("apPreferId");
         JsonNode paymentData = Utils.mapper.readTree(((JsonObject)this.transService.getDocBYId(apPreferId).content()).toString());
         if (paymentData != null) {
           ExtendedJsonNode extPaymentData = new ExtendedJsonNode(paymentData);
           ObjectNode transactionInfo = Utils.mapper.createObjectNode();
           transactionInfo.put("TransactionNo", extPaymentData.findValueAsText("TransactionNo"));
           transactionInfo.put("totalPremium", extPaymentData.asText("Amount"));
           if (transactionInfo != null)
             finalDataNode.put("transactionInfo", (JsonNode)transactionInfo); 
         } 
         ExtendedJsonNode vehicleDetailsNode = extendedJsonNode.get("proposalRequest").get("vehicleDetails");
         String vehicleDocId = String.valueOf(String.valueOf(vehicleDetailsNode.getKey("variantId"))) + "-" + extendedJsonNode.findValueAsText("carrierId");
         this.log.info("variantId :" + vehicleDocId);
         JsonNode variantIdDocument = Utils.mapper.readTree(((JsonObject)this.service.getDocBYId(vehicleDocId).content()).toString());
         this.log.info("variantIdDocument :" + variantIdDocument);
         if (variantIdDocument != null)
           extendedJsonNode.get("proposalRequest").put("carrierVehicleInfo", variantIdDocument); 
         ExtendedJsonNode proposerDetailsNode = extendedJsonNode.get("proposalRequest").get("proposerDetails");
         String policyHolderName = proposerDetailsNode.getKey("firstName");
         if (proposerDetailsNode.has("lastName"))
           policyHolderName = String.valueOf(String.valueOf(StringUtils.capitalise(policyHolderName))) + " " + StringUtils.capitalise(proposerDetailsNode.getKey("lastName")); 
         String address = "";
         if (proposerDetailsNode.get("communicationAddress").has("comDoorNo"))
           address = proposerDetailsNode.get("communicationAddress").getKey("comDoorNo"); 
         address = String.valueOf(String.valueOf(address)) + " " + proposerDetailsNode.get("communicationAddress").getKey("comDisplayArea");
         userDataNode.put("name", policyHolderName);
         userDataNode.put("address", address);
         userDataNode.put("pincode", proposerDetailsNode.get("communicationAddress").getKey("comPincode"));
         String RtoDocumentId = String.valueOf(String.valueOf(vehicleDetailsNode.getKey("RTOCode"))) + "-" + extendedJsonNode.findValueAsText("carrierId") + "-" + extendedJsonNode.findValue("premiumDetails").get("quoteType").asInt();
         JsonNode rtoDocumentNode = Utils.mapper.readTree(((JsonObject)this.service.getDocBYId(RtoDocumentId).content()).toString());
         this.log.info("rtoDocumentNode :" + rtoDocumentNode);
         ExtendedJsonNode proposerDataNode = extendedJsonNode.get("proposalRequest").get("proposerDetails");
 
 
 
 
 
         
         String policyIssuedAddress = address.concat("\n").concat(proposerDataNode.findValueAsText("comCity")).concat(" - ").concat(proposerDataNode.findValueAsText("comPincode")).concat("\n").concat(String.valueOf(String.valueOf(proposerDataNode.findValueAsText("comState"))) + "(" + rtoDocumentNode.get("stateCode").asInt() + ")").concat("\n").concat("India").concat("\n").concat("Contact Details ").concat(proposerDataNode.getKey("mobileNumber"));
         ((ObjectNode)policyNode).put("policyIssuedAddress", policyIssuedAddress);
         userDataNode.put("salutation", proposerDataNode.getKey("salutation"));
         finalDataNode.put("userInfo", (JsonNode)userDataNode);
         finalDataNode.put("userProposal", Utils.mapper.readTree(policyNode.toString()));
         double odpremium = extendedJsonNode.get("proposalRequest").get("premiumDetails").asDouble("odpremium");
         double ncbDiscount = extendedJsonNode.get("proposalRequest").get("premiumDetails").get("discountList").get(0).asDouble("discountAmount");
         double totalownDamagePremium = odpremium - ncbDiscount;
         double totalLibilityPremium = (extendedJsonNode.get("proposalRequest").get("premiumDetails").asInt("paidDriverCover") + extendedJsonNode.get("proposalRequest").get("premiumDetails").asInt("tppremium"));
         if (extendedJsonNode.get("proposalRequest").get("premiumDetails").has("ridersList") && proposalDataNode.get("proposalRequest").get("premiumDetails").get("ridersList").size() > 0) {
           totalLibilityPremium += extendedJsonNode.get("proposalRequest").get("premiumDetails").get("ridersList").get(0).asInt("riderValue");
           this.log.info("rider amount is added :" + totalLibilityPremium);
         } 
         extendedJsonNode.get("proposalRequest").get("premiumDetails").putDouble("totalLibilityPremium", totalLibilityPremium);
         extendedJsonNode.get("proposalRequest").get("premiumDetails").putDouble("totalOwnDamagePremium", totalownDamagePremium);
         ArrayNode premiumTaxableData = Utils.mapper.createArrayNode();
         ObjectNode TotalTaxableValue = Utils.mapper.createObjectNode();
         TotalTaxableValue.put("name", "Taxable Value Of Services (A+B)");
         TotalTaxableValue.put("value", totalLibilityPremium + totalownDamagePremium);
         premiumTaxableData.add((JsonNode)TotalTaxableValue);
         ObjectNode gstNode = Utils.mapper.createObjectNode();
         ExtendedJsonNode discountNode = extendedJsonNode.get("proposalRequest").get("premiumDetails").get("discountList").get(0);
         if (discountNode.isNull("discountAmount"))
           extendedJsonNode.get("proposalRequest").get("premiumDetails").get("discountList").get(0).putInt("discountAmount", 0); 
         double serviceTax = extendedJsonNode.get("proposalRequest").get("premiumDetails").asDouble("serviceTax");
         String gstDocumentId = "GSTConfig-" + extendedJsonNode.findValueAsText("carrierId");
         JsonNode gstConfigDoc = Utils.mapper.readTree(((JsonObject)this.service.getDocBYId(gstDocumentId).content()).toString());
         double sgst = serviceTax / 2.0D;
         double cgst = serviceTax - sgst;
         ObjectNode cgsNode = Utils.mapper.createObjectNode();
         if (gstConfigDoc != null)
           if (gstConfigDoc.has(proposerDataNode.findValueAsText("comState"))) {
             if (gstConfigDoc.get(proposerDataNode.findValueAsText("comState")).asText().equalsIgnoreCase("CGST")) {
               cgsNode.put("name", "CGST @ 9%");
               cgsNode.put("value", cgst);
               premiumTaxableData.add((JsonNode)cgsNode);
               gstNode.put("name", "SGST @ 9%");
               gstNode.put("value", sgst);
               premiumTaxableData.add((JsonNode)gstNode);
             } else {
               cgsNode.put("name", "CGST @ 9%");
               cgsNode.put("value", cgst);
               premiumTaxableData.add((JsonNode)cgsNode);
               gstNode.put("name", "UGST @ 9%");
               gstNode.put("value", sgst);
               premiumTaxableData.add((JsonNode)gstNode);
             } 
           } else {
             gstNode.put("name", "IGST @ 18%");
             gstNode.put("value", serviceTax);
             premiumTaxableData.add((JsonNode)gstNode);
           }  
         ObjectNode totalPremium = Utils.mapper.createObjectNode();
         totalPremium.put("name", "Total Premium");
         totalPremium.put("value", proposalDataNode.get("totalPremium"));
         premiumTaxableData.add((JsonNode)totalPremium);
         finalDataNode.put("premiumTaxTableData", (JsonNode)premiumTaxableData);
         finalDataNode.put("premiumDetails", extendedJsonNode.get("proposalRequest").get("premiumDetails"));
         if (!vehicleDetailsNode.has("financeInstitution") || vehicleDetailsNode.getKey("purchasedLoan").equalsIgnoreCase("No"))
           vehicleDetailsNode.put("financeInstitution", "NA"); 
         finalDataNode.put("vehicleDetails", vehicleDetailsNode);
         JsonNode regAddressNode = proposalDataNode.get("proposalRequest").get("vehicleDetails").get("registrationAddress");
         JsonNode premiumDetails = proposalDataNode.get("proposalRequest").get("premiumDetails");
         int ncb = Integer.parseInt(premiumDetails.get("ncbPercentage").textValue());
         String ncbpercentage = "Bonus Percent @  " + ncb + "%";
         ((ObjectNode)proposalDataNode.get("proposalRequest").get("insuranceDetails")).put("ncb", ncbpercentage);
         finalDataNode.put("vehicleRegAddress", regAddressNode);
         finalDataNode.put("proposalData", proposalDataNode);
         finalDataNode.put("requestType", "BikePolicyDocumentRequest");
         String documentId = "BikePolicyDocumentRequest-" + proposalDataNode.get("carrierId").asText();
         exchange.setProperty("carrierReqMapConf", ((JsonObject)this.service.getDocBYId(documentId).content()).toString());
         exchange.getIn().setBody(finalDataNode);
       } else {
         exchange.getIn().setHeader("reqFlag", "False");
         ExtendedJsonNode failure = (new ExceptionResponse()).failure("user profile details not found in database :");
         exchange.getIn().setBody(failure);
         throw new ExecutionTerminator();
       } 
     } catch (Exception e) {
       exchange.getIn().setHeader("reqFlag", "False");
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("exception in policy doc viewer request processor :because of details not found  :" + e.getMessage());
       exchange.getIn().setBody(failure);
       throw new ExecutionTerminator();
     } 
   }
 }


