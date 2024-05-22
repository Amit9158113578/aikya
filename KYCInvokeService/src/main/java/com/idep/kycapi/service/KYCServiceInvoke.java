package com.idep.kycapi.service;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.kycapi.util.CreateJWTToken;
import com.idep.kycapi.util.EncrypDecryptOperation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class KYCServiceInvoke implements Processor {
  Logger log = Logger.getLogger(KYCServiceInvoke.class.getName());
  
  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
  
  static ObjectMapper mapper = new ObjectMapper();
  
  public void process(Exchange exchange) {
    Object request = null;
    String url = null;
    JsonNode carrierRequestConfiguration = null;
    ObjectNode finalResultNode = mapper.createObjectNode();
    try {
      String reqBody = (String)exchange.getIn().getBody();
      JsonNode requestBody = mapper.readTree(reqBody);
      this.log.info("requestBody :" + requestBody);
      if (requestBody.has("documentId")) {
        JsonNode defaultLeadProfileDoc = mapper.readTree(((JsonObject)this.serverConfig
            .getDocBYId(requestBody.get("documentId").asText()).content())
            .toString());
        finalResultNode.put("responseCode", "P365RES100");
        finalResultNode.put("message", "response");
        finalResultNode.put("data", defaultLeadProfileDoc);
        exchange.getIn().setBody(finalResultNode);
      } else if (requestBody.has("documetType") && requestBody.has("state")) {
        String query = "select cities from ServerConfig where documentType='" + requestBody.get("documetType").asText() + "' and state='" + requestBody.get("state").asText() + "'";
        this.log.info("query for cities :" + query);
        List<Map<String, Object>> executeQuery = this.serverConfig.executeQuery(query);
        ArrayNode convertValue = (ArrayNode)mapper.convertValue(executeQuery, ArrayNode.class);
        exchange.getIn().setBody(convertValue);
      } else if (requestBody.has("documetType")) {
        String query = "select state from ServerConfig where documentType=" + requestBody.get("documetType");
        List<Map<String, Object>> executeQuery = this.serverConfig.executeQuery(query);
        ArrayNode convertValue = (ArrayNode)mapper.convertValue(executeQuery, ArrayNode.class);
        exchange.getIn().setBody(convertValue);
      } else if (requestBody.has("kycRequestJson")) {
        String tokenUrl = requestBody.get("tokenUrl").asText();
        String kycUrl = requestBody.get("kycUrl").asText();
        String tokenKey = requestBody.get("tokenKey").asText();
        String kycRequestJson = requestBody.get("kycRequestJson").asText();
        this.log.info("tokenUrl : " + tokenUrl + "kycUrl :" + kycUrl + "tokenKey :" + tokenKey + "kycRequestJson :" + kycRequestJson);
        Map<String, String> tokenHeader = new HashMap<>();
        tokenHeader.put("api_key", "595d52aa-fc66-4a");
        String tokenResponse = performRequest(tokenUrl, tokenHeader);
        JsonNode jsonResponse = mapper.readTree(tokenResponse);
        String token = jsonResponse.get("data").get("token").asText();
        this.log.info("token :" + token);
        Map<String, String> kycHeaders = new HashMap<>();
        kycHeaders.put(tokenKey, token);
        Map<String, Object> kycParams = (Map<String, Object>)mapper.convertValue(requestBody.get("kycRequestJson"), Map.class);
        StringBuilder encodedParamsBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : kycParams.entrySet()) {
          try {
            if (encodedParamsBuilder.length() > 0)
              encodedParamsBuilder.append("&"); 
            encodedParamsBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
              .append("=")
              .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name()));
          } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
          } 
        } 
        String encodedParams = encodedParamsBuilder.toString();
        String kycUrlWithParams = kycUrl + "?" + encodedParams;
        String kycResponse = performRequest(kycUrlWithParams, kycHeaders);
        this.log.info("kyc service response : " + kycResponse);
        finalResultNode.put("kycResponse", kycResponse);
        exchange.getIn().setBody(finalResultNode);
      } else if (!requestBody.has("URL") && !requestBody.has("request")) {
        finalResultNode.put("responseCode", "P365RES101");
        finalResultNode.put("message", "error");
        finalResultNode.put("data", "request details not found.. URL or request");
        exchange.getIn().setBody(finalResultNode);
      } else {
        Map<String, Object> reqHeaders = new HashMap<>();
        if (requestBody.has("headers")) {
          reqHeaders = (Map<String, Object>)mapper.convertValue(requestBody.get("headers"), Map.class);
        } else {
          reqHeaders.put("Content-Type", "application/json");
          reqHeaders.put("CamelHttpMethod", "POST");
          reqHeaders.put("CamelAcceptContentType", "application/json");
        } 
        if (requestBody.has("carrierId")) {
          ObjectNode requestNode = mapper.createObjectNode();
          JsonNode configurations = mapper.readTree(((JsonObject)this.serverConfig.getDocBYId("KYCINFO").content()).toString());
          if (configurations != null && configurations
            .has(requestBody.get("carrierId").asText())) {
            carrierRequestConfiguration = configurations.get(requestBody.get("carrierId").asText());
            if (carrierRequestConfiguration.has("GenerateUUID"))
              reqHeaders.put(carrierRequestConfiguration.get("GenerateUUID")
                  .get("key").asText(), UUID.randomUUID().toString()); 
            if (carrierRequestConfiguration.has("GenerateJWTToken"))
              reqHeaders.put(carrierRequestConfiguration
                  .get("GenerateJWTToken")
                  .get("AuthKey").asText(), 
                  CreateJWTToken.createFreshJWTToken(carrierRequestConfiguration
                    .get("GenerateJWTToken")
                    .get("SecretKey").asText())); 
            Map<String, String> requestBodyNode = (Map<String, String>)mapper.convertValue(requestBody.get("request"), Map.class);
            JsonNode jsonNode = carrierRequestConfiguration.get("payload");
            for (Map.Entry<String, String> node : requestBodyNode.entrySet()) {
              if (jsonNode.has(node.getKey()))
                ((ObjectNode)jsonNode).put(node.getKey(), node.getValue()); 
            } 
            if (carrierRequestConfiguration.has("encryption") && carrierRequestConfiguration
              .get("encryption").asText().equals("Y")) {
              String encrypt = EncrypDecryptOperation.encrypt(carrierRequestConfiguration
                  .get("GenerateJWTToken")
                  .get("SecretKey").asText(), jsonNode
                  .toString(), carrierRequestConfiguration
                  .get("GenerateJWTToken")
                  .get("SecretKey").asText());
              requestNode.put("payload", encrypt);
              ((ObjectNode)requestBody).put("request", (JsonNode)requestNode);
            } 
            this.log.info("requestBody :" + requestBody);
          } else {
            finalResultNode.put("responseCode", "P365RES101");
            finalResultNode.put("message", "Not Found");
            finalResultNode.put("data", "KYC Request config document not found : KYCINFO");
            exchange.getIn().setBody(finalResultNode);
          } 
        } 
        if (requestBody.has("methodType") && "GET"
          .equalsIgnoreCase(requestBody.get("methodType").asText())) {
          String response = "";
          URL urlR = new URL(requestBody.get("URL").asText());
          HttpsURLConnection conn = (HttpsURLConnection)urlR.openConnection();
          conn.setRequestMethod("GET");
          for (Map.Entry<String, Object> entry : reqHeaders.entrySet())
            conn.setRequestProperty(((String)entry.getKey()).toString(), entry.getValue().toString()); 
          conn.setDoOutput(true);
          BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          String inputLine;
          while ((inputLine = in.readLine()) != null)
            response = String.valueOf(response) + inputLine; 
          in.close();
          exchange.getIn().setBody(response);
        } else {
          CloseableHttpClient httpclient = HttpClientBuilder.create().build();
          String serviceResponse = null;
          BasicHttpParams basicHttpParams = new BasicHttpParams();
          int timeoutConnection = 80000;
          HttpConnectionParams.setConnectionTimeout((HttpParams)basicHttpParams, timeoutConnection);
          int timeoutSocket = 80000;
          HttpConnectionParams.setSoTimeout((HttpParams)basicHttpParams, timeoutSocket);
          HttpPost httppost = new HttpPost(requestBody.get("URL").asText());
          httppost.setParams((HttpParams)basicHttpParams);
          for (Map.Entry<String, Object> entry : reqHeaders.entrySet())
            httppost.setHeader(entry.getKey(), entry.getValue().toString()); 
          StringEntity stringEntity = new StringEntity(requestBody.get("request").toString(), StandardCharsets.UTF_8);
          httppost.setEntity((HttpEntity)stringEntity);
          CloseableHttpResponse closeableHttpResponse = httpclient.execute((HttpUriRequest)httppost);
          HttpEntity r_entity = closeableHttpResponse.getEntity();
          serviceResponse = EntityUtils.toString(r_entity);
          if (requestBody.get("carrierId").asInt() == 52)
            serviceResponse = EncrypDecryptOperation.decrypt(carrierRequestConfiguration
                .get("GenerateJWTToken")
                .get("SecretKey").asText(), mapper
                .readTree(serviceResponse).get("payload").toString(), carrierRequestConfiguration
                .get("GenerateJWTToken")
                .get("SecretKey").asText()); 
          finalResultNode.put("responseCode", "P365RES100");
          finalResultNode.put("message", "response");
          finalResultNode.put("data", mapper.readTree(serviceResponse));
          this.log.info("kyc service response : " + finalResultNode);
          exchange.getIn().setBody(finalResultNode);
        } 
      } 
    } catch (Exception e) {
      this.log.error("error in kyc service invoke class", e);
      finalResultNode.put("responseCode", "P365RES101");
      finalResultNode.put("message", "error");
      finalResultNode.put("data", e.getLocalizedMessage());
      exchange.getIn().setBody(finalResultNode);
    } 
  }
  
  private static String performRequest(String url, Map<String, String> headers) throws IOException {
    CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    HttpGet request = new HttpGet(url);
    if (headers != null)
      for (Map.Entry<String, String> entry : headers.entrySet())
        request.setHeader(entry.getKey(), entry.getValue());  
    CloseableHttpResponse response = httpclient.execute((HttpUriRequest)request);
    return handleResponse(response);
  }
  
  private static String handleResponse(CloseableHttpResponse response) {
    try {
      HttpEntity entity = response.getEntity();
      if (entity != null)
        return EntityUtils.toString(entity); 
    } catch (ParseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        response.close();
      } catch (IOException e) {
        e.printStackTrace();
      } 
    } 
    return "Service not available";
  }
}
