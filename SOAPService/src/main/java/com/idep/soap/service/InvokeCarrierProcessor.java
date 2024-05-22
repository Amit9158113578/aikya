package com.idep.soap.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.idep.soap.util.ResponseMessageProcessor;
import com.idep.soap.util.SoapUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
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

public class InvokeCarrierProcessor implements Processor {
  Logger log = Logger.getLogger(InvokeCarrierProcessor.class.getName());
  
  public void process(Exchange exchange) throws Exception {
    CloseableHttpClient httpclient = HttpClientBuilder.create().build();
    int carrierId = 0;
    try {
      String reqBody = (String)exchange.getIn().getBody();
      JsonNode requestBody = SoapUtils.objectMapper.readTree(reqBody);
      JsonNode reqProperty = SoapUtils.objectMapper.readTree(exchange.getProperty("inputRequest").toString());
      carrierId = reqProperty.findValue("carrierId").asInt();
      int productId = reqProperty.findValue("productId").asInt();
      String url = requestBody.get("url").asText();
      String request = requestBody.get("carrierData").asText();
      JsonNode headers = requestBody.get("headers");
      Map<String, Object> reqHeaders = new HashMap<>();
      reqHeaders = (Map<String, Object>)SoapUtils.objectMapper.readValue(headers.toString(), new TypeReference<Map<String, String>>() {
          
          });
      String serviceResponse = null;
      BasicHttpParams basicHttpParams = new BasicHttpParams();
      int timeoutConnection = 40000;
      HttpConnectionParams.setConnectionTimeout((HttpParams)basicHttpParams, timeoutConnection);
      int timeoutSocket = 40000;
      HttpConnectionParams.setSoTimeout((HttpParams)basicHttpParams, timeoutSocket);
      RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
      requestConfigBuilder.setConnectionRequestTimeout(20);
      HttpPost httppost = new HttpPost(url);
      httppost.setParams((HttpParams)basicHttpParams);
      for (Map.Entry<String, Object> entry : reqHeaders.entrySet())
        httppost.setHeader(entry.getKey(), entry.getValue().toString()); 
      StringEntity stringEntity = new StringEntity(request, StandardCharsets.UTF_8);
      httppost.setEntity((HttpEntity)stringEntity);
      this.log.info("Complete Request for CarrierId:" + carrierId + ", ProductId:" + productId + " is " + EntityUtils.toString((HttpEntity)stringEntity));
      this.log.info("URL :" + url);
      CloseableHttpResponse closeableHttpResponse = httpclient.execute((HttpUriRequest)httppost);
      HttpEntity r_entity = closeableHttpResponse.getEntity();
      Header[] resHeaders = closeableHttpResponse.getAllHeaders();
      byte b;
      int i;
      Header[] arrayOfHeader1;
      for (i = (arrayOfHeader1 = resHeaders).length, b = 0; b < i; ) {
        Header h = arrayOfHeader1[b];
        exchange.getIn().setHeader(h.getName(), h.getValue());
        b = (byte)(b + 1);
      } 
      serviceResponse = EntityUtils.toString(r_entity);
      this.log.info("SOAP service response for CarrierId:" + carrierId + ", ProductId:" + productId + " is " + serviceResponse);
      exchange.getIn().setBody(serviceResponse);
      exchange.getIn().setHeader("serviceDown", "False");
      httpclient.close();
    } catch (Exception e) {
      this.log.error("Exception in InvokeCarrierProcessor :" + e);
      exchange.getIn().setBody(ResponseMessageProcessor.returnFailedResponse(e.toString(), carrierId));
      exchange.getIn().setHeader("serviceDown", "True");
      httpclient.close();
    } finally {
      httpclient.close();
    } 
  }
}
