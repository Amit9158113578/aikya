 package com.idep.policy.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.listener.core.ProductMetaData;
 import com.idep.proposal.exception.processor.ExceptionResponse;
 import com.idep.proposal.exception.processor.ExtendedJsonNode;
 import com.idep.proposal.util.Utils;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 
 public class CarrierInformation {
   Logger logger = Logger.getLogger(CarrierInformation.class);
   
   public ExtendedJsonNode preProcessing(ExtendedJsonNode masterReqNode, String carrierId, ExtendedJsonNode ProposalValidationDocConfig) throws Exception {
     ProductMetaData productMetaData = new ProductMetaData();
     try {
       this.logger.info("masterReqNode :" + masterReqNode);
       String city = masterReqNode.get("vehicleDetails").get("registrationAddress").getKey("regCity").toUpperCase();
       if (ProposalValidationDocConfig.has("vehicleInfoReq") && ProposalValidationDocConfig.get("vehicleInfoReq").has(carrierId)) {
         JsonObject vehicleDetails = productMetaData.getVehicleDetails(masterReqNode.findValueAsText("variantId"), carrierId);
         masterReqNode.put("carrierVehicleInfo", Utils.mapper.readTree(vehicleDetails.toString()));
       } 
       if (ProposalValidationDocConfig.has("rtoInfoReq") && ProposalValidationDocConfig.get("rtoInfoReq").has(carrierId)) {
         JsonObject rtoDetails = productMetaData.getRTODetails(masterReqNode.findValueAsText("RTOCode"), carrierId, masterReqNode.getKey("businessLineId"));
         masterReqNode.put("carrierRTOInfo", Utils.mapper.readTree(rtoDetails.toString()));
       } 
       if (ProposalValidationDocConfig.has("preInsurerReqCarrierId") && ProposalValidationDocConfig.get("preInsurerReqCarrierId").has(carrierId) && masterReqNode.findValueAsText("insuranceType").equalsIgnoreCase("renew")) {
         JsonObject preInsurerDetails = productMetaData.getPreInsurerMapping(carrierId, masterReqNode.get("insuranceDetails").getKey("insurerId"));
         if (preInsurerDetails != null) {
           masterReqNode.put("preInsurerDetails", Utils.mapper.readTree(preInsurerDetails.toString()));
         } else {
           return (new ExceptionResponse()).quoteInfoNotFound(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("preInsurerNotFoundMsg").asText());
         } 
       } 
       if (ProposalValidationDocConfig.has("districtDetailsReqCarrierId") && ProposalValidationDocConfig.get("districtDetailsReqCarrierId").has(carrierId)) {
         JsonObject districtDetails = productMetaData.getDistrictMapping(carrierId, city);
         if (districtDetails != null) {
           masterReqNode.put("districtDetails", Utils.mapper.readTree(districtDetails.toString()));
         } else {
           return (new ExceptionResponse()).quoteInfoNotFound(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("distrctDetailsNotFoundMsg").asText());
         } 
       } 
       if (ProposalValidationDocConfig.has("cityDetailsReqCarrierId") && ProposalValidationDocConfig.get("cityDetailsReqCarrierId").has(carrierId)) {
         String pincode;
         if (!masterReqNode.get("vehicleDetails").asBoolean("isVehicleAddressSameAsCommun")) {
           pincode = masterReqNode.get("vehicleDetails").get("registrationAddress").getKey("pincode");
         } else {
           pincode = masterReqNode.get("proposerDetails").get("communicationAddress").getKey("comPincode");
         } 
         String query = "select ServerConfig.* from ServerConfig where documentType='CityDetails' and carrierId = " + masterReqNode.findValueAsText("carrierId") + "and pincode ='" + pincode + "'";
         String display = "cityCode,stateCode,areaCode,districtCode";
         List<JsonObject> pincodeDocumentList = Utils.serverConfig.executeConfigQuery(query, display);
         if (pincodeDocumentList.size() > 0) {
           JsonObject pincodeDocument = pincodeDocumentList.get(0);
           masterReqNode.put("cityDetails", Utils.mapper.readTree(pincodeDocument.toString()));
         } else {
           return (new ExceptionResponse()).quoteInfoNotFound(DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("cityDetailsNotFoundMsg").asText());
         } 
       } 
       if (masterReqNode.has("carQuoteRequest") && 
         masterReqNode.findValueAsText("insuranceType").equalsIgnoreCase("renew") && masterReqNode.get("carQuoteRequest").get("vehicleInfo").getKey("previousPolicyExpired").equalsIgnoreCase("Y") && 
         ProposalValidationDocConfig.has("breakInDetailsRequired") && ProposalValidationDocConfig.get("breakInDetailsRequired").has(carrierId)) {
         String documentId = "BreakingLocationMapping-" + city + "-" + carrierId;
         JsonDocument BreakInLocationDetails = Utils.serverConfig.getDocBYId(documentId);
         if (BreakInLocationDetails != null) {
           JsonNode BreakInLocation = Utils.mapper.readTree(((JsonObject)BreakInLocationDetails.content()).toString());
           masterReqNode.put("breakInLocationDetails", BreakInLocation);
         } 
       } 
       JsonObject nomineeDetails = productMetaData.getNomineeRelMapping(carrierId, masterReqNode.get("nominationDetails").getKey("nominationRelationId"));
       if (nomineeDetails != null)
         masterReqNode.put("nomineeDetails", Utils.mapper.readTree(nomineeDetails.toString())); 
       validateRegistrationNumber(masterReqNode);
     } catch (Exception e) {
       return (new ExceptionResponse()).failure("Error in CarrierInformation preProcessing :" + e.getMessage());
     } 
     return masterReqNode;
   }
   
   public ExtendedJsonNode validateRegistrationNumber(ExtendedJsonNode requestNode) throws JsonProcessingException, IOException {
     String pattern = null;
     try {
       String registrationNumber = requestNode.get("vehicleDetails").getKey("registrationNumber");
       if (registrationNumber.length() < 10) {
         HashMap<String, String> RegistrationNumberPatternDocMapNode = new HashMap<>();
         JsonDocument registrationNumberPatternDoc = Utils.serverConfig.getDocBYId("RegistrationNumberPatternDoc");
         if (registrationNumberPatternDoc != null) {
           JsonNode RegistrationNumberPatternDocNode = Utils.mapper.readTree(((JsonObject)registrationNumberPatternDoc.content()).toString());
           RegistrationNumberPatternDocMapNode = (HashMap<String, String>)Utils.mapper.convertValue(RegistrationNumberPatternDocNode, HashMap.class);
           Set<Map.Entry<String, String>> keySet = RegistrationNumberPatternDocMapNode.entrySet();
           for (Map.Entry<String, String> entry : keySet) {
             pattern = entry.getValue();
             Pattern compile = Pattern.compile(pattern);
             if (compile.matcher(registrationNumber).matches()) {
               if (((String)entry.getKey()).equals("pattern4"))
                 registrationNumber = (new StringBuffer(registrationNumber)).insert(registrationNumber.length() - 1, "000").toString(); 
               if (((String)entry.getKey()).equals("pattern5"))
                 registrationNumber = (new StringBuffer(registrationNumber)).insert(registrationNumber.length() - 2, "00").toString(); 
               if (((String)entry.getKey()).equals("pattern6"))
                 registrationNumber = (new StringBuffer(registrationNumber)).insert(registrationNumber.length() - 3, "0").toString(); 
             } 
           } 
           requestNode.get("vehicleDetails").put("registrationNumberFormatted", createFormatedRegNumber(registrationNumber));
           requestNode.get("vehicleDetails").put("registrationNumber", registrationNumber);
         } else {
           this.logger.error("Notable to read pattern document from couchbase database :" + registrationNumberPatternDoc);
         } 
       } else {
         requestNode.get("vehicleDetails").put("registrationNumberFormatted", createFormatedRegNumber(registrationNumber));
         this.logger.info("Registration Number Validation Not Found :");
       } 
     } catch (Exception e) {
       e.getStackTrace();
       this.logger.error("error in CarrierInformation validateRegistration Number method :");
     } 
     return requestNode;
   }
   
   public String createFormatedRegNumber(String registrationNumber) {
     try {
       String[] split = registrationNumber.split("([0-9]+)");
       String[] split2 = registrationNumber.split("([A-Z]+)");
       String formatedRegistration = String.valueOf(String.valueOf(String.valueOf(split[0]))) + "-" + split2[1] + "-" + split[1] + "-" + split2[2];
       return formatedRegistration;
     } catch (Exception e) {
       this.logger.error("create formated number method error");
       return registrationNumber;
     } 
   }
 }


