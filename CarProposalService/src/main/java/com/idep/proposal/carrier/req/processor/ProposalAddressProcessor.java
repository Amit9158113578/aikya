 package com.idep.proposal.carrier.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExecutionTerminator;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class ProposalAddressProcessor implements Processor {
   Logger log = Logger.getLogger(ProposalAddressProcessor.class.getName());
   
   JsonNode addressValidationConfig = null;
   
   public void process(Exchange exchange) throws Exception {
     try {
       String proposalRequest = (String)exchange.getIn().getBody(String.class);
       ExtendedJsonNode proposalReqNode = new ExtendedJsonNode(Utils.mapper.readTree(proposalRequest));
       if (this.addressValidationConfig == null) {
         JsonDocument addressConfigDocument = Utils.serverConfig.getDocBYId("ProposerAddressConfig");
         if (addressConfigDocument != null) {
           this.addressValidationConfig = Utils.mapper.readTree(((JsonObject)addressConfigDocument.content()).toString());
         } else {
           this.log.error("ProposerAddressConfig document not found in DB");
         } 
       } 
       int carrierAddressMaxLength = carrierAddressMaxLength(proposalReqNode);
       if (this.addressValidationConfig.get("validateRegAddress").asText().equals("Y"))
         proposalReqNode = validateRegAddress(proposalReqNode, carrierAddressMaxLength); 
       if (this.addressValidationConfig.get("validateCommAddress").asText().equals("Y"))
         proposalReqNode = validateCommAddress(proposalReqNode, carrierAddressMaxLength); 
       exchange.getIn().setBody(proposalReqNode);
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("Exception in Bike Proposal Address Processor  :" + e.getMessage());
       exchange.getIn().setBody(failure);
     } 
   }
   
   public int carrierAddressMaxLength(ExtendedJsonNode proposalReqNode) throws Exception {
     int carrierAddrLength = 30;
     try {
       JsonNode maxLengthNode = this.addressValidationConfig.get("proposerAddressValidation").get("maxlength");
       String carrierId = proposalReqNode.findValueAsText("carrierId");
       if (carrierId != null && maxLengthNode != null)
         carrierAddrLength = maxLengthNode.get(carrierId).asInt(); 
     } catch (Exception e) {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("unable to get the vehicle reg address max length for carrier. hence used default value 30" + e.toString());
       (new ExecutionTerminator()).execution(failure);
     } 
     return carrierAddrLength;
   }
   
   public ExtendedJsonNode validateCommAddress(ExtendedJsonNode proposalReqNode, int carrierAddrLength) throws Exception {
     if (proposalReqNode.get("proposerDetails").get("communicationAddress").get("comDisplayArea") != null) {
       String proposerAddress = proposalReqNode.get("proposerDetails").get("communicationAddress").getKey("comDisplayArea");
       if (proposerAddress.length() <= carrierAddrLength) {
         proposalReqNode.get("proposerDetails").put("addressLine1", proposerAddress);
         proposalReqNode.get("proposerDetails").put("addressLine2", "");
       } else {
         int start = 0;
         int end = carrierAddrLength;
         int i = 0;
         for (i = 1; i <= proposerAddress.length() / carrierAddrLength; i++) {
           proposalReqNode.get("proposerDetails").put("addressLine" + i, proposerAddress.substring(start, end));
           start = end;
           end += carrierAddrLength;
         } 
         if (proposerAddress.length() % carrierAddrLength > 0)
           proposalReqNode.get("proposerDetails").put("addressLine" + i, proposerAddress.substring(start, start + proposerAddress.length() % carrierAddrLength)); 
       } 
     } else {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("please check proposer address details in input request");
       (new ExecutionTerminator()).execution(failure);
     } 
     return proposalReqNode;
   }
   
   public ExtendedJsonNode validateRegAddress(ExtendedJsonNode proposalReqNode, int carrierAddrLength) throws Exception {
     if (proposalReqNode.get("vehicleDetails").get("registrationAddress").get("regDisplayArea") != null) {
       String proposerVehRegAddress = proposalReqNode.get("vehicleDetails").get("registrationAddress").getKey("regDisplayArea");
       if (proposerVehRegAddress.length() <= carrierAddrLength) {
         proposalReqNode.get("vehicleDetails").put("addressLine1", proposerVehRegAddress);
         proposalReqNode.get("vehicleDetails").put("addressLine2", "");
       } else {
         int start = 0;
         int end = carrierAddrLength;
         int i = 0;
         for (i = 1; i <= proposerVehRegAddress.length() / carrierAddrLength; i++) {
           proposalReqNode.get("vehicleDetails").put("addressLine" + i, proposerVehRegAddress.substring(start, end));
           start = end;
           end += carrierAddrLength;
         } 
         if (proposerVehRegAddress.length() % carrierAddrLength > 0)
           proposalReqNode.get("vehicleDetails").put("addressLine" + i, proposerVehRegAddress.substring(start, start + proposerVehRegAddress.length() % carrierAddrLength)); 
       } 
     } else {
       ExtendedJsonNode failure = (new ExceptionResponse()).failure("please check proposer vehicle reg address details in input request");
       (new ExecutionTerminator()).execution(failure);
     } 
     return proposalReqNode;
   }
 }


