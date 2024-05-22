 package com.idep.carquote.req.processor;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.PBRating.Car.CalculateCarRating;
 import com.idep.carquote.util.CorrelationKeyGenerator;
 import com.idep.couchbase.api.impl.CBInstanceProvider;
 import com.idep.couchbase.api.impl.CBService;
 import com.idep.data.searchconfig.cache.DocumentDataConfig;
 import com.idep.encryption.session.GenrateEncryptionKey;
 import com.idep.listener.core.CalculatePolicyDates;
 import com.idep.listener.core.CarProductValidation;
 import com.idep.service.quote.cache.CarQuoteConfigCache;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import org.apache.camel.CamelContext;
 import org.apache.camel.Exchange;
 import org.apache.camel.ExchangePattern;
 import org.apache.camel.Processor;
 import org.apache.camel.ProducerTemplate;
 import org.apache.log4j.Logger;
 
 public class CarQuoteReqProcessor
   implements Processor {
   static ObjectMapper objectMapper = new ObjectMapper();
   
   Logger log = Logger.getLogger(CarQuoteReqProcessor.class.getName());
   
   CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
   
   CalculatePolicyDates dates = new CalculatePolicyDates();
   
   CBService policyTransaction = CBInstanceProvider.getPolicyTransInstance();
   
   JsonNode errorNode;
   
   String encQuoteId;
   
   CarProductValidation validator = new CarProductValidation();
   
   CalculateCarRating componentList = new CalculateCarRating();
   
   SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
   
   static ObjectNode createObjectNode = objectMapper.createObjectNode();
   
   static {
     List<Map<String, Object>> executeQueryCouchDB1 = CBInstanceProvider.getServerConfigInstance().executeQuery("select REPLACE(make,' ','') || TOSTRING(REPLACE(model,' ','')) ||REPLACE(TOSTRING(variant),' ','') as displayVehicle,variantId from ServerConfig where documentType='carVariant'");
     if (executeQueryCouchDB1 == null)
       executeQueryCouchDB1 = CBInstanceProvider.getServerConfigInstance().executeQuery("select REPLACE(make,' ','') || TOSTRING(REPLACE(model,' ','')) ||REPLACE(TOSTRING(variant),' ','')  as displayVehicle,variantId from ServerConfig where documentType='carVariant'"); 
     ArrayNode convertValue = (ArrayNode)objectMapper.convertValue(executeQueryCouchDB1, ArrayNode.class);
     for (JsonNode objectNode : convertValue)
       createObjectNode.put(objectNode.get("displayVehicle").asText(), objectNode.get("variantId").asText()); 
   }
   
   public void process(Exchange exchange) throws JsonProcessingException, IOException {
     try {
       CamelContext camelContext = exchange.getContext();
       ProducerTemplate template = camelContext.createProducerTemplate();
       String quotedata = exchange.getIn().getBody().toString();
       JsonNode reqNode = objectMapper.readTree(quotedata);
       ArrayNode reqQListNode = objectMapper.createArrayNode();
       reqNode = getCityAndState(reqNode);
       JsonNode generateCashLessGargeRating = this.componentList.generateCarRating(reqNode.toString());
       reqNode = this.dates.calculateDates(reqNode);
       String displayVehicle = String.valueOf(reqNode.findValue("make").asText()) + reqNode.findValue("model").asText() + reqNode.findValue("variant").asText() + reqNode.findValue("cubicCapacity").asText();
       displayVehicle = displayVehicle.replace(" ", "");
       ArrayNode finalProductList = objectMapper.createArrayNode();
       ArrayNode unMappedCarrierIdArray = objectMapper.createArrayNode();
       this.log.info("UI displayVehicle Name : " + displayVehicle);
       if (createObjectNode.has(displayVehicle)) {
         ((ObjectNode)reqNode.get("vehicleInfo")).put("variantId", createObjectNode.get(displayVehicle).asText());
       } else {
         ObjectNode finalResultNode = objectMapper.createObjectNode();
         finalResultNode.put("responseCode", "P365RES101");
         finalResultNode.put("message", "variant id not found for the request");
         finalResultNode.put("data", this.errorNode);
         exchange.getIn().setBody(finalResultNode);
       } 
       
       for (JsonNode product : CarQuoteConfigCache.getcarProductsCache()) {
         boolean validateFlag = this.validator.validateCarProduct(product, reqNode.get("quoteParam"), reqNode.get("vehicleInfo"));
         if (product.get("carrierId").asInt() == 53 && product
           .has("minAllowedVehicleAgeForRenew") && reqNode.get("quoteParam").get("policyType").asText().equals("renew")) {
           validateFlag = validateVehicleMinAge(reqNode.get("quoteParam").get("vehicleAge").doubleValue(), product
               .get("minAllowedVehicleAgeForRenew").doubleValue());
         }
         if (validateFlag)
           finalProductList.add(product); 
       } 
       if (finalProductList.size() > 0) {
         String carQuoteId = String.valueOf(DocumentDataConfig.getConfigDocList().get("DocumentIDConfig").get("carQuoteId").asText()) + this.serverConfig.updateDBSequence("SEQCARQUOTE");
         try {
           if (reqNode.has("calcQuote") && reqNode
             .get("calcQuote").asBoolean()) {
             String messageId = reqNode.findValue("messageId").asText();
             String docId = "LeadProfile-" + messageId;
             JsonNode defaultLeadProfileDoc = objectMapper.readTree(((JsonObject)this.policyTransaction.getDocBYId(docId).content()).toString());
             ((ObjectNode)defaultLeadProfileDoc).put("latestQUOTE_ID", carQuoteId);
             ((ObjectNode)defaultLeadProfileDoc).put("latestQuoteBusinessLineId", 3);
             JsonObject documentContent = JsonObject.fromJson(defaultLeadProfileDoc.toString());
             String doc_status = this.policyTransaction.replaceDocument(docId, documentContent);
             reqNode = getP365VehicleDetails(reqNode);
             this.log.info("quote id updated in lead for RAMP:" + doc_status);
           } 
         } catch (Exception e) {
           this.log.error("exception in updating lead profile for integrating portal ");
         } 
         ArrayNode carQuoteResponses = (new CarQuoteIDVReqProcessor()).getBestQuoteIdDetails(reqNode);
         double userIDV = reqNode.get("vehicleInfo").get("IDV").doubleValue();
         for (JsonNode product : finalProductList) {
           try {
             if (generateCashLessGargeRating.has("garageRating") && generateCashLessGargeRating.get("garageRating").size() > 0) {
               JsonNode generateCashLessGargeRatingArray = generateCashLessGargeRating.get("garageRating");
               for (JsonNode generateCash : generateCashLessGargeRatingArray) {
                 if (generateCash.get("carrierId").asInt() == product.get("carrierId").asInt())
                   ((ObjectNode)product).put("garageIndex", generateCash.get("rating").asDouble()); 
               } 
             } 
             if (reqNode.get("vehicleInfo").has("carrierVariants") && reqNode.get("vehicleInfo").get("carrierVariants").size() > 0) {
               JsonNode carrierVariantsArray = reqNode.get("vehicleInfo").get("carrierVariants");
               for (JsonNode carrierVariants : carrierVariantsArray) {
                 if (carrierVariants.get("carrierId").asInt() == product.get("carrierId").asInt())
                   ((ObjectNode)reqNode.get("vehicleInfo")).put("unVariantId", carrierVariants.get("variantId").asText()); 
               } 
             } 
             this.log.info("request node :" + reqNode);
             reqNode = (new CarQuoteIDVReqProcessor()).process(reqNode, userIDV, (JsonNode)carQuoteResponses, product);
             ObjectNode objectNode = objectMapper.createObjectNode();
             ((ObjectNode)reqNode).put("productInfo", product);
             ((ObjectNode)reqNode).put("QUOTE_ID", carQuoteId);
             JsonNode keyConfigDoc = objectMapper.readTree(((JsonObject)this.serverConfig.getDocBYId("encryptionPrivateKeyConfig").content()).toString());
             this.encQuoteId = GenrateEncryptionKey.GetEncryptedKey(carQuoteId, keyConfigDoc.get("encryptionKey").asText());
             this.log.info("Encrypted QUOTE_ID :" + this.encQuoteId);
             ((ObjectNode)reqNode).put("encryptedQuoteId", this.encQuoteId);
             objectNode.put("inputMessage", reqNode);
             String correlationKey = (new CorrelationKeyGenerator()).getUniqueKey().toString();
             objectNode.put("uniqueKey", correlationKey);
             objectNode.put("QUOTE_ID", carQuoteId);
             objectNode.put("encryptedQuoteId", this.encQuoteId);
             String uri = "activemq:queue:CarrierCarReqQ";
             exchange.getIn().setBody(objectNode.toString());
             exchange.setPattern(ExchangePattern.InOnly);
             template.send(uri, exchange);
             ObjectNode resultNode = objectMapper.createObjectNode();
             resultNode.put("qname", CarQuoteConfigCache.getcarQuoteDocCache().get("CarCarrierQList").get("carrierResQ").get(product.get("carrierId").asText()).textValue());
             resultNode.put("messageId", correlationKey);
             resultNode.put("QUOTE_ID", carQuoteId);
             resultNode.put("encryptedQuoteId", this.encQuoteId);
             resultNode.put("carrierId", product.get("carrierId").asInt());
             resultNode.put("status", 0);
             ObjectNode carQuoteRequest = objectMapper.createObjectNode();
             carQuoteRequest.put("carQuoteRequest", reqNode);
             carQuoteRequest.put("documentType", "carQuoteResults");
             carQuoteRequest.put("businessLineId", 3);
             carQuoteRequest.put("quoteCreatedDate", this.dateFormat.format(new Date()));
             carQuoteRequest.put("QUOTE_ID", carQuoteId);
             if (userIDV > 0.0D)
               ((ObjectNode)carQuoteRequest.get("carQuoteRequest").get("vehicleInfo")).put("IDV", userIDV); 
             carQuoteRequest.put("carrierTransformedReq", (JsonNode)objectMapper.createObjectNode());
             ((ObjectNode)carQuoteRequest.get("carQuoteRequest")).remove("productInfo");
             CBService bucketInstance = CBInstanceProvider.getBucketInstance("QuoteData");
             String status = bucketInstance.createDocument(carQuoteId, JsonObject.fromJson(carQuoteRequest.toString()));
             this.log.info("QuoteId is created in carQuote Request : " + carQuoteId + " Status  is : " + status);
             reqQListNode.add((JsonNode)resultNode);
           } catch (NullPointerException e) {
             this.log.info("NullPointerException at CarQuoteReqProcessor : ", e);
           } 
         } 
         if (reqQListNode.size() > 0) {
 
 
 
           
           this.log.info("reqQListNode details : " + reqQListNode);
           ObjectNode finalresultNode = objectMapper.createObjectNode();
           ObjectNode policyDate = objectMapper.createObjectNode();
           finalresultNode.put("responseCode", "P365RES100");
           finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("successMessage").asText());
           finalresultNode.put("data", (JsonNode)reqQListNode);
           finalresultNode.put("businessLineId", 3);
           if (reqNode.get("quoteParam").get("policyType").textValue().equals("renew")) {
             policyDate.put("PreviousPolicyStartDate", reqNode.get("vehicleInfo").get("PreviousPolicyStartDate").textValue());
             policyDate.put("PreviousPolicyExpiryDate", reqNode.get("vehicleInfo").get("PreviousPolicyExpiryDate").textValue());
           } 
           policyDate.put("policyStartDate", reqNode.get("systemPolicyStartDate").get("sysPolicyStartDate").textValue());
           policyDate.put("policyEndDate", reqNode.get("systemPolicyStartDate").get("sysPolicyEndDate").textValue());
           finalresultNode.put("policyDate", (JsonNode)policyDate);
           finalresultNode.put("QUOTE_ID", carQuoteId);
           finalresultNode.put("unMappedCarrierId", (JsonNode)unMappedCarrierIdArray);
           finalresultNode.put("encryptedQuoteId", this.encQuoteId);
           exchange.getIn().setBody(finalresultNode);
           LeadProfileRequest.sendLeadProfileRequest(finalresultNode, exchange);
         } else {
           ObjectNode finalresultNode = objectMapper.createObjectNode();
           finalresultNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("carQuoteDataErrorCode").asText());
           finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("carQuoteDataErrorMessage").asText());
           finalresultNode.put("data", (JsonNode)reqQListNode);
           exchange.getIn().setBody(finalresultNode);
         } 
       } else {
         ObjectNode finalresultNode = objectMapper.createObjectNode();
         finalresultNode.put("responseCode", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("carQuoteDataErrorCode").asText());
         finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("carQuoteDataErrorMessage").asText());
         finalresultNode.put("data", this.errorNode);
         exchange.getIn().setBody(finalresultNode);
       } 
     } catch (Exception e) {
       this.log.error("Exception at CarQuoteReqProcessor ", e);
       ObjectNode finalresultNode = objectMapper.createObjectNode();
       finalresultNode.put("responseCode", "P365RES101");
       finalresultNode.put("message", DocumentDataConfig.getConfigDocList().get("ResponseMessages").get("errorMessage").asText());
       finalresultNode.put("data", this.errorNode);
       exchange.getIn().setBody(finalresultNode);
     } 
   }
   
   public boolean validateVehicleMinAge(double vehicleAge, double minAllowedVehicleAge) {
     if (vehicleAge >= minAllowedVehicleAge)
       return true; 
     return false;
   }
   
   public JsonNode getP365VehicleDetails(JsonNode inputNode) throws JsonProcessingException, IOException {
     JsonDocument variantIdDoc = this.serverConfig.getDocBYId(inputNode.findValue("variantId").asText());
     JsonNode variantDoc = objectMapper.readTree(((JsonObject)variantIdDoc.content()).toString());
     ((ObjectNode)inputNode.get("vehicleInfo")).put("fuel", variantDoc.get("fuelType").asText());
     ((ObjectNode)inputNode.get("vehicleInfo")).put("cubicCapacity", variantDoc.get("cubicCapacity").asText());
     return inputNode;
   }
   
   public JsonNode getCityAndState(JsonNode inpNode) throws JsonProcessingException, IOException {
     JsonDocument docBYId = this.serverConfig.getDocBYId("RTODetails-" + inpNode.findValue("RTOCode").asText().substring(0, 2) + "-" + inpNode.findValue("RTOCode").asText().substring(2, 4));
     if (docBYId != null) {
       JsonNode readTree = objectMapper.readTree(((JsonObject)docBYId.content()).toString());
       ((ObjectNode)inpNode.get("vehicleInfo")).put("city", readTree.findValue("commonCityName").asText());
       ((ObjectNode)inpNode.get("vehicleInfo")).put("state", readTree.get("state").asText());
     } 
     return inpNode;
   }
 }


