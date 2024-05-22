 package com.idep.service.payment.util;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonArray;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.impl.PaymentDataAccessor;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriInfo;
 import org.apache.log4j.Logger;
 
 public class CommonLib
 {
   private static Logger log = Logger.getLogger(CommonLib.class.getName());
   
   private static ObjectMapper objectMapper = new ObjectMapper();
   
   private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
   
   public static String formSuccessURLByList(MultivaluedMap<String, String> formParams, JsonNode carrierNode, JsonNode masterNode, String defaultURL) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
     log.info("Client Param Map :" + formParams.toString());
     log.info("Carrier config : " + carrierNode);
     log.info("Default URL : " + defaultURL);
     String returnURL = "";
     String URLParam = "?";
     String key = null;
     String value = null;
     ObjectNode paymentRecord = objectMapper.createObjectNode();
     PaymentConstant.PROPERTIES.getClass();
     PaymentConstant.PROPERTIES.getClass();
     JsonNode paymentStatusNodes = carrierNode.get("params").get("paymentStatus");
     returnURL = defaultURL;
     boolean payStatusFlag = false;
     if (paymentStatusNodes != null) {
       if (carrierNode.has("decryptionKey")) {
         formParams = CheckSumUtil.futureGenResChecksum(formParams, carrierNode.get("decryptionKey").asText());
       }
       for (JsonNode paymentStatusNode : paymentStatusNodes) {
         PaymentConstant.PROPERTIES.getClass();
         String paymentStatusDefaultValue = paymentStatusNode.get("successValue").asText();
         PaymentConstant.PROPERTIES.getClass();
         String paymentStatusRespValue = (String)formParams.getFirst(paymentStatusNode.get("transStatusField").asText());
         boolean isPresentCondition = false;
         PaymentConstant.PROPERTIES.getClass();
         if (paymentStatusNode.get("isPresentCondition") != null) {
           PaymentConstant.PROPERTIES.getClass();
           isPresentCondition = paymentStatusNode.get("isPresentCondition").asBoolean();
         } 
         if ((paymentStatusRespValue != null && paymentStatusDefaultValue.equalsIgnoreCase(paymentStatusRespValue)) || (isPresentCondition && paymentStatusRespValue != null)) {
           
           PaymentConstant.PROPERTIES.getClass();
           log.info("Success Response, verfied for field :" + paymentStatusNode.get("transStatusField").asText() + " and value :" + paymentStatusRespValue);
           PaymentConstant.PROPERTIES.getClass();
           returnURL = carrierNode.get("successurl").asText();
           payStatusFlag = true;
           break;
         } 
       } 
       if (payStatusFlag) {
         PaymentConstant.PROPERTIES.getClass();
         JsonNode sucessParamNodes = carrierNode.get("params").get("success");
         PaymentConstant.PROPERTIES.getClass();
         paymentRecord.put("paymentStatus", "success");
         for (JsonNode paramNode : sucessParamNodes) {
           PaymentConstant.PROPERTIES.getClass();
           key = paramNode.get("appKey").asText();
           PaymentConstant.PROPERTIES.getClass();
           value = (String)formParams.getFirst(paramNode.get("clientKey").asText());
           paymentRecord.put(key, value);
           log.info("paymentRecord :" + paymentRecord);
           PaymentConstant.PROPERTIES.getClass();
           if (paramNode.get("isUIRequired").asBoolean()) {
             if (value == null || value.trim().length() == 0) {
               log.info("Payment response is consider as failed due to unavailblity of " + key);
               PaymentConstant.PROPERTIES.getClass();
               returnURL = carrierNode.get("failureurl").asText();
               break;
             } 
             URLParam = String.valueOf(URLParam) + key + "=" + value + "&";
           } 
         } 
         if (value != null) {
           returnURL = String.valueOf(returnURL) + URLParam;
           PaymentConstant.PROPERTIES.getClass();
           paymentRecord.put("paymentStatus", "success");
           log.info("Payment response is considered as success, all the fields are verified!");
         } 
       } 
     } 
     String docId = createPaymentResponseRecord(paymentRecord, carrierNode, masterNode.get("masterParams"), formParams);
     if (!returnURL.contains("?"))
       returnURL = String.valueOf(returnURL) + "?"; 
     returnURL = String.valueOf(returnURL) + "apPreferId=" + docId;
     return returnURL;
   }
   
   public static String createPaymentRequestRecord(ObjectNode paymentRecord) {
     log.info("create Payment Request Records :");
     PaymentConstant.PROPERTIES.getClass();
     paymentRecord.put("documentType", "PaymentRequestDetails");
     log.info("Payment request record is saving into database : " + paymentRecord);
     PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
     String documentId = paymentDataAccessor.createDatabaseRecord(paymentRecord, "request");
     return documentId;
   }
   
   public static ObjectNode createPolicyParamConfig(MultivaluedMap<String, String> formParams, JsonNode paymentResponseParamConfig, String carrier, String lob, UriInfo info) {
     log.info("Inside createPolicyParamConfig method");
     ObjectNode paymentGatewayResponse = objectMapper.createObjectNode();
     String paymentReferenceId = null;
     String paramKey = null;
     try {
       log.info("formParams : " + formParams);
       log.info("paymentResponseParamConfig new : " + paymentResponseParamConfig);
       log.info("carrier : " + carrier);
       log.info("lob : " + lob);
       PaymentConstant.PROPERTIES.getClass();
       if (paymentResponseParamConfig.get("carriers").get(carrier).has("decryptionKey"))
       {
         formParams = CheckSumUtil.futureGenResChecksum(formParams, paymentResponseParamConfig.get("carriers").get(carrier).get("decryptionKey").asText());
       }
       if (paymentResponseParamConfig.get("carriers").get(carrier).has("breakInCar")) {
         PaymentConstant.PROPERTIES.getClass();
         if (formParams.containsKey(paymentResponseParamConfig.get("carriers").get(carrier).get("breakInCar").asText())) {
           PaymentConstant.PROPERTIES.getClass();
           paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get("breakInCar").asText();
         } else {
           PaymentConstant.PROPERTIES.getClass();
           if (paymentResponseParamConfig.get("carriers").get(carrier).has("renewalPlan")) {
             PaymentConstant.PROPERTIES.getClass();
             if (formParams.containsKey(paymentResponseParamConfig.get("carriers").get(carrier).get("renewalPlan").asText())) {
               log.info("renewal plan");
               PaymentConstant.PROPERTIES.getClass();
               paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get("renewalPlan").asText();
             } else {
               PaymentConstant.PROPERTIES.getClass();
               paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
             } 
           } else {
             PaymentConstant.PROPERTIES.getClass();
             paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
           } 
         } 
       } else {
         PaymentConstant.PROPERTIES.getClass();
         paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
       } 
       log.info("paramKey : " + paramKey);
       if (info != null) {
         MultivaluedMap<String, String> formGetParams = info.getQueryParameters();
         PaymentConstant.PROPERTIES.getClass();
         PaymentConstant.PROPERTIES.getClass();
         String methodType = paymentResponseParamConfig.get("carriers").get(carrier).get("methodType").asText();
         if (formGetParams != null && formGetParams.size() != 0 && methodType.equalsIgnoreCase("GET")) {
           log.info("Payment Response Client Reponse Get Params : " + formGetParams);
           paymentReferenceId = (String)formGetParams.getFirst(paramKey);
         } else {
           log.info("Payment Response Client Reponse Get Params info is null");
           paymentReferenceId = (String)formParams.getFirst(paramKey);
         } 
       } else {
         log.info("Payment Response Client Reponse Get Params info is null");
         paymentReferenceId = (String)formParams.getFirst(paramKey);
       } 
       log.info("paymentReferenceId : " + paymentReferenceId);
       String proposalId = fetchProposalId(lob, paymentReferenceId);
       PaymentConstant.PROPERTIES.getClass();
       paymentGatewayResponse.put("proposalId", proposalId);
       log.info("paymentGatewayResponse : " + paymentGatewayResponse);
     } catch (Exception ex) {
       log.info("Exception : Create Policy Param Config Method : ", ex);
     } 
     return paymentGatewayResponse;
   }
   
   public static ObjectNode createPolicyParamConfig(JsonNode formParams, JsonNode paymentResponseParamConfig, String carrier, String lob, UriInfo info) {
     log.info("Inside createPolicyParamConfig method");
     ObjectNode paymentGatewayResponse = objectMapper.createObjectNode();
     String paymentReferenceId = null;
     try {
       log.info("formParams : " + formParams);
       log.info("paymentResponseParamConfig new : " + paymentResponseParamConfig);
       log.info("carrier : " + carrier);
       log.info("lob : " + lob);
       PaymentConstant.PROPERTIES.getClass();
       String paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
       log.info("paramKey : " + paramKey);
       if (info != null) {
         MultivaluedMap<String, String> formGetParams = info.getQueryParameters();
         PaymentConstant.PROPERTIES.getClass();
         PaymentConstant.PROPERTIES.getClass();
         String methodType = paymentResponseParamConfig.get("carriers").get(carrier).get("methodType").asText();
         if (formGetParams != null && formGetParams.size() != 0 && methodType.equalsIgnoreCase("GET")) {
           log.info("Payment Response Client Reponse Get Params : " + formGetParams);
           paymentReferenceId = (String)formGetParams.getFirst(paramKey);
         } else {
           log.info("Payment Response Client Reponse Get Params info is null");
           paymentReferenceId = formParams.get(paramKey).asText();
         } 
       } else {
         log.info("Payment Response Client Reponse Get Params info is null");
         paymentReferenceId = formParams.get(paramKey).asText();
       } 
       log.info("paymentReferenceId : " + paymentReferenceId);
       String proposalId = fetchProposalId(lob, paymentReferenceId);
       PaymentConstant.PROPERTIES.getClass();
       paymentGatewayResponse.put("proposalId", proposalId);
       log.info("paymentGatewayResponse : " + paymentGatewayResponse);
     } catch (Exception ex) {
       log.info("Exception : Create Policy Param Config Method : ", ex);
     } 
     return paymentGatewayResponse;
   }
   
   public static String fetchProposalId(String lineOfBusiness, String payResRefKey) {
     String proposalId = "";
     String query = "";
     JsonArray paramArray = JsonArray.create();
     try {
       if (lineOfBusiness.equalsIgnoreCase("bike")) {
         query = "select meta().id as documentId from PolicyTransaction where documentType='bikeProposalRequest' and paymentRequest.payReqRefVal=$1";
       } else if (lineOfBusiness.equalsIgnoreCase("car")) {
         query = "select meta().id as documentId from PolicyTransaction where documentType='carProposalRequest' and paymentRequest.payReqRefVal=$1";
       } else if (lineOfBusiness.equalsIgnoreCase("health")) {
         query = "select meta().id as documentId from PolicyTransaction where documentType='healthProposalRequest' and paymentRequest.payReqRefVal=$1";
       } else if (lineOfBusiness.equalsIgnoreCase("travel")) {
         query = "select meta().id as documentId from PolicyTransaction where documentType='travelProposalRequest' and paymentRequest.payReqRefVal=$1";
       } else if (lineOfBusiness.equalsIgnoreCase("personalaccident")) {
         query = "select meta().id as documentId from PolicyTransaction where documentType='personalAccidentProposalRequest' and paymentRequest.payReqRefVal=$1";
       } 
       paramArray.add(payResRefKey);
       PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
       proposalId = paymentDataAccessor.executeDbQueryWithParam(query, paramArray);
     } catch (Exception ex) {
       log.info("Exception : Fetch Proposal ID Method : ", ex);
     } 
     return proposalId;
   }
   
   public static String updateProposalDocument(String paymentRequestDocId, String proposalId) {
     try {
       log.info("Inside updateProposalDocument.");
       JsonNode businessLines = objectMapper.readTree("{\"1\":\"life\", \"2\":\"bike\", \"3\":\"car\", \"4\":\"health\", \"5\":\"travel\", \"7\":\"home\", \"8\":\"personalaccident\"}");
       PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
       JsonDocument proposalDocument = paymentDataAccessor.fetchDocumentById("policyTransaction", proposalId);
       JsonNode paymentReqParamDocument = paymentDataAccessor.fetchDBDocument(paymentRequestDocId, "policyTransaction");
       JsonNode carrierConfigDocument = paymentDataAccessor.fetchDBDocument("CarrierConfig", "serverConfig");
       JsonNode paymentRequestParamConfig = paymentDataAccessor.fetchDBDocument("PaymentRequestParamConfig", "serverConfig");
       JsonNode paymentResponseParamConfig = paymentDataAccessor.fetchDBDocument("PaymentResponseParamConfig", "serverConfig");
       log.info("paymentReqParamDocument : " + paymentReqParamDocument);
       log.info("carrierConfigDocument : " + carrierConfigDocument);
       PaymentConstant.PROPERTIES.getClass();
       String carrierId = ((JsonObject)proposalDocument.content()).get("carrierId").toString();
       PaymentConstant.PROPERTIES.getClass();
       String businessLineId = ((JsonObject)proposalDocument.content()).get("businessLineId").toString();
       String carrierName = carrierConfigDocument.get(carrierId).asText();
       String lineOfBusiness = businessLines.get(businessLineId).asText();
       JsonNode proposalDoc = objectMapper.readTree(((JsonObject)proposalDocument.content()).toString());
       if (proposalDoc.has("bikeProposalResponse") && proposalDoc.get("bikeProposalResponse").has("renewalPlan"))
         lineOfBusiness = "renewalPlan"; 
       log.info("carrierId : " + carrierId);
       log.info("businessLineId : " + businessLineId);
       log.info("carrierName : " + carrierName);
       log.info("lineOfBusiness : " + lineOfBusiness);
       PaymentConstant.PROPERTIES.getClass();
       log.info("paymentRequestParamConfig.carriers : " + paymentRequestParamConfig.get("carriers"));
       PaymentConstant.PROPERTIES.getClass();
       log.info("paymentRequestParamConfig.carriers.carrierName : " + paymentRequestParamConfig.get("carriers").get(carrierName));
       PaymentConstant.PROPERTIES.getClass();
       JsonNode payReqCarrierNameParam = paymentRequestParamConfig.get("carriers").get(carrierName);
       String payReqUrlParamKey = payReqCarrierNameParam.get(lineOfBusiness).asText();
       PaymentConstant.PROPERTIES.getClass();
       String methodType = payReqCarrierNameParam.get("methodType").asText();
       log.info("payReqCarrierNameParam : " + payReqCarrierNameParam);
       log.info("payReqUrlParamKey : " + payReqUrlParamKey);
       log.info("methodType : " + methodType);
       PaymentConstant.PROPERTIES.getClass();
       JsonNode payResCarrierNameParam = paymentResponseParamConfig.get("carriers").get(carrierName);
       String payResUrlParamKey = payResCarrierNameParam.get(lineOfBusiness).asText();
       log.info("payResCarrierNameParam : " + payResCarrierNameParam);
       log.info("payResUrlParamKey : " + payResUrlParamKey);
       String payReqUrlParamVal = "";
       if (methodType.equalsIgnoreCase("GET")) {
         payReqUrlParamVal = proposalId;
       } else {
         log.info("paymentReqParamDocument :" + paymentReqParamDocument);
         PaymentConstant.PROPERTIES.getClass();
         for (JsonNode node : paymentReqParamDocument.get("paramterList")) {
           log.info("node requestParam :" + node);
           PaymentConstant.PROPERTIES.getClass();
           String paramName = node.get("name").asText();
           if (paramName.equalsIgnoreCase(payReqUrlParamKey)) {
             PaymentConstant.PROPERTIES.getClass();
             payReqUrlParamVal = node.get("value").asText();
             break;
           } 
         } 
       } 
       log.info("payReqUrlParamVal : " + payReqUrlParamVal);
       ObjectNode paymentRequestRecord = objectMapper.createObjectNode();
       paymentRequestRecord.put("payReqRefId", paymentRequestDocId);
       paymentRequestRecord.put("payReqRefVal", payReqUrlParamVal);
       paymentRequestRecord.put("payReqRefKey", payResUrlParamKey);
       JsonObject proposalDocumentFinal = ((JsonObject)proposalDocument.content()).put("paymentRequest", JsonObject.fromJson(objectMapper.writeValueAsString(paymentRequestRecord)));
       return paymentDataAccessor.replaceDocument("policyTransaction", proposalId, proposalDocumentFinal);
     } catch (Exception e) {
       log.info("Update Proposal Document Exception : ", e);
       return null;
     } 
   }
   
   public static ObjectNode updateReturnURL(ObjectNode paymentReqParams, String proposalNumber) {
     try {
       log.info("Inside updateReturnURL.");
       JsonNode businessLines = objectMapper.readTree("{\"1\":\"life\", \"2\":\"bike\", \"3\":\"car\", \"4\":\"health\", \"5\":\"travel\", \"7\":\"home\", \"8\":\"personalaccident\"}");
       PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
       JsonNode proposalDocument = paymentDataAccessor.fetchDBDocument(proposalNumber, "policyTransaction");
       JsonNode carrierConfigDocument = paymentDataAccessor.fetchDBDocument("CarrierConfig", "serverConfig");
       JsonNode paymentRequestParamConfig = paymentDataAccessor.fetchDBDocument("PaymentRequestParamConfig", "serverConfig");
       PaymentConstant.PROPERTIES.getClass();
       String carrierId = proposalDocument.get("carrierId").asText();
       PaymentConstant.PROPERTIES.getClass();
       String businessLineId = proposalDocument.get("businessLineId").asText();
       String carrierName = carrierConfigDocument.get(carrierId).asText();
       String lineOfBusiness = businessLines.get(businessLineId).asText();
       log.info("carrierId : " + carrierId);
       log.info("businessLineId : " + businessLineId);
       log.info("carrierName : " + carrierName);
       log.info("lineOfBusiness : " + lineOfBusiness);
       log.info("paymentRequestParamConfig : " + paymentRequestParamConfig);
       PaymentConstant.PROPERTIES.getClass();
       JsonNode paymentParam = paymentRequestParamConfig.get("carriers").get(carrierName);
       log.info("paymentParam : " + paymentParam);
       String urlParam = paymentParam.get(lineOfBusiness).asText();
       PaymentConstant.PROPERTIES.getClass();
       String methodType = paymentParam.get("methodType").asText();
       log.info("urlParam : " + urlParam);
       log.info("methodType : " + methodType);
       String returnUrlKey = "";
       PaymentConstant.PROPERTIES.getClass();
       if (paymentParam.get("returnUrlKey") != null) {
         PaymentConstant.PROPERTIES.getClass();
         returnUrlKey = paymentParam.get("returnUrlKey").asText();
       } else {
         return paymentReqParams;
       } 
       log.info("returnUrlKey : " + returnUrlKey);
       log.info("paymentReqParams : " + paymentReqParams);
       if (methodType.equalsIgnoreCase("GET")) {
         ArrayNode payParamList = objectMapper.createArrayNode();
         for (JsonNode node : paymentReqParams.get("paramterList")) {
           PaymentConstant.PROPERTIES.getClass();
           String paramName = node.get("name").asText();
           if (paramName.equalsIgnoreCase(returnUrlKey)) {
             PaymentConstant.PROPERTIES.getClass();
             String returnURL = node.get("value").asText();
             returnURL = String.valueOf(returnURL) + "?" + urlParam + "=" + proposalNumber;
             PaymentConstant.PROPERTIES.getClass();
             ((ObjectNode)node).put("value", returnURL);
           } 
           payParamList.add(node);
         } 
         paymentReqParams.put("paramterList", (JsonNode)payParamList);
       } 
       log.info("Updated paymentReqParams : " + paymentReqParams);
     } catch (Exception ex) {
       log.info("Update Return URL Exception : ", ex);
     } 
     return paymentReqParams;
   }
   
   public static Map<String, String> getQueryMap(String query) {
     String[] params = query.split("&");
     Map<String, String> createPolicyParam = new HashMap<>();
     byte b;
     int i;
     String[] arrayOfString1;
     for (i = (arrayOfString1 = params).length, b = 0; b < i; ) {
       String param = arrayOfString1[b];
       String name = param.split("=")[0];
       String value = param.split("=")[1];
       createPolicyParam.put(name, value);
       b = (byte)(b + 1);
     } 
     return createPolicyParam;
   }
   
   private static String createPaymentResponseRecord(ObjectNode paymentRecord, JsonNode carrierNode, JsonNode masterParam, MultivaluedMap<String, String> formParams) {
     log.info("create Payment Response Records :");
     log.info("Payment :" + paymentRecord);
     log.info("carrierNode :" + carrierNode);
     log.info("formParams :" + formParams);
     ObjectNode clientResponse = objectMapper.createObjectNode();
     Set<Map.Entry<String, List<String>>> entrySet = formParams.entrySet();
     Iterator<Map.Entry<String, List<String>>> itr = entrySet.iterator();
     JsonNode jsonNode = null;
     while (itr.hasNext()) {
       Map.Entry<String, List<String>> entry = itr.next();
       clientResponse.put(entry.getKey(), ((List<String>)entry.getValue()).get(0));
     } 
     PaymentConstant.PROPERTIES.getClass();
     PaymentConstant.PROPERTIES.getClass();
     paymentRecord.put("carrier", carrierNode.get("carrier").asText());
     paymentRecord.put("lob", carrierNode.get("lob").asText());
     PaymentConstant.PROPERTIES.getClass();
     paymentRecord.put("documentType", "PaymentResponseDetails");
     paymentRecord.put("clientResponse", (JsonNode)clientResponse);
     for (JsonNode objNode : masterParam) {
       PaymentConstant.PROPERTIES.getClass();
       String param = objNode.get("name").asText().trim();
       PaymentConstant.PROPERTIES.getClass();
       String dataType = objNode.get("datatype").asText().trim();
       log.info("Master Param Name :" + param);
       log.info("Master dataType Name :" + dataType);
       if (!paymentRecord.has(param)) {
         PaymentConstant.PROPERTIES.getClass();
         if (param.equalsIgnoreCase("TransDate")) {
           paymentRecord.put(param, sdf.format(new Date()));
           continue;
         } 
         PaymentConstant.PROPERTIES.getClass();
         if (dataType.equalsIgnoreCase("String")) {
           paymentRecord.put(param, "");
           continue;
         } 
         PaymentConstant.PROPERTIES.getClass();
         if (dataType.equalsIgnoreCase("Number")) {
           paymentRecord.put(param, 0);
           continue;
         } 
         paymentRecord.put(param, jsonNode);
       } 
     } 
     log.info("Payment response record is saving into database :" + paymentRecord);
     PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
     String documentId = paymentDataAccessor.createDatabaseRecord(paymentRecord, "response");
     return documentId;
   }
   
   public static MultivaluedMap<String, String> updateMapParams(MultivaluedMap<String, String> formParams, JsonNode carrierNode) {
     String renderParamKey = carrierNode.get("concatParamKey").asText();
     String renderconcatKeyFormat = carrierNode.get("concatFormat").asText();
     String delimiter = carrierNode.get("delimiter").asText();
     String concatValue = (String)formParams.getFirst(renderParamKey);
     log.info("Input Params to update Map Params Method");
     log.info("renderParamKey \t\t\t: " + renderParamKey);
     log.info("renderconcatKeyFormat \t: " + renderconcatKeyFormat);
     log.info("delimiter \t\t\t\t: " + delimiter);
     log.info("concatValue \t\t\t\t: " + concatValue);
     formParams.remove(renderParamKey);
     String[] paramKeys = renderconcatKeyFormat.split(delimiter);
     String[] paramValues = concatValue.split(delimiter);
     log.info("paramKeys lenght :" + paramKeys.length);
     log.info("paramValues length : " + paramValues.length);
     for (int count = 0; count < paramKeys.length; count++)
       formParams.putSingle(paramKeys[count], paramValues[count]); 
     log.info("Prepare MultiValued Map : " + formParams);
     return formParams;
   }
   
   public static void isEncryptionEnabled(JsonNode carrierNode, MultivaluedMap<String, String> formParams) {
     if (!carrierNode.has("encryptionType"))
       return; 
     String encryptionType = carrierNode.get("encryptionType").asText();
     if (encryptionType.equalsIgnoreCase("tataaig")) {
       String encryptionFormParamKey = carrierNode.get("encryptionFormParamKey").asText();
       String encryptedValue = ((List<String>)formParams.remove(encryptionFormParamKey)).get(0);
       TataAIGEncryptionUtilJava tataAIGEncryptionUrilJava = new TataAIGEncryptionUtilJava();
       try {
         String response = tataAIGEncryptionUrilJava.decrypt(encryptedValue);
         jsonToMultiValueMap(formParams, response);
       } catch (JsonProcessingException e) {
         e.printStackTrace();
       } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
       } catch (IOException e) {
         e.printStackTrace();
       } 
     } 
   }
   
   public static void jsonToMultiValueMap(MultivaluedMap<String, String> formParams, String jsonString) throws JsonProcessingException, IOException {
     JsonNode responseJSON = objectMapper.readTree(jsonString);
     Iterator<Map.Entry<String, JsonNode>> fields = responseJSON.fields();
     while (fields.hasNext()) {
       Map.Entry<String, JsonNode> field = fields.next();
       List<String> value = new ArrayList<>();
       value.add(((JsonNode)field.getValue()).asText());
       formParams.put(field.getKey(), value);
     } 
   }
   
   public static String getProposalId(MultivaluedMap<String, String> formParams, JsonNode paymentResponseParamConfig, String carrier, String lob, UriInfo info) {
     String paymentReferenceId = null;
     String paramKey = null;
     String proposalId = null;
     try {
       log.info("getProposalId-formParams : " + formParams);
       log.info("getProposalId-paymentResponseParamConfig new : " + paymentResponseParamConfig);
       if (paymentResponseParamConfig.get("carriers").get(carrier).has("decryptionKey"))
       {
         formParams = CheckSumUtil.futureGenResChecksum(formParams, paymentResponseParamConfig.get("carriers").get(carrier).get("decryptionKey").asText());
       }
       PaymentConstant.PROPERTIES.getClass();
       if (paymentResponseParamConfig.get("carriers").get(carrier).has("breakInCar")) {
         PaymentConstant.PROPERTIES.getClass();
         if (formParams.containsKey(paymentResponseParamConfig.get("carriers").get(carrier).get("breakInCar").asText())) {
           PaymentConstant.PROPERTIES.getClass();
           paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get("breakInCar").asText();
         } else {
           PaymentConstant.PROPERTIES.getClass();
           if (paymentResponseParamConfig.get("carriers").get(carrier).has("renewalPlan")) {
             PaymentConstant.PROPERTIES.getClass();
             if (formParams.containsKey(paymentResponseParamConfig.get("carriers").get(carrier).get("renewalPlan").asText())) {
               log.info("renewal plan");
               PaymentConstant.PROPERTIES.getClass();
               paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get("renewalPlan").asText();
             } else {
               PaymentConstant.PROPERTIES.getClass();
               paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
             } 
           } else {
             PaymentConstant.PROPERTIES.getClass();
             paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
           } 
         } 
       } else {
         PaymentConstant.PROPERTIES.getClass();
         paramKey = paymentResponseParamConfig.get("carriers").get(carrier).get(lob).asText();
         log.info("Payment Response Client paramKey:" + paramKey);
       } 
       if (info != null) {
         MultivaluedMap<String, String> formGetParams = info.getQueryParameters();
         PaymentConstant.PROPERTIES.getClass();
         PaymentConstant.PROPERTIES.getClass();
         String methodType = paymentResponseParamConfig.get("carriers").get(carrier).get("methodType").asText();
         if (formGetParams != null && formGetParams.size() != 0 && methodType.equalsIgnoreCase("GET")) {
           log.info("Payment Response Client Reponse Get Params : " + formGetParams);
           paymentReferenceId = (String)formGetParams.getFirst(paramKey);
         } else {
           log.info("Payment Response Client Reponse Get Params info is null");
           paymentReferenceId = (String)formParams.getFirst(paramKey);
           log.info("Payment Response Client Reponse=>" + formParams + "<=Get Params paymentReferenceId:" + paymentReferenceId);
         } 
       } else {
         log.info("Payment Response Client Reponse Get Params info is null in else.");
         paymentReferenceId = (String)formParams.getFirst(paramKey);
       } 
       log.info("getProposalId-paramKey : " + paramKey);
       log.info("getProposalId-paymentReferenceId : " + paymentReferenceId);
       proposalId = fetchProposalId(lob, paymentReferenceId);
     } catch (Exception ex) {
       log.info("Exception : getting proposal ID : ", ex);
     } 
     return proposalId;
   }
   
   public static String validatePaymentResponseDoc(MultivaluedMap<String, String> formParams, JsonNode carrierNode, String returnURL) {
     String key = null;
     String value = null;
     log.info("validatePaymentResponseDoc formParams :" + formParams);
     log.info("validatePaymentResponseDoc carrierNode :" + carrierNode);
     log.info("validatePaymentResponseDoc returnURL :" + returnURL);
     ObjectNode paymentRecord = objectMapper.createObjectNode();
     PaymentConstant.PROPERTIES.getClass();
     PaymentConstant.PROPERTIES.getClass();
     JsonNode paymentStatusArray = carrierNode.get("params").get("paymentStatus");
     for (JsonNode paymentStatus : paymentStatusArray) {
       PaymentConstant.PROPERTIES.getClass();
       String transStatusField = (String)formParams.getFirst(paymentStatus.get("transStatusField").textValue());
       log.info("transStatusField :" + transStatusField);
       log.info("paymentStatus :" + paymentStatus);
       PaymentConstant.PROPERTIES.getClass();
       log.info("paymentStatus get success value :" + paymentStatus.get("successValue"));
       PaymentConstant.PROPERTIES.getClass();
       if (transStatusField != null && transStatusField.equals(paymentStatus.get("successValue").textValue())) {
         log.info("transStatusField :" + transStatusField);
         PaymentConstant.PROPERTIES.getClass();
         JsonNode sucessParamNodes = carrierNode.get("params").get("success");
         PaymentConstant.PROPERTIES.getClass();
         paymentRecord.put("paymentStatus", "success");
         label17: for (JsonNode paramNode : sucessParamNodes) {
           PaymentConstant.PROPERTIES.getClass();
           key = paramNode.get("appKey").asText();
           PaymentConstant.PROPERTIES.getClass();
           value = (String)formParams.getFirst(paramNode.get("clientKey").asText());
           paymentRecord.put(key, value);
           PaymentConstant.PROPERTIES.getClass();
           if (paramNode.get("isUIRequired").asBoolean()) { if (value != null) { if (value
                 .trim().length() == 0)
                 break label17;  continue; }  returnURL = String.valueOf(returnURL) + "&statusCode=PE1020"; }
         
         } 
       } 
     } 
     
     return returnURL;
   }
 }


