package com.idep.services.impl;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.services.IECMSManager;
import com.idep.services.util.ECMSManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import sun.net.www.protocol.https.Handler;

public class ECMSManagerAPI implements IECMSManager {
  private static class DefaultTrustManager implements X509TrustManager {
    private DefaultTrustManager() {}
    
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
    
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }
  
  ObjectMapper objectMapper = new ObjectMapper();
  
  Logger log = Logger.getLogger(ECMSManagerAPI.class.getName());
  
  ECMSManager ecmsManager = new ECMSManager();
  
  public String uploadPolicyDocument(JsonNode contentMgmtConfig, String fileName, JsonObject metaData) throws HttpException, IOException {
    try {
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(new javax.net.ssl.KeyManager[0], new TrustManager[] { new DefaultTrustManager(null) }, new SecureRandom());
      SSLContext.setDefault(ctx);
      String loginURL = this.ecmsManager.getRestURL("loginURL");
      URL urlR = new URL(null, loginURL, new Handler());
      HttpsURLConnection conn = (HttpsURLConnection)urlR.openConnection();
      conn.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String arg0, SSLSession arg1) {
              return true;
            }
          });
      HttpClient client = new HttpClient();
      GetMethod getAuth = new GetMethod(loginURL);
      client.executeMethod((HttpMethod)getAuth);
      String ticketId = getAuth.getResponseBodyAsString();
      ticketId = ticketId.substring(ticketId.indexOf("<ticket>") + 8, ticketId.indexOf("</ticket>"));
      String uploadDocURL = this.ecmsManager.getRestURL("uploadDocRestURL");
      uploadDocURL = String.valueOf(String.valueOf(uploadDocURL)) + ticketId;
      String filePath = String.valueOf(String.valueOf(contentMgmtConfig.get("fileLocation").asText())) + fileName;
      File file = new File(filePath);
      String filetype = "application/pdf";
      PostMethod postUploadDoc = new PostMethod(uploadDocURL);
      JsonObject ecmsConfigDetails = this.ecmsManager.getConfigDetails();
      Part[] parts = { (Part)new FilePart("filedata", fileName, file, filetype, null), 
          (Part)new StringPart("filename", fileName), 
          (Part)new StringPart("description", contentMgmtConfig.get("description").asText()), 
          (Part)new StringPart("username", ecmsConfigDetails.getString("userName")), 
          (Part)new StringPart("password", ecmsConfigDetails.getString("password")), 
          (Part)new StringPart("siteid", contentMgmtConfig.get("siteid").asText()), 
          (Part)new StringPart("containerid", contentMgmtConfig.get("containerid").asText()), 
          (Part)new StringPart("uploaddirectory", contentMgmtConfig.get("uploaddirectory").asText()) };
      postUploadDoc
        .setRequestEntity((RequestEntity)new MultipartRequestEntity(parts, postUploadDoc.getParams()));
      int status = client.executeMethod((HttpMethod)postUploadDoc);
      if (status == 200) {
        String responseBody = postUploadDoc.getResponseBodyAsString();
        JsonNode responseNode = this.objectMapper.readTree(responseBody);
        postUploadDoc.releaseConnection();
        String nodeRef = responseNode.get("nodeRef").asText();
        nodeRef = nodeRef.substring(nodeRef.lastIndexOf("/") + 1, nodeRef.length());
        String downloadDocURL = formDownloadPolicyDocURL(nodeRef);
        int metaDataStatus = updateMetaDataProp(nodeRef, metaData);
        this.log.info("MetaData Updation Status : " + metaDataStatus);
        return downloadDocURL;
      } 
      return null;
    } catch (Exception e) {
      this.log.info("Exception at ECMSManagerAPI in uploadPolicyDocument method " + e);
      e.printStackTrace();
      return null;
    } 
  }
  
  public String formDownloadPolicyDocURL(String documentId) {
    String downloadDocURL = this.ecmsManager.getRestURL("downloadDocRestURL");
    downloadDocURL = downloadDocURL.replace("<documentId>", documentId);
    System.out.println("downloadDocURL : " + downloadDocURL);
    return downloadDocURL;
  }
  
  public String generateAuthTicket() throws HttpException, IOException {
    SSLContext ctx = SSLContext.getInstance("TLS");
    ctx.init(new javax.net.ssl.KeyManager[0], new TrustManager[] { new DefaultTrustManager(null) }, new SecureRandom());
    SSLContext.setDefault(ctx);
    String loginURL = this.ecmsManager.getRestURL("loginURL");
    URL urlR = new URL(null, loginURL, new Handler());
    HttpsURLConnection conn = (HttpsURLConnection)urlR.openConnection();
    conn.setHostnameVerifier(new HostnameVerifier() {
          public boolean verify(String arg0, SSLSession arg1) {
            return true;
          }
        });
    HttpClient client = new HttpClient();
    GetMethod getAuth = new GetMethod(loginURL);
    client.executeMethod((HttpMethod)getAuth);
    String ticketId = getAuth.getResponseBodyAsString();
    ticketId = ticketId.substring(ticketId.indexOf("<ticket>") + 8, ticketId.indexOf("</ticket>"));
    this.log.info("TicketID : " + ticketId);
    if (!ticketId.equals(null))
      return ticketId; 
    return ticketId;
  }
  
  public int updateMetaDataProp(String documentId, JsonObject metaData) {
    try {
      ObjectNode metaDataNode = this.objectMapper.createObjectNode();
      metaDataNode.put("p365:policyNumber", metaData.getString("policyNumber"));
      metaDataNode.put("p365:customerId", metaData.getString("customerId"));
      metaDataNode.put("p365:emailId", metaData.getString("emailId"));
      metaDataNode.put("p365:mobileNumber", metaData.getString("mobileNumber"));
      metaDataNode.put("p365:policyBond", metaData.getString("policyBond"));
      ObjectNode finalMetaDataNode = this.objectMapper.createObjectNode();
      finalMetaDataNode.put("properties", (JsonNode)metaDataNode);
      String metaDataString = this.objectMapper.writeValueAsString(finalMetaDataNode);
      StringRequestEntity requestEntity = new StringRequestEntity(metaDataString);
      String loginURL = this.ecmsManager.getRestURL("loginURL");
      HttpClient client = new HttpClient();
      GetMethod getAuth = new GetMethod(loginURL);
      client.executeMethod((HttpMethod)getAuth);
      String ticketId = getAuth.getResponseBodyAsString();
      ticketId = ticketId.substring(ticketId.indexOf("<ticket>") + 8, ticketId.indexOf("</ticket>"));
      String uploadMetaDataDocURL = this.ecmsManager.getRestURL("updateMetaDataURL");
      uploadMetaDataDocURL = uploadMetaDataDocURL.replace("<documentId>", documentId);
      uploadMetaDataDocURL = String.valueOf(String.valueOf(uploadMetaDataDocURL)) + ticketId;
      PostMethod postUploadDocMetaData = new PostMethod(uploadMetaDataDocURL);
      postUploadDocMetaData.addRequestHeader("Content-Type", "application/json");
      postUploadDocMetaData.setRequestEntity((RequestEntity)requestEntity);
      int status = client.executeMethod((HttpMethod)postUploadDocMetaData);
      this.log.info("status : " + status);
      String responseBody = postUploadDocMetaData.getResponseBodyAsString();
      this.log.info("Response Body : " + responseBody);
      return status;
    } catch (Exception e) {
      this.log.info("Error due to : " + e);
      return 0;
    } 
  }
  
  public static void main(String[] args) throws HttpException, IOException {}
}
