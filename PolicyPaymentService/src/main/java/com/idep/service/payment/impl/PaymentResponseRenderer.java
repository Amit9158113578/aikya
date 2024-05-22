 package com.idep.service.payment.impl;
 
 import com.couchbase.client.java.document.JsonDocument;
 import com.couchbase.client.java.document.json.JsonObject;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.idep.service.payment.exception.ExecutionTerminator;
 import com.idep.service.payment.util.CommonLib;
 import com.idep.service.payment.util.PaymentConstant;
 import java.net.URI;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import org.apache.log4j.Logger;
 
 
 
 
 
 
 
 
 
 
 
 public class PaymentResponseRenderer
 {
   Logger log = Logger.getLogger(PaymentResponseRenderer.class.getName());
   ObjectMapper objectMapper = new ObjectMapper();
   PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
   JsonNode paymentConstantNode = null;
   JsonNode masterPaymentReponseConfig = null;
   JsonNode paymentConfigNode = null;
   JsonNode paymentResponseParamConfig = null;
   JsonNode paymentRequestParamConfig = null;
   String defaultSuccessURL = null;
   String defaultFailureURL = null;
   JsonNode masterPaymentRedirectionResponse = null;
   JsonNode PaymentRedirectionResponseBySource = null;
 
 
 
 
 
 
 
   
   public String paymentResponseSuccessPOST(MultivaluedMap<String, String> formParams, String carrier, String lob, UriInfo info) throws ExecutionTerminator {
     this.log.info("Payment Response Processing Type : paymentResponseSuccessPOST");
     this.log.info("Payment Response Carrier : " + carrier + " Line of Business: " + lob);
     this.log.info("Payment Response Client Reponse Params : " + formParams);
     String source = null;
     ObjectNode paymentGatewayResponse = this.objectMapper.createObjectNode();
     initPaymentConfig();
     String successURL = this.defaultFailureURL;
     
     try {
       if (carrier != null && lob != null && formParams != null && formParams.size() > 0) {
         
         String documentId = carrier.toLowerCase().trim() + lob.toLowerCase().trim();
         this.log.info("documentId (carrier + lob):" + documentId);
         this.log.info("formParams.containsKey hdnmsg" + formParams.containsKey("hdnmsg"));
         this.log.info("lob :" + lob.equalsIgnoreCase("bike"));
         this.log.info("carrier :" + carrier.equalsIgnoreCase("hdfc"));
 
 
 
         
         this.log.info("Final documentId :" + documentId);
         JsonNode carrierNode = this.masterPaymentReponseConfig.get(documentId);
         String renderType = carrierNode.get("renderType").asText();
         CommonLib.isEncryptionEnabled(carrierNode, formParams);
         
         this.log.info("Payment Response Client Reponse Params : " + renderType);
         this.masterPaymentRedirectionResponse = this.paymentDataAccessor.fetchDBDocument("MasterPaymentRedirection", "serverConfig");
         this.PaymentRedirectionResponseBySource = this.masterPaymentRedirectionResponse.get(lob);
         
         if (renderType.equalsIgnoreCase("list")) {
           
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           
           successURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } else if (renderType.equalsIgnoreCase("concat")) {
 
 
 
 
 
 
           
           formParams = CommonLib.updateMapParams(formParams, carrierNode);
 
           
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           this.log.info("PaymentRedirectionResponseBySource :" + this.PaymentRedirectionResponseBySource + " source :" + source);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           successURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } 
       } else {
         String policyNo = getPaymentRequestParamList(formParams);
         if (policyNo != null && policyNo.trim().length() > 0) {
           successURL = this.defaultFailureURL + "?" + this.paymentConstantNode.get("POLICY_NO").textValue() + "=" + policyNo;
         } else {
           
           successURL = this.paymentConfigNode.findValue(this.paymentConstantNode.get("PAYMENT_FAILURE_URL").textValue()).textValue();
         } 
       } 
       paymentGatewayResponse = CommonLib.createPolicyParamConfig(formParams, this.paymentResponseParamConfig, carrier, lob, info);
       successURL = successURL + "&source=" + source;
       
       if (carrier != null && lob != null && formParams != null && formParams.size() > 0) {
         String documentId = carrier.toLowerCase().trim() + lob.toLowerCase().trim();
         JsonNode carrierNode = this.masterPaymentReponseConfig.get(documentId);
         successURL = CommonLib.validatePaymentResponseDoc(formParams, carrierNode, successURL);
       } 
       
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("successURL", successURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("failureURL", successURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("policyStatus", "success");
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("lineOfBusiness", lob);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("carrier", carrier);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("paymentGatewayResponse", (JsonNode)this.objectMapper.convertValue(formParams, JsonNode.class));
     }
     catch (Exception ex) {
       this.log.info("Exception : Payment Gateway POST success response error : ", ex);
       throw new ExecutionTerminator();
     } 
     return paymentGatewayResponse.toString();
   }
 
 
 
 
 
 
 
   
   public String paymentResponseSuccessGET(UriInfo info, String carrier, String lob) throws ExecutionTerminator {
     this.log.info("Payment Response Processing Type : paymentResponseSuccessGET");
     this.log.info("Payment Response Carrier : " + carrier + " Line of Business: " + lob);
     MultivaluedMap<String, String> formParams = info.getQueryParameters();
     this.log.info("Payment Response Client Reponse Params : " + formParams);
     this.log.info("Payment Response Client info : " + info.toString());
     String source = null;
     
     ObjectNode paymentGatewayResponse = this.objectMapper.createObjectNode();
     initPaymentConfig();
 
     
     String successURL = this.defaultFailureURL;
     this.log.info("successURL in paymentResponseSuccessGET  : " + successURL);
     
     try {
       if (carrier != null && lob != null && formParams != null && formParams.size() > 0) {
         String documentId = carrier.toLowerCase().trim() + lob.toLowerCase().trim();
         JsonNode carrierNode = this.masterPaymentReponseConfig.get(documentId);
         String renderType = carrierNode.get("renderType").asText();
         
         this.log.info("Payment Response Client Reponse Params : " + renderType);
         
         this.masterPaymentRedirectionResponse = this.paymentDataAccessor.fetchDBDocument("MasterPaymentRedirection", "serverConfig");
         this.PaymentRedirectionResponseBySource = this.masterPaymentRedirectionResponse.get(lob);
 
         
         if (renderType.equalsIgnoreCase("list")) {
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           successURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } else if (renderType.equalsIgnoreCase("concat")) {
           formParams = CommonLib.updateMapParams(formParams, carrierNode);
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           successURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } 
       } else {
         source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
         String policyNo = getPaymentRequestParamList(formParams);
         if (policyNo != null && policyNo.trim().length() > 0) {
           successURL = this.defaultSuccessURL + "?" + this.paymentConstantNode.get("POLICY_NO").textValue() + "=" + policyNo;
         } else {
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           successURL = this.paymentConfigNode.findValue(this.paymentConstantNode.get("PAYMENT_FAILURE_URL").textValue()).textValue();
         } 
       } 
       
       paymentGatewayResponse = CommonLib.createPolicyParamConfig(formParams, this.paymentResponseParamConfig, carrier, lob, info);
       
       successURL = successURL + "&source=" + source;
       if (carrier != null && lob != null && formParams != null && formParams.size() > 0) {
         String documentId = carrier.toLowerCase().trim() + lob.toLowerCase().trim();
         JsonNode carrierNode = this.masterPaymentReponseConfig.get(documentId);
         successURL = CommonLib.validatePaymentResponseDoc(formParams, carrierNode, successURL);
       } 
       this.log.info("final returnURL  :" + successURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("successURL", successURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("failureURL", successURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("policyStatus", "success");
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("lineOfBusiness", lob);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("carrier", carrier);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("paymentGatewayResponse", (JsonNode)this.objectMapper.convertValue(formParams, JsonNode.class));
     }
     catch (Exception ex) {
       this.log.info("Exception : Payment Gateway GET success response error : ", ex);
       throw new ExecutionTerminator();
     } 
     return paymentGatewayResponse.toString();
   }
 
 
 
 
 
 
 
   
   public String paymentResponseFailurePOST(MultivaluedMap<String, String> formParams, String carrier, String lob, UriInfo info) throws ExecutionTerminator {
     this.log.info("Payment Response Processing Type : paymentResponseFailurePOST");
     this.log.info("Payment Response Carrier : " + carrier + " Line of Business: " + lob);
     this.log.info("Payment Response Client Reponse Params : " + formParams);
     
     ObjectNode paymentGatewayResponse = this.objectMapper.createObjectNode();
     initPaymentConfig();
     String source = null;
     String failureURL = this.defaultFailureURL;
     try {
       if (carrier != null && lob != null && formParams != null && formParams.size() > 0) {
         String documentId = carrier.toLowerCase().trim() + lob.toLowerCase().trim();
         JsonNode carrierNode = this.masterPaymentReponseConfig.get(documentId);
         String renderType = carrierNode.get("renderType").asText();
         
         this.log.info("Payment Response Client Reponse Params : " + renderType);
         
         this.masterPaymentRedirectionResponse = this.paymentDataAccessor.fetchDBDocument("MasterPaymentRedirection", "serverConfig");
         this.PaymentRedirectionResponseBySource = this.masterPaymentRedirectionResponse.get(lob);
         
         if (renderType.equalsIgnoreCase("list")) {
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           failureURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } else if (renderType.equalsIgnoreCase("concat")) {
           formParams = CommonLib.updateMapParams(formParams, carrierNode);
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           failureURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } 
       } 
       
       paymentGatewayResponse = CommonLib.createPolicyParamConfig(formParams, this.paymentResponseParamConfig, carrier, lob, info);
       
       failureURL = failureURL + "&source=" + source;
       
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("successURL", failureURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("failureURL", failureURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("policyStatus", "failure");
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("lineOfBusiness", lob);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("carrier", carrier);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("paymentGatewayResponse", (JsonNode)this.objectMapper.convertValue(formParams, JsonNode.class));
     }
     catch (Exception ex) {
       this.log.info("Exception : Payment Gateway POST failure response error : ", ex);
       throw new ExecutionTerminator();
     } 
     return paymentGatewayResponse.toString();
   }
 
 
 
 
 
 
 
   
   public String paymentResponseFailureGET(UriInfo info, String carrier, String lob) throws ExecutionTerminator {
     MultivaluedMap<String, String> formParams = info.getQueryParameters();
     this.log.info("Payment Response Processing Type : paymentResponseFailureGET");
     this.log.info("Payment Response Carrier : " + carrier + " Line of Business: " + lob);
     this.log.info("Payment Response Client Reponse Params : " + formParams);
     String source = null;
     ObjectNode paymentGatewayResponse = this.objectMapper.createObjectNode();
     initPaymentConfig();
     
     String failureURL = this.defaultFailureURL;
     
     try {
       if (carrier != null && lob != null && formParams != null && formParams.size() > 0) {
         String documentId = carrier.toLowerCase().trim() + lob.toLowerCase().trim();
         JsonNode carrierNode = this.masterPaymentReponseConfig.get(documentId);
         String renderType = carrierNode.get("renderType").asText();
         
         this.log.info("Payment Response Client Reponse Params : " + renderType);
         
         this.masterPaymentRedirectionResponse = this.paymentDataAccessor.fetchDBDocument("MasterPaymentRedirection", "serverConfig");
         this.PaymentRedirectionResponseBySource = this.masterPaymentRedirectionResponse.get(lob);
         
         if (renderType.equalsIgnoreCase("list")) {
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           failureURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } else if (renderType.equalsIgnoreCase("concat")) {
           formParams = CommonLib.updateMapParams(formParams, carrierNode);
           source = initDefaultPaymentURL(formParams, this.paymentResponseParamConfig, carrier, lob, info);
           JsonNode sourceURLNode = this.PaymentRedirectionResponseBySource.get(source);
           this.log.info("sourceURLNode" + sourceURLNode);
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("successurl", sourceURLNode.get("successurl").asText());
           PaymentConstant.PROPERTIES.getClass(); ((ObjectNode)carrierNode).put("failureurl", sourceURLNode.get("failureurl").asText());
           this.log.info("updated carrierNode :" + carrierNode);
           failureURL = CommonLib.formSuccessURLByList(formParams, carrierNode, this.masterPaymentReponseConfig, this.defaultFailureURL);
         } 
       } 
 
       
       paymentGatewayResponse = CommonLib.createPolicyParamConfig(formParams, this.paymentResponseParamConfig, carrier, lob, info);
       
       failureURL = failureURL + "&source=" + source;
       
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("successURL", failureURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("failureURL", failureURL);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("policyStatus", "failure");
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("lineOfBusiness", lob);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("carrier", carrier);
       PaymentConstant.PROPERTIES.getClass(); paymentGatewayResponse.put("paymentGatewayResponse", (JsonNode)this.objectMapper.convertValue(formParams, JsonNode.class));
     }
     catch (Exception ex) {
       this.log.info("Exception : Payment Gateway GET failure response error : ", ex);
       throw new ExecutionTerminator();
     } 
     return paymentGatewayResponse.toString();
   }
 
 
 
 
 
 
 
   
   public String getPaymentRequestParamList(MultivaluedMap<String, String> formParams) {
     String policyNo = null;
     if (formParams != null && formParams.size() > 0) {
       JsonNode reponseConfiguration = this.paymentDataAccessor.fetchDBDocument(this.paymentConstantNode.get("CLIENT_PAY_RES_PARAM_DOC").textValue(), "serverConfig");
       JsonNode paymentRequestParamNode = reponseConfiguration.get(this.paymentConstantNode.get("REQUEST_PARAM").textValue());
       JsonNode paymentResponseParamNode = reponseConfiguration.get(this.paymentConstantNode.get("RESPONSE_PARAM").textValue());
       ObjectNode paymentResponseParamList = this.objectMapper.createObjectNode();
       
       for (JsonNode node : reponseConfiguration.get("PAYMENT_RESPONSE_PARAM")) {
         paymentResponseParamList.put(node.textValue(), "");
       }
       if (paymentRequestParamNode.isArray()) {
         Iterator<JsonNode> itr = paymentRequestParamNode.iterator();
         while (itr.hasNext()) {
           String requestParam = ((JsonNode)itr.next()).asText();
           if (paymentResponseParamList.has(requestParam)) {
             String urlParam = (String)formParams.getFirst(requestParam);
             
             String[] urlParamArray = urlParam.toString().split("\\|", -1);
             PaymentConstant.PROPERTIES.getClass(); if (urlParam != null && urlParam.length() > 0 && urlParamArray[paymentResponseParamNode.get(requestParam).get("statusindex").asInt()].equals(paymentResponseParamNode.get(requestParam).get("status").textValue())) {
               policyNo = urlParamArray[paymentResponseParamNode.get(requestParam).get("valueindex").asInt()];
             }
             continue;
           } 
           policyNo = (String)formParams.getFirst(requestParam);
           if (policyNo != null) {
             break;
           }
         } 
       } 
     } 
     return policyNo;
   }
 
 
 
 
 
 
 
   
   public ObjectNode getPaymentRedirectionURLs(ObjectNode paymentGatewayResponse, String successURL) {
     ObjectNode redirectURLDetails = this.objectMapper.createObjectNode();
     try {
       PaymentDataAccessor paymentDataAccessor = new PaymentDataAccessor();
       PaymentConstant.PROPERTIES.getClass(); String proposalId = paymentGatewayResponse.get("proposalId").asText();
       String failureURL = this.paymentConfigNode.findValue(this.paymentConstantNode.get("PAYMENT_FAILURE_URL").textValue()).textValue();
       JsonNode proposalDocument = paymentDataAccessor.fetchDBDocument(proposalId, "policyTransaction");
 
       
       PaymentConstant.PROPERTIES.getClass();
       PaymentConstant.PROPERTIES.getClass(); String source = proposalDocument.get("source").asText();
       PaymentConstant.PROPERTIES.getClass(); if (proposalDocument != null && proposalDocument.has("source") && source.equalsIgnoreCase("wordpress")) {
         PaymentConstant.PROPERTIES.getClass(); String wordpressHostURL = this.paymentConfigNode.findValue("wordpressHostURL").textValue();
         PaymentConstant.PROPERTIES.getClass(); String websiteHostURL = this.paymentConfigNode.findValue("websiteHostURL").textValue();
         successURL = successURL.replace(websiteHostURL, wordpressHostURL);
         failureURL = failureURL.replace(websiteHostURL, wordpressHostURL);
       } 
 
 
       
       PaymentConstant.PROPERTIES.getClass(); redirectURLDetails.put("successURL", successURL);
       PaymentConstant.PROPERTIES.getClass(); redirectURLDetails.put("failureURL", failureURL);
     }
     catch (Exception ex) {
       this.log.error("Exception payment redirection urls : ", ex);
     } 
     this.log.info("Redirect URL Details : " + redirectURLDetails);
     return redirectURLDetails;
   }
 
 
 
 
 
 
 
 
   
   public Response paymentFailureResponseRedirection(String createPolicyResponse) throws ExecutionTerminator {
     URI location = null;
     try {
       JsonNode paymentGatewayResp = this.objectMapper.readTree(createPolicyResponse);
       ObjectNode paymentResponseRecord = this.objectMapper.createObjectNode();
       this.log.info("Policy creation failure response from create policy call : " + paymentGatewayResp);
       Date currentDate = new Date();
       DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
       
       PaymentConstant.PROPERTIES.getClass(); String proposalId = paymentGatewayResp.get("proposalId").textValue();
       PaymentConstant.PROPERTIES.getClass(); String redirectURL = paymentGatewayResp.get("failureURL").textValue();
       
       this.log.info("Proposal Id : " + proposalId);
       this.log.info("Redirect URL : " + redirectURL);
       
       String[] redirectURLArray = redirectURL.split("\\?");
       String apPreferIdVal = "";
       if (redirectURLArray.length > 1) {
         Map<String, String> successURLParamMap = CommonLib.getQueryMap(redirectURLArray[1]);
         PaymentConstant.PROPERTIES.getClass(); apPreferIdVal = successURLParamMap.get("apPreferId");
         this.log.info("apPreferId : " + apPreferIdVal);
       } 
       
       JsonDocument proposalDocument = this.paymentDataAccessor.fetchDocumentById("policyTransaction", proposalId);
       
       PaymentConstant.PROPERTIES.getClass(); paymentResponseRecord.put("transactionStatusCode", 0);
       PaymentConstant.PROPERTIES.getClass(); paymentResponseRecord.put("apPreferId", apPreferIdVal);
       PaymentConstant.PROPERTIES.getClass(); PaymentConstant.PROPERTIES.getClass(); paymentResponseRecord.put("proposalStatus", "paymentFailure");
       PaymentConstant.PROPERTIES.getClass(); PaymentConstant.PROPERTIES.getClass(); paymentResponseRecord.put("documentType", "paymentResponse");
       PaymentConstant.PROPERTIES.getClass(); paymentResponseRecord.put("updatedDate", dateFormat.format(currentDate));
       PaymentConstant.PROPERTIES.getClass(); paymentResponseRecord.put("proposalId", proposalId);
       
       this.log.info("Payment response record to be stored in DB : " + paymentResponseRecord);
       
       JsonObject proposalDocumentFinal = ((JsonObject)proposalDocument.content()).put("paymentResponse", JsonObject.fromJson(this.objectMapper.writeValueAsString(paymentResponseRecord)));
       this.paymentDataAccessor.replaceDocument("policyTransaction", proposalId, proposalDocumentFinal);
       
       location = new URI(redirectURL);
     } catch (Exception ex) {
       this.log.info("Exception : Payment Response Redirection Failed : ", ex);
       throw new ExecutionTerminator();
     } 
     return Response.seeOther(location).build();
   }
 
 
 
 
 
 
 
   
   public Response paymentResponseRedirection(String redirectURL) throws ExecutionTerminator {
     URI location = null;
     try {
       this.log.info("Payment reponse redirection URL : " + redirectURL);
       location = new URI(redirectURL);
     } catch (Exception ex) {
       this.log.info("Exception : Payment Response Redirection Failed : ", ex);
       throw new ExecutionTerminator();
     } 
     return Response.seeOther(location).build();
   }
 
 
 
   
   public void initPaymentConfig() {
     try {
       if (this.paymentConstantNode == null) {
         this.paymentConstantNode = this.paymentDataAccessor.fetchDBDocument("PaymentConstantConfig", "serverConfig");
       }
       
       if (this.masterPaymentReponseConfig == null) {
         this.masterPaymentReponseConfig = this.paymentDataAccessor.fetchDBDocument("MasterPaymentConfigDetails", "serverConfig");
       }
       
       if (this.paymentConfigNode == null) {
         this.paymentConfigNode = this.paymentDataAccessor.fetchDBDocument(this.paymentConstantNode.get("REDIRECT_URL_CONFIG_DOC").textValue(), "serverConfig");
       }
       
       if (this.paymentResponseParamConfig == null) {
         this.paymentResponseParamConfig = this.paymentDataAccessor.fetchDBDocument("PaymentResponseParamConfig", "serverConfig");
       }
       
       if (this.paymentRequestParamConfig == null) {
         this.paymentRequestParamConfig = this.paymentDataAccessor.fetchDBDocument("PaymentRequestParamConfig", "serverConfig");
       
       }
     
     }
     catch (Exception e) {
       this.log.info("Payment Init Config - Exception : ", e);
     } 
   }
   public String initDefaultPaymentURL(MultivaluedMap<String, String> formParams, JsonNode paymentResponseParamConfig, String carrier, String lob, UriInfo info) {
     String source = null;
     try {
       this.log.info("paymentConstantNode :" + this.paymentConstantNode);
       
       if (this.paymentConstantNode != null && this.paymentConfigNode != null) {
         String proposalId = CommonLib.getProposalId(formParams, paymentResponseParamConfig, carrier, lob, info);
         this.log.info("proposalId :" + proposalId);
         if (proposalId != null && proposalId != "") {
           JsonNode proposalDoc = this.paymentDataAccessor.fetchDBDocument(proposalId, "policyTransaction");
           source = proposalDoc.get("source").asText();
           this.log.info("validate source :" + source);
           if (this.paymentConfigNode.get(source) != null)
           {
             this.defaultSuccessURL = this.paymentConfigNode.get(source).findValue(this.paymentConstantNode.get("PAYMENT_SUCCESS_URL").textValue()).textValue();
             this.defaultFailureURL = this.paymentConfigNode.get(source).findValue(this.paymentConstantNode.get("PAYMENT_FAILURE_URL").textValue()).textValue();
           }
         
         }
         else if (carrier.equalsIgnoreCase("star")) {
           this.log.info("in else condition !!!");
           source = "wordpress";
           this.defaultSuccessURL = this.paymentConfigNode.get(source).findValue(this.paymentConstantNode.get("PAYMENT_SUCCESS_URL").textValue()).textValue();
           this.defaultFailureURL = this.paymentConfigNode.get(source).findValue(this.paymentConstantNode.get("PAYMENT_FAILURE_URL").textValue()).textValue();
         } 
       } 
     } catch (Exception e) {
       this.log.info("Default URL fetch - Exception : ", e);
     } 
     return source;
   }
 }


