 package com.idep.service.payment.util;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import java.nio.charset.StandardCharsets;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Base64;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.Mac;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 import javax.ws.rs.core.MultivaluedHashMap;
 import javax.ws.rs.core.MultivaluedMap;
 import org.apache.log4j.Logger;
 
 
 
 public class CheckSumUtil
 {
   static Logger log = Logger.getLogger(CheckSumUtil.class.getName());
   
   static ObjectMapper mapper = new ObjectMapper();
   
   public static JsonNode generatePaymentReqParam(JsonNode paymentMapperResponse, String checksumKey, String paymentGateway) {
     log.info("Inside CheckSumUtil generatePaymentReqParam : " + paymentMapperResponse);
     log.info("Inside CheckSumUtil generatePaymentReqParam checksumKey: " + checksumKey);
     if (paymentGateway.equals("billdesk")) {
       String returnUrl = paymentMapperResponse.get("returnUrl").textValue();
       String payReqStr = ((JsonNode)paymentMapperResponse.get("paramterList").iterator().next()).get("value").textValue();
       log.info("inside GeneratepaymentreqParams payReqStr:" + payReqStr);
       try {
         String paymentReqParams = String.valueOf(payReqStr) + "|" + returnUrl;
         String checksumValue = checkSumSHA256(String.valueOf(paymentReqParams) + "|" + checksumKey);
         String paymentRequestParams = String.valueOf(paymentReqParams) + "|" + checksumValue;
         ArrayNode payParamList = mapper.createArrayNode();
         for (JsonNode node : paymentMapperResponse.get("paramterList")) {
           String paramName = node.get("name").asText();
           if (paramName.equalsIgnoreCase("msg"))
             ((ObjectNode)node).put("value", paymentRequestParams); 
           payParamList.add(node);
         } 
         ((ObjectNode)paymentMapperResponse).put("paramterList", (JsonNode)payParamList);
       } catch (Exception e) {
         log.error("Checksum processing exception at generatePaymentReqParam : ", e);
       } 
     } 
     return paymentMapperResponse;
   }
   
   public static JsonNode generatePaymentPipeReqParam(JsonNode paymentMapperResponse, String checksumKey, String paymentGateway) {
     log.info("Inside CheckSumUtil generatePaymentPipeReqParam : " + paymentMapperResponse);
     if (paymentGateway.equals("billdesk")) {
       String returnUrl = paymentMapperResponse.get("returnUrl").textValue();
       String payReqStr = ((JsonNode)paymentMapperResponse.get("paramterList").iterator().next()).get("value").textValue();
       try {
         String paymentReqParams = String.valueOf(payReqStr) + "|" + returnUrl;
         ArrayNode payParamList = mapper.createArrayNode();
         for (JsonNode node : paymentMapperResponse.get("paramterList")) {
           String paramName = node.get("name").asText();
           if (paramName.equalsIgnoreCase("request"))
             ((ObjectNode)node).put("value", paymentReqParams); 
           payParamList.add(node);
         } 
         ((ObjectNode)paymentMapperResponse).put("paramterList", (JsonNode)payParamList);
       } catch (Exception e) {
         log.error("Checksum processing exception at generatePaymentReqParam : ", e);
       } 
     } 
     return paymentMapperResponse;
   }
   
   public static JsonNode generateCheckSum(JsonNode paymentMapperResponse) {
     log.info("Inside CheckSumUtil generateCheckSum : " + paymentMapperResponse);
     try {
       ArrayNode payParamList = mapper.createArrayNode();
       for (JsonNode node : paymentMapperResponse.get("paramterList")) {
         String paramName = node.get("name").asText();
         if (paramName.equalsIgnoreCase("checkSum")) {
           log.info("checkSum :" + node.get("value").textValue());
           String checkSum = node.get("value").textValue();
           checkSum = checkSumSHA256(checkSum);
           ((ObjectNode)node).put("value", checkSum);
         } 
         payParamList.add(node);
       } 
       ((ObjectNode)paymentMapperResponse).put("paramterList", (JsonNode)payParamList);
     } catch (Exception e) {
       log.error("Checksum processing exception at generatePaymentReqParam : ", e);
     } 
     return paymentMapperResponse;
   }
   
   public static JsonNode generateTataAigCheckSum(JsonNode paymentMapperResponse) {
     ObjectNode objectNode = mapper.createObjectNode();
     ArrayNode payParamList = mapper.createArrayNode();
     for (JsonNode node : paymentMapperResponse.get("paramterList"))
       objectNode.put(node.get("name").asText(), node.get("value").asText()); 
     TataAIGEncryptionUtilJava tataaigEncrypt = new TataAIGEncryptionUtilJava();
     String rs = tataaigEncrypt.encrypt(objectNode.toString());
     objectNode = mapper.createObjectNode();
     objectNode.put("ngModel", "pgiRequest");
     objectNode.put("name", "pgiRequest");
     objectNode.put("value", rs);
     payParamList.add((JsonNode)objectNode);
     ((ObjectNode)paymentMapperResponse).put("paramterList", (JsonNode)payParamList);
     return paymentMapperResponse;
   }
   
   public static JsonNode generateSHA512CheckSum(JsonNode paymentMapperResponse) {
     log.debug("Inside CheckSumUtil generateSHA512CheckSum : " + paymentMapperResponse);
     try {
       ArrayNode payParamList = mapper.createArrayNode();
       for (JsonNode node : paymentMapperResponse.get("paramterList")) {
         String paramName = node.get("name").asText();
         if (paramName.equalsIgnoreCase("hash")) {
           String hash = node.get("value").textValue();
           hash = checkSumSHA512(hash);
           ((ObjectNode)node).put("value", hash.toLowerCase());
         } 
         payParamList.add(node);
       } 
       ((ObjectNode)paymentMapperResponse).put("paramterList", (JsonNode)payParamList);
     } catch (Exception e) {
       log.error("Checksum processing exception at generatePaymentReqParam : ", e);
     } 
     log.debug("paymentMapperResponse value in generateSHA512CheckSum: " + paymentMapperResponse);
     return paymentMapperResponse;
   }
   
   public static JsonNode generateHmacSHA256PaymentReqParam(JsonNode paymentMapperResponse, String checksumKey, String paymentGateway) {
     log.info("Inside CheckSumUtil generateHmacSHA256PaymentReqParam : " + paymentMapperResponse);
     if (paymentGateway.equals("billdesk")) {
       String returnUrl = paymentMapperResponse.get("returnUrl").textValue();
       String payReqStr = ((JsonNode)paymentMapperResponse.get("paramterList").iterator().next()).get("value").textValue();
       try {
         String paymentReqParams = String.valueOf(payReqStr) + "|" + returnUrl;
         String checksumValue = HmacSHA256(paymentReqParams, checksumKey);
         String paymentRequestParams = String.valueOf(paymentReqParams) + "|" + checksumValue;
         ArrayNode payParamList = mapper.createArrayNode();
         for (JsonNode node : paymentMapperResponse.get("paramterList")) {
           String paramName = node.get("name").asText();
           if (paramName.equalsIgnoreCase("msg"))
             ((ObjectNode)node).put("value", paymentRequestParams); 
           payParamList.add(node);
         } 
         ((ObjectNode)paymentMapperResponse).put("paramterList", (JsonNode)payParamList);
       } catch (Exception e) {
         log.error("Checksum processing exception at generateHmacSHA256PaymentReqParam : ", e);
       } 
     } 
     return paymentMapperResponse;
   }
   
   public static String HmacSHA256(String message, String secret) {
     MessageDigest md = null;
     try {
       Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
       SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
       sha256_HMAC.init(secret_key);
       byte[] raw = sha256_HMAC.doFinal(message.getBytes());
       StringBuffer ls_sb = new StringBuffer();
       for (int i = 0; i < raw.length; i++)
         ls_sb.append(char2hex(raw[i])); 
       return ls_sb.toString();
     } catch (Exception e) {
       e.printStackTrace();
       return null;
     } 
   }
   
   public static String checkSumSHA512(String plaintext) {
     MessageDigest md = null;
     try {
       md = MessageDigest.getInstance("SHA-512");
       md.update(plaintext.getBytes("UTF-8"));
     } catch (Exception e) {
       md = null;
     } 
     StringBuffer ls_sb = new StringBuffer();
     byte[] raw = md.digest();
     for (int i = 0; i < raw.length; i++)
       ls_sb.append(char2hex(raw[i])); 
     return ls_sb.toString();
   }
   
   public static String checkSumSHA256(String plaintext) {
     MessageDigest md = null;
     try {
       md = MessageDigest.getInstance("SHA-256");
       md.update(plaintext.getBytes("UTF-8"));
     } catch (Exception e) {
       md = null;
     } 
     StringBuffer ls_sb = new StringBuffer();
     byte[] raw = md.digest();
     for (int i = 0; i < raw.length; i++)
       ls_sb.append(char2hex(raw[i])); 
     return ls_sb.toString();
   }
   
   public static String char2hex(byte x) {
     char[] arr = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 
 
 
 
     
     char[] c = { arr[(x & 0xF0) >> 4], arr[x & 0xF] };
     return new String(c);
   }
 
 
 
 
   
   public static MultivaluedMap<String, String> futureGenResChecksum(MultivaluedMap<String, String> formParams, String decrKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
     byte[] IV = { 18, 50, 80, 125, -116, -86, -51, -26 };
     if (formParams.getFirst("ResponseData") != null) {
       String newString = ((String)formParams.getFirst("ResponseData")).replace("[", "").replace("]", "").replace("$", "+");
       byte[] inputByteArray = new byte[newString.length()];
       byte[] byKey = decrKey.substring(0, 8).getBytes(StandardCharsets.UTF_8);
       SecretKeySpec secretKeySpec = new SecretKeySpec(byKey, "DES");
       Cipher des = Cipher.getInstance("DES/CBC/PKCS5Padding");
       des.init(2, secretKeySpec, new IvParameterSpec(IV));
       inputByteArray = Base64.getDecoder().decode(newString);
       byte[] decryptedBytes = des.doFinal(inputByteArray);
       String outputString = new String(decryptedBytes, StandardCharsets.UTF_8);
       MultivaluedHashMap multivaluedHashMap = new MultivaluedHashMap();
       String[] keyValuePairs = outputString.split("&");
       for (String pair : keyValuePairs) {
         
         String[] entry = pair.split("=");
         multivaluedHashMap.add(entry[0].trim(), entry[1].trim());
       } 
       return (MultivaluedMap<String, String>)multivaluedHashMap;
     } 
     return formParams;
   }
 
   
   public static void main(String[] args) throws Exception {
     String str = "{\"responseCode\":1000,\"message\":\"success\",\"data\":{\"policyNo\":\"PTF/180105100237\",\"proposalId\":\"PROP000C4411\"}}";
     ObjectMapper objectMapper = new ObjectMapper();
     JsonNode paymentGatewayResp = objectMapper.readTree(str);
     System.out.println(paymentGatewayResp.get("responseCode"));
   }
 }


