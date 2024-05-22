 package com.idep.proposal.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.listener.core.ProductMetaData;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import org.apache.camel.Exchange;
 import org.apache.camel.Processor;
 import org.apache.log4j.Logger;
 
 public class MasterProposalReqProcessor implements Processor {
   ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(MasterProposalReqProcessor.class.getName());
   
   CBService service = null;
   
   CBService policy = null;
   
   JsonNode serviceConfigNode = null;
   
   JsonNode ProposalValidationDocConfig = null;
   
   public void process(Exchange inExchange) {
     try {
       String inputmsg = (String)inExchange.getIn().getBody(String.class);
       JsonNode masterReqNode = this.objectMapper.readTree(inputmsg);
       if (this.service == null) {
         this.service = CBInstanceProvider.getServerConfigInstance();
         this.serviceConfigNode = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("CarProposalServiceURLConfig").content()).toString());
         this.ProposalValidationDocConfig = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("ProposalValidationDocConfig").content()).toString());
       } 
       masterReqNode = preProcessing(masterReqNode, masterReqNode.get("carrierId").asText());
       String deviceId = inExchange.getIn().getHeader("deviceId").toString();
       ((ObjectNode)masterReqNode).put("deviceId", deviceId);
       boolean validatePolicyDates = validatePolicyDates(masterReqNode.findValue("policyStartDate").textValue());
       if (validatePolicyDates) {
         this.log.info("This policy is expired. Please check current policy startdate will be greate than or equals to todays date :" + masterReqNode.findValue("policyStartDate").textValue());
         ObjectNode obj = this.objectMapper.createObjectNode();
         ObjectNode quoteRequestNode = this.objectMapper.createObjectNode();
         quoteRequestNode.put("previousPolicyExpired", "Y");
         obj.put("responseCode", this.ProposalValidationDocConfig.get("ResponseMessages").get("policyExpiredResCode").asInt());
         obj.put("message", String.valueOf(String.valueOf(String.valueOf(this.ProposalValidationDocConfig.get("ResponseMessages").get("policyExpiredMsg").asText()))) + masterReqNode.findValue("policyStartDate").textValue());
         obj.put("data", (JsonNode)quoteRequestNode);
         inExchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
       } else if (masterReqNode.has("preInsrerDetailsNotFound") && masterReqNode.findValue("policyType").asText().equalsIgnoreCase("renew")) {
         ObjectNode obj = this.objectMapper.createObjectNode();
         obj.put("responseCode", this.ProposalValidationDocConfig.get("ResponseMessages").get("proposalValidationResCode").asInt());
         obj.put("message", String.valueOf(String.valueOf(String.valueOf(this.ProposalValidationDocConfig.get("ResponseMessages").get("preInsurerNotFoundMsg").asText()))) + masterReqNode.findValue("insurerName").textValue());
         obj.put("data", String.valueOf(String.valueOf(String.valueOf(this.ProposalValidationDocConfig.get("ResponseMessages").get("preInsurerNotFoundMsg").asText()))) + masterReqNode.findValue("insurerName").textValue());
         inExchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
       } else if (masterReqNode.has("districtDetailsNotFound")) {
         ObjectNode obj = this.objectMapper.createObjectNode();
         obj.put("responseCode", this.ProposalValidationDocConfig.get("ResponseMessages").get("proposalValidationResCode").asInt());
         obj.put("message", this.ProposalValidationDocConfig.get("ResponseMessages").get("distrctDetailsNotFoundMsg").asText());
         obj.put("data", this.ProposalValidationDocConfig.get("ResponseMessages").get("distrctDetailsNotFoundMsg").asText());
         inExchange.getIn().setBody(this.objectMapper.writeValueAsString(obj));
       } else {
         inExchange.getIn().setHeader("carproposalService", this.serviceConfigNode.get("Proposal-" + masterReqNode.get("carrierId").intValue() + 
               "-" + masterReqNode.get("productId").intValue()).textValue());
         inExchange.getIn().setBody(this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(masterReqNode));
       } 
     } catch (JsonProcessingException e) {
       this.log.error("JsonProcessingException at MasterProposalReqProcessor", (Throwable)e);
     } catch (IOException e) {
       this.log.error("IOException at MasterProposalReqProcessor", e);
     } catch (Exception e) {
       this.log.error("Exception at MasterProposalReqProcessor", e);
     } 
   }
   
   public JsonNode preProcessing(JsonNode masterReqNode, String carrierId) throws JsonProcessingException, IOException {
     String preInsurerCarrierId = null;
     String nominationRelationId = null;
     String occupationId = null;
     try {
       if (masterReqNode.get("insuranceDetails").has("insurerId"))
         preInsurerCarrierId = masterReqNode.get("insuranceDetails").get("insurerId").asText(); 
       if (masterReqNode.get("nominationDetails").has("nominationRelationId"))
         nominationRelationId = masterReqNode.get("nominationDetails").get("nominationRelationId").asText(); 
       if (masterReqNode.get("proposerDetails").has("occupationId"))
         occupationId = masterReqNode.get("proposerDetails").get("occupationId").asText(); 
     } catch (Exception e) {
       this.log.error("error in getting insurerId,nominationRelationId and occupationId details :");
     } 
     ObjectNode objectNode1 = this.objectMapper.createObjectNode();
     ObjectNode objectNode2 = this.objectMapper.createObjectNode();
     ObjectNode objectNode3 = this.objectMapper.createObjectNode();
     ObjectNode objectNode4 = this.objectMapper.createObjectNode();
     try {
       ProductMetaData productMetaData = new ProductMetaData();
       JsonObject preInsurerDetails = productMetaData.getPreInsurerMapping(carrierId, preInsurerCarrierId);
       if (preInsurerDetails != null) {
         JsonNode jsonNode = this.objectMapper.readTree(preInsurerDetails.toString());
         ((ObjectNode)masterReqNode).put("preInsurerDetails", jsonNode);
       } else if (this.ProposalValidationDocConfig.get("preInsurerReqCarrierId").has(carrierId)) {
         ((ObjectNode)masterReqNode).put("preInsrerDetailsNotFound", true);
       } 
       JsonObject nomineeDetails = productMetaData.getNomineeRelMapping(carrierId, nominationRelationId);
       if (nomineeDetails != null) {
         JsonNode jsonNode = this.objectMapper.readTree(nomineeDetails.toString());
         ((ObjectNode)masterReqNode).put("nomineeDetails", jsonNode);
       } 
       String city = masterReqNode.get("vehicleDetails").get("city").asText().toUpperCase();
       JsonObject districtDetails = productMetaData.getDistrictMapping(carrierId, city);
       if (districtDetails != null) {
         JsonNode jsonNode = this.objectMapper.readTree(districtDetails.toString());
         ((ObjectNode)masterReqNode).put("districtDetails", jsonNode);
       } else if (this.ProposalValidationDocConfig.get("districtDetailsReqCarrierId").has(carrierId)) {
         ((ObjectNode)masterReqNode).put("districtDetailsNotFound", true);
       } 
       String carRegCity = masterReqNode.get("vehicleDetails").get("city").asText().toUpperCase();
       JsonObject carRegDistrictDetails = productMetaData.getDistrictMapping(carrierId, carRegCity);
       if (carRegDistrictDetails != null) {
         JsonNode carRegDistrictDetailsNode = this.objectMapper.readTree(carRegDistrictDetails.toString());
         ((ObjectNode)masterReqNode).put("CarRegdistrictDetails", carRegDistrictDetailsNode);
       } 
       if (occupationId != null) {
         JsonObject occupationDetails = productMetaData.getOccupationDetails(occupationId, carrierId);
         if (occupationDetails != null) {
           JsonNode jsonNode = this.objectMapper.readTree(occupationDetails.toString());
           ((ObjectNode)masterReqNode).put("occupationDetails", jsonNode);
         } 
       } 
     } catch (NullPointerException e) {
       this.log.error("exception in getting preInsurerDetails,nomineeDetails,districtDetails from DB :");
     } 
     try {
       if (masterReqNode.get("vehicleDetails").has("registrationNumber")) {
         ObjectNode UIResponse = this.objectMapper.createObjectNode();
         JsonNode MobileCarVariantDoc = null;
         this.policy = CBInstanceProvider.getPolicyTransInstance();
         JsonDocument registrationNumberDoc = this.policy.getDocBYId(masterReqNode.get("vehicleDetails").get("registrationNumber").asText());
         if (masterReqNode.get("vehicleDetails").has("variantId"))
           MobileCarVariantDoc = this.objectMapper.readTree(((JsonObject)this.service.getDocBYId("Mobile" + masterReqNode.get("vehicleDetails").get("variantId").textValue()).content()).toString()); 
         if (registrationNumberDoc != null) {
           JsonNode jsonNode = this.objectMapper.readTree(((JsonObject)registrationNumberDoc.content()).toString());
           if (jsonNode.has("UIResponse") && jsonNode.get("UIResponse").textValue() != null) {
             JsonNode UIResponseNode = jsonNode.get("UIResponse");
             if (!UIResponseNode.has("variantId")) {
               if (masterReqNode.get("vehicleDetails").has("variantId")) {
                 ((ObjectNode)jsonNode.get("UIResponse")).put("variantId", masterReqNode.get("vehicleDetails").get("variantId").textValue());
                 ((ObjectNode)jsonNode.get("UIResponse")).put("displayVehicle", MobileCarVariantDoc.get("displayVehicle").textValue());
                 ((ObjectNode)jsonNode.get("UIResponse")).put("registrationDate", masterReqNode.get("vehicleDetails").get("registrationDate").asText());
               } 
             } else {
               this.log.info("registration number document already create successfully :" + masterReqNode.get("vehicleDetails").get("registrationNumber").asText());
               return masterReqNode;
             } 
           } else {
             UIResponse.put("variantId", masterReqNode.get("vehicleDetails").get("variantId").textValue());
             UIResponse.put("displayVehicle", MobileCarVariantDoc.get("displayVehicle").textValue());
             UIResponse.put("registrationDate", masterReqNode.get("vehicleDetails").get("registrationDate").asText());
             if (masterReqNode.get("vehicleDetails").has("regYear"))
               UIResponse.put("registrationYear", masterReqNode.get("vehicleDetails").get("regYear").asText()); 
             ((ObjectNode)jsonNode).put("UIResponse", (JsonNode)UIResponse);
           } 
           JsonObject jsonObject = JsonObject.fromJson(this.objectMapper.readTree(jsonNode.toString()).toString());
           String doc_status = this.policy.replaceDocument(masterReqNode.get("vehicleDetails").get("registrationNumber").asText(), jsonObject);
           this.log.info(" registration number document updated :  " + masterReqNode.get("vehicleDetails").get("registrationNumber").asText());
           return masterReqNode;
         } 
         ObjectNode registrationNumberNode = this.objectMapper.createObjectNode();
         this.log.info("registration number document not found in policyTransaction Bucket :" + masterReqNode.get("vehicleDetails").get("registrationNumber").asText());
         UIResponse.put("variantId", masterReqNode.get("vehicleDetails").get("variantId").textValue());
         UIResponse.put("displayVehicle", MobileCarVariantDoc.get("displayVehicle").textValue());
         UIResponse.put("registrationDate", masterReqNode.get("vehicleDetails").get("registrationDate").asText());
         if (masterReqNode.get("vehicleDetails").has("regYear"))
           UIResponse.put("registrationYear", masterReqNode.get("vehicleDetails").get("regYear").asText()); 
         registrationNumberNode.put("UIResponse", (JsonNode)UIResponse);
         JsonObject docObj = JsonObject.fromJson(this.objectMapper.readTree(registrationNumberNode.toString()).toString());
         this.policy.createDocument(masterReqNode.get("vehicleDetails").get("registrationNumber").asText(), docObj);
         this.log.info(" registration number document Crerated :  " + masterReqNode.get("vehicleDetails").get("registrationNumber").asText());
         return masterReqNode;
       } 
       return masterReqNode;
     } catch (Exception e) {
       this.log.error("failed to retrieve occupationDetails,CarRegdistrictDetails,districtDetails and nominee details mappping data :", e);
       return masterReqNode;
     } 
   }
   
   public boolean validatePolicyDates(String policyStartDate) throws ParseException {
     SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
     Calendar cal = new GregorianCalendar();
     String today = dateFormat.format(cal.getTime());
     Date currentPolicyStartDate = dateFormat.parse(policyStartDate);
     Date todays = dateFormat.parse(today);
     if (currentPolicyStartDate.before(todays))
       return true; 
     return false;
   }
 }


