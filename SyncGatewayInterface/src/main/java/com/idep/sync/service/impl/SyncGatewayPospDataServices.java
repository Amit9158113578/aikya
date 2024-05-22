package com.idep.sync.service.impl;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class SyncGatewayPospDataServices {
  ObjectMapper objectMapper = new ObjectMapper();
  
  Logger log = Logger.getLogger(SyncGatewayPospDataServices.class.getName());
  
  public JsonNode getPospDataDocumentBySync(String docid) {
    JsonNode documentNode = null;
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      HttpGet httpGet = null;
      httpGet = new HttpGet(String.valueOf(SyncGatewayConfigLoader.syncPospDataPublicURL) + docid);
      HttpResponse response = httpclient.execute((HttpUriRequest)httpGet);
      this.log.info("retrieve document status code : " + response.getStatusLine());
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        documentNode = this.objectMapper.readTree(EntityUtils.toString(entity));
        this.log.info("sync gateway response : " + documentNode);
        if (!documentNode.has("_id") || !documentNode.has("_rev")) {
          this.log.error("gateway response : " + documentNode);
          documentNode = null;
        } 
      } 
    } catch (ConnectException e) {
      this.log.error("ConnectException : unable to connect sync gateway REST API", e);
    } catch (SocketException e) {
      this.log.error("SocketException : unable to connect sync gateway REST API", e);
    } catch (SocketTimeoutException e) {
      this.log.error("SocketTimeoutException : unable to connect sync gateway REST API", e);
    } catch (Exception e) {
      this.log.error("sync gateway REST API error: ", e);
    } finally {
      httpclient.getConnectionManager().shutdown();
    } 
    return documentNode;
  }
  
  public String createPospDataDocumentBySync(String docid, JsonObject jsonobj) {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      jsonobj.put("_id", docid);
      HttpPost httppost = null;
      httppost = new HttpPost(SyncGatewayConfigLoader.syncPospDataPublicURL);
      StringEntity input = new StringEntity(jsonobj.toString(), ContentType.APPLICATION_JSON);
      httppost.setEntity((HttpEntity)input);
      HttpResponse response = httpclient.execute((HttpUriRequest)httppost);
      this.log.info("create document response code : " + response.getStatusLine());
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        JsonNode documentResNode = this.objectMapper.readTree(EntityUtils.toString(entity));
        this.log.info("gateway response : " + documentResNode);
        if (documentResNode.has("id") && documentResNode.has("rev") && documentResNode.has("ok")) {
          if (documentResNode.get("ok").asBoolean())
            return "doc_created"; 
          this.log.error("gateway response : " + documentResNode);
          return "error";
        } 
        if (documentResNode.has("error") && documentResNode.has("reason")) {
          if (documentResNode.get("error").asText().equalsIgnoreCase("conflict") && 
            documentResNode.get("reason").asText().equalsIgnoreCase("Document exists")) {
            this.log.error(String.valueOf(docid) + " document already exist : " + documentResNode);
            return "doc_exist";
          } 
          this.log.error("gateway response : " + documentResNode);
          return "error";
        } 
        this.log.error("gateway response : " + documentResNode);
        return "error";
      } 
      this.log.error("status code : " + response.getStatusLine());
      return "error";
    } catch (ConnectException e) {
      this.log.error("ConnectException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (SocketException e) {
      this.log.error("SocketException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (SocketTimeoutException e) {
      this.log.error("SocketTimeoutException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (Exception e) {
      this.log.error("sync gateway REST API error : ", e);
      return "error";
    } finally {
      httpclient.getConnectionManager().shutdown();
    } 
  }
  
  public String replacePospDataDocumentBySync(String docid, JsonObject jsonobj) {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      JsonNode documentNode = getPospDataDocumentBySync(docid);
      if (documentNode != null) {
        jsonobj.put("_id", docid);
        HttpPut httpput = null;
        httpput = new HttpPut(String.valueOf(SyncGatewayConfigLoader.syncPospDataPublicURL) + docid + "?new_edits=true&rev=" + documentNode.get("_rev").asText());
        StringEntity input = new StringEntity(jsonobj.toString(), ContentType.APPLICATION_JSON);
        httpput.setEntity((HttpEntity)input);
        HttpResponse response = httpclient.execute((HttpUriRequest)httpput);
        this.log.info("create document response code : " + response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          JsonNode documentResNode = this.objectMapper.readTree(EntityUtils.toString(entity));
          this.log.info("gateway response : " + documentResNode);
          if (documentResNode.has("id") && documentResNode.has("rev") && documentResNode.has("ok")) {
            if (documentResNode.get("ok").asBoolean())
              return "doc_updated"; 
            this.log.error(String.valueOf(docid) + " update document gateway response : " + documentResNode);
            return "error";
          } 
          this.log.error("gateway response : " + documentResNode);
          return "error";
        } 
        this.log.error("status code : " + response.getStatusLine());
        return "error";
      } 
      this.log.error("document not found by sync gateway : " + docid);
      return "doc_notexist";
    } catch (ConnectException e) {
      this.log.error("ConnectException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (SocketException e) {
      this.log.error("SocketException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (SocketTimeoutException e) {
      this.log.error("SocketTimeoutException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (Exception e) {
      this.log.error("sync gateway REST API error : ", e);
      return "error";
    } finally {
      httpclient.getConnectionManager().shutdown();
    } 
  }
  
  public String deletePospDataDocumentBySync(String docid) {
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      JsonNode documentNode = getPospDataDocumentBySync(docid);
      if (documentNode != null) {
        HttpDelete httpDelete = null;
        httpDelete = new HttpDelete(String.valueOf(SyncGatewayConfigLoader.syncPospDataPublicURL) + docid + "?new_edits=true&rev=" + documentNode.get("_rev").asText());
        HttpResponse response = httpclient.execute((HttpUriRequest)httpDelete);
        this.log.info("delete document response code : " + response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          JsonNode documentResNode = this.objectMapper.readTree(EntityUtils.toString(entity));
          this.log.info("gateway response : " + documentResNode);
          if (documentResNode.has("id") && documentResNode.has("rev") && documentResNode.has("ok")) {
            if (documentResNode.get("ok").asBoolean())
              return "doc_deleted"; 
            this.log.error(String.valueOf(docid) + " delete document gateway response : " + documentResNode);
            return "error";
          } 
          this.log.error("sync gateway response : " + documentResNode);
          return "error";
        } 
        this.log.error("status code : " + response.getStatusLine());
        return "error";
      } 
      this.log.error("document not found by sync gateway : " + docid);
      return "doc_notexist";
    } catch (ConnectException e) {
      this.log.error("ConnectException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (SocketException e) {
      this.log.error("SocketException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (SocketTimeoutException e) {
      this.log.error("SocketTimeoutException : unable to connect sync gateway REST API", e);
      return "error";
    } catch (Exception e) {
      this.log.error("sync gateway REST API error : ", e);
      return "error";
    } finally {
      System.out.println("closing connection");
      httpclient.getConnectionManager().shutdown();
    } 
  }
  
  public JsonNode executePospDataSyncGatewayView(String viewName, String key) {
    JsonNode viewResNode = null;
    DefaultHttpClient httpclient = new DefaultHttpClient();
    try {
      HttpGet httpGet = null;
      String keyFormat = "?key=%22" + key + "%22";
      String viewURL = String.valueOf(SyncGatewayConfigLoader.syncAdminBaseURL) + 
        SyncGatewayConfigLoader.getProperty(viewName) + 
        keyFormat + 
        "&stale=update_after";
      httpGet = new HttpGet(viewURL);
      HttpResponse response = httpclient.execute((HttpUriRequest)httpGet);
      this.log.info("sync gateway VIEW execution status code : " + response.getStatusLine());
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        viewResNode = this.objectMapper.readTree(EntityUtils.toString(entity));
        this.log.info("sync gateway view execution response : " + viewResNode);
        if (viewResNode.has("rows") && viewResNode.has("total_rows")) {
          if (viewResNode.get("rows").size() > 0)
            return viewResNode.get("rows").get(0); 
          this.log.error("SYNC VIEW Execution : no records found : " + viewResNode);
          viewResNode = null;
        } else {
          this.log.error("sync gateway view execution response : " + viewResNode);
          viewResNode = null;
        } 
      } 
    } catch (ConnectException e) {
      this.log.error("ConnectException : unable to connect sync gateway VIEW REST API", e);
    } catch (SocketException e) {
      this.log.error("SocketException : unable to connect sync gateway VIEW REST API", e);
    } catch (SocketTimeoutException e) {
      this.log.error("SocketTimeoutException : unable to connect sync gateway VIEW REST API", e);
    } catch (Exception e) {
      this.log.error("sync gateway VIEW REST API error: ", e);
    } finally {
      httpclient.getConnectionManager().shutdown();
    } 
    return viewResNode;
  }
}
