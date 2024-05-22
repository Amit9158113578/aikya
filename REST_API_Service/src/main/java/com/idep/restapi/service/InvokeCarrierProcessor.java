package com.idep.restapi.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
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

import com.couchbase.client.core.deps.io.netty.util.internal.StringUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.restapi.utils.ResponseMessageProcessor;
import com.idep.restapi.utils.RestAPIConstants;

public class InvokeCarrierProcessor implements Processor {
  Logger log = Logger.getLogger(InvokeCarrierProcessor.class.getName());
  
  private static class DefaultTrustManager implements X509TrustManager {
    private DefaultTrustManager() {}
    
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }
  
  public void process(Exchange exchange) {
    try {
      Object request;
      String reqBody = (String)exchange.getIn().getBody();
      JsonNode requestBody = RestAPIConstants.objectMapper.readTree(reqBody);
      String url = requestBody.get("url").asText().replaceAll(" ", "+");
      if (exchange.getIn().getHeader("RestDataType").equals("JSON")) {
        request = requestBody.get("carrierData");
      } else {
        request = requestBody.get("carrierData").asText();
      } 
      
      JsonNode headers = requestBody.get("headers");
      Map<String, Object> reqHeaders = new HashMap<>();
      reqHeaders = (Map<String, Object>)RestAPIConstants.objectMapper.readValue(headers.toString(), new TypeReference<Map<String, String>>() {
          
          });
      this.log.info("request URL : " + url);
      if (exchange.getIn().getHeader("ByPassSSL").toString().equalsIgnoreCase("True")) {
        this.log.info("1");
        try {
          this.log.info("0");
          SSLContext ctx = SSLContext.getInstance("TLS");
          ctx.init(new javax.net.ssl.KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
          SSLContext.setDefault(ctx);
          String response = "";
          URL urlR = new URL(url);
          HttpsURLConnection conn = (HttpsURLConnection)urlR.openConnection();
          this.log.info("1");
          conn.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String arg0, SSLSession arg1) {
                  return true;
                }
              });
          this.log.info("2");
          if (exchange.getIn().getHeader("isRequestTypeGET").toString().equalsIgnoreCase("True")) {
            this.log.info("Inside SSL Tester GET METHOD :");
            conn.setRequestMethod("GET");
            for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
              this.log.info("request Header : " + (String)entry.getKey() + " : " + entry.getValue().toString());
              conn.setRequestProperty(((String)entry.getKey()).toString(), entry.getValue().toString());
            } 
            conn.setDoOutput(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(
                  conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
              response = String.valueOf(response) + inputLine; 
            in.close();
            ObjectNode ns = RestAPIConstants.objectMapper.createObjectNode();
            response = response.replace("\\", "");
            response = response.replaceAll("\"", "");
            this.log.info("KOTAK SHARE PDF RESPONSE : " + response);
            ns.put("base64", response);
            exchange.getIn().setBody(ns);
            this.log.info("Setting base64 in Node");
          } else {
            this.log.info("Inside SSL Tester POST METHOD :");
            conn.setRequestMethod("POST");
            for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
              this.log.info("request Header : " + (String)entry.getKey() + " : " + entry.getValue().toString());
              conn.setRequestProperty(((String)entry.getKey()).toString(), entry.getValue().toString());
            } 
            conn.setDoOutput(true);
            this.log.info("3");
            String jsonInputString = request.toString();
            byte[] compressedData = jsonInputString.getBytes();
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.write(compressedData);
            outputStream.flush();
            this.log.info("4");
            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null)
              response = String.valueOf(response) + inputLine; 
            in.close();
            exchange.getIn().setBody(response);
            outputStream.close();
          } 
          this.log.info("SSL TESTER RESPONSE :" + response.toString());
          conn.disconnect();
        } catch (Exception e) {
          this.log.info("Error SSLTESTER: " + e.getMessage());
          this.log.error("Ops!", e);
          exchange.getIn().setBody(ResponseMessageProcessor.returnDynamicResponse("carrier timeout exception"));
          exchange.getIn().setHeader("carrierResponse", "Failure");
        } 
      } else {
        this.log.info("NOT IN  SSL TESTER" + url);
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        String serviceResponse = null;
        try {
          BasicHttpParams basicHttpParams = new BasicHttpParams();
          int timeoutConnection = 80000;
          HttpConnectionParams.setConnectionTimeout((HttpParams)basicHttpParams, timeoutConnection);
          int timeoutSocket = 80000;
          HttpConnectionParams.setSoTimeout((HttpParams)basicHttpParams, timeoutSocket);
          if (exchange.getIn().getHeader("isRequestTypeGET").toString().equalsIgnoreCase("True")) {
        	  if(exchange.getIn().getHeader(RestAPIConstants.IS_METHOD_TYPE_GET).toString().equalsIgnoreCase("True"))
	           {
	        	   GetMethod(reqHeaders, url, exchange,serviceResponse);
	           }
	           else
	           {
	        	String response = "";
		        URL urlR = new URL(url);
	            HttpsURLConnection conn = (HttpsURLConnection)urlR.openConnection();
	            conn.setRequestMethod("GET");
	            for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
	              this.log.info("request Header : " + (String)entry.getKey() + " : " + entry.getValue().toString());
	              conn.setRequestProperty(((String)entry.getKey()).toString(), entry.getValue().toString());
	            } 
	            conn.setDoOutput(true);
	            BufferedReader in = new BufferedReader(new InputStreamReader(
	                  conn.getInputStream()));
	            String inputLine;
	            while ((inputLine = in.readLine()) != null)
	              response = String.valueOf(response) + inputLine; 
	            in.close();
	            ObjectNode ns = RestAPIConstants.objectMapper.createObjectNode();
	            response = response.replace("\\", "");
	            response = response.replaceAll("\"", "");
	            this.log.info("KOTAK SHARE PDF RESPONSE : " + response);
	            ns.put("base64", response);
	            exchange.getIn().setBody(ns);
	            this.log.info("Setting base64 in Node");
	            exchange.getIn().setBody(response);
	           }
          }
          else {
            HttpPost httppost = new HttpPost(url);
            httppost.setParams((HttpParams)basicHttpParams);
            for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
              this.log.info("request Header : " + (String)entry.getKey() + " : " + entry.getValue().toString());
              httppost.setHeader(entry.getKey(), entry.getValue().toString());
            } 
            if(httppost.getFirstHeader("Content-Type").getValue().equals("application/x-www-form-urlencoded"))
            {
            	String form = StringUtil.EMPTY_STRING;
            	Map<String,String> parameters = RestAPIConstants.objectMapper.convertValue(request, Map.class);
            	for (Map.Entry<String,String> entry : parameters.entrySet()) 
            	{
            		form=form+entry.getKey()+"="+URLEncoder.encode(entry.getValue().toString())+"&";
            	}
            	form = form.substring(0, form.length()-1);
            	StringEntity stringEntity = new StringEntity(form, StandardCharsets.UTF_8);
                httppost.setEntity((HttpEntity)stringEntity);
                CloseableHttpResponse closeableHttpResponse = httpclient.execute((HttpUriRequest)httppost);
                HttpEntity r_entity = closeableHttpResponse.getEntity();
                 serviceResponse = EntityUtils.toString(r_entity);
                 this.log.info("Complete Request for content type check :" + EntityUtils.toString((HttpEntity)stringEntity));
            }
            else {
            	StringEntity stringEntity = new StringEntity(request.toString(), StandardCharsets.UTF_8);
                httppost.setEntity((HttpEntity)stringEntity);
                this.log.info("Complete Request :" + EntityUtils.toString((HttpEntity)stringEntity));
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
                this.log.info("REST service response : " + serviceResponse);
            }
            exchange.getIn().setBody(serviceResponse);
          } 
          httpclient.close();
        } catch (Exception e) {
          httpclient.close();
          exchange.getIn().setBody(ResponseMessageProcessor.returnDynamicResponse("carrier timeout exception"));
          exchange.getIn().setHeader("carrierResponse", "Failure");
          this.log.error("Ops!", e);
        } finally {
          httpclient.close();
        } 
      } 
    } catch (Exception e) {
      this.log.error("Ops!", e);
      exchange.getIn().setBody(ResponseMessageProcessor.returnDynamicResponse("carrier timeout exception"));
      exchange.getIn().setHeader("carrierResponse", "Failure");
    } 
  }
  
  public void GetMethod(Map<String, Object> reqHeaders, String url, Exchange exchange ,String serviceResponse) throws ClientProtocolException, IOException, URISyntaxException
  {
      CloseableHttpClient httpclient = HttpClientBuilder.create().build();

	  HttpGet httpget = new HttpGet();
	  URI uri=new URI(url);
	  httpget.setURI(uri);
      for (Map.Entry<String, Object> entry : reqHeaders.entrySet()) {
        this.log.info("GET request Header : " + (String)entry.getKey() + " : " + entry.getValue().toString());
        httpget.setHeader(entry.getKey(), entry.getValue().toString());
      } 
      CloseableHttpResponse closeableHttpResponse = httpclient.execute((HttpUriRequest)httpget);
      HttpEntity r_entity = closeableHttpResponse.getEntity();
      serviceResponse = EntityUtils.toString(r_entity);
      this.log.info("GET method response : " + serviceResponse);
      exchange.getIn().setBody(serviceResponse);
  }
}
