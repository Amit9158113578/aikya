package com.idep.policy.document.req.processor;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.BikeProposalConstants;
import com.idep.proposal.util.Utils;
import com.idep.user.profile.impl.UserProfileServices;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

public class PolicyPDFSignProcessor implements Processor {
  Logger log = Logger.getLogger(PolicyPDFSignProcessor.class.getName());
  
  CBService service = CBInstanceProvider.getServerConfigInstance();
  
  CBService transService = CBInstanceProvider.getPolicyTransInstance();
  
  UserProfileServices profileServices = new UserProfileServices();
  
  public void process(Exchange exchange) throws Exception {
    try {
      String inputReq = (String)exchange.getIn().getBody(String.class);
      JsonNode policyPDFDataNode = Utils.mapper.readTree(inputReq);
      ObjectNode pdfReqNode = Utils.mapper.createObjectNode();
      String userSecretKey = policyPDFDataNode.get("uKey").textValue();
      String policySecretKey = policyPDFDataNode.get("pKey").textValue();
      JsonNode userProfileDetailsNode = this.profileServices.getUserProfileByUkey(userSecretKey);
      if (userProfileDetailsNode == null) {
        this.log.info("user profile not found.. retry after 10 seconds");
        Thread.sleep(20000L);
        userProfileDetailsNode = this.profileServices.getUserProfileByUkey(userSecretKey);
      } 
      if (userProfileDetailsNode != null) {
        JsonNode userPersonalInfo = userProfileDetailsNode.get("userProfile");
        JsonNode policyNode = this.profileServices.getPolicyRecordByPkey(userPersonalInfo.get("mobile").asText(), policySecretKey);
        try {
          ObjectNode proposalDocument = (ObjectNode)Utils.mapper.readTree(((JsonObject)this.transService.getDocBYId(policyNode.get("proposalId").asText()).content()).toString());
          if (proposalDocument != null) {
            JsonNode paymentResponseNode = Utils.mapper.readTree(((JsonObject)this.transService.getDocBYId(proposalDocument.get("paymentResponse").get("apPreferId").asText()).content()).toString());
            try {
              if (paymentResponseNode != null) {
                pdfReqNode.put("TransactionReference", paymentResponseNode.get("clientResponse").get("mihpayid").textValue());
                pdfReqNode.put("carrierPolicyResponse", proposalDocument.get("bikePolicyResponse"));
                pdfReqNode.put("name", proposalDocument.get("firstName").asText());
                pdfReqNode.put("requestType", "BikePolicyPDFSignRequest");
                pdfReqNode.put(BikeProposalConstants.CARRIER_ID, proposalDocument.get(BikeProposalConstants.CARRIER_ID).asInt());
                pdfReqNode.put("productId", proposalDocument.get("productId").asInt());
                String pdfData = policyPDFDataNode.get("policyDocStream").textValue().replaceAll("data:application/pdf;base64,", "");
                pdfReqNode.put("signPDFInput", pdfData);
                exchange.setProperty("BikePolicyPDFSignRequestSample", "BikePolicyPDFSignRequest-" + 
                    proposalDocument.get(BikeProposalConstants.CARRIER_ID).asInt() + "-" + proposalDocument.get("productId").asInt() + "-sample");
                exchange.getIn().setBody(pdfReqNode);
              } 
            } catch (Exception e) {
              this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "PDFSINFPRO" + "|ERROR|" + "payment details not found policy pdf sign req processing failed :");
            } 
          } 
        } catch (Exception e) {
          this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "PDFSINFPRO" + "|ERROR|" + "proposal details not found policy pdf sign req processing failed :");
        } 
      } 
    } catch (Exception e) {
      this.log.error(String.valueOf(exchange.getProperty("logReq").toString()) + "PDFSINFPRO" + "|ERROR|" + "policy pdf sign req processing failed :", e);
    } 
  }
}
