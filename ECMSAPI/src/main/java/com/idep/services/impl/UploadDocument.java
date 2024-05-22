package com.idep.services.impl;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.io.File;
import java.io.IOException;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class UploadDocument {
  ObjectMapper objectMapper = new ObjectMapper();
  
  Logger log = Logger.getLogger(UploadDocument.class.getName());
  
  CBService PospDataService = CBInstanceProvider.getBucketInstance("PospData");
  
  JsonObject pospConfig = getContentConfiguration();
  
  public String uploadPospDocument(String filePath, JsonNode metaData) {
    try {
      String loginURL = this.pospConfig.get(
          "loginURL").toString();
      HttpClient client = new HttpClient();
      GetMethod getAuth = new GetMethod(loginURL);
      client.executeMethod((HttpMethod)getAuth);
      String ticketId = getAuth.getResponseBodyAsString();
      this.log.info("ticketId genrated for POSP Document : " + ticketId);
      ticketId = ticketId.substring(ticketId.indexOf("<ticket>") + 8, 
          ticketId.indexOf("</ticket>"));
      String uploadDocURL = this.pospConfig.get("uploadDocRestURL")
        .toString();
      uploadDocURL = String.valueOf(String.valueOf(uploadDocURL)) + ticketId;
      File file = new File(filePath);
      System.out.println("File path : " + filePath);
      String filetype = getFileType(filePath, file.getName());
      PostMethod postUploadDoc = new PostMethod(uploadDocURL);
      String uploadDir = "";
      if (metaData.has("documentType")) {
        this.log.info("POSP Document Upload Request found : " + metaData.get("documentType").asText());
        uploadDir = this.pospConfig.getString(metaData.get("documentType").asText()).toString();
        if (uploadDir == null || uploadDir.equalsIgnoreCase(""))
          uploadDir = this.pospConfig.getString("otherDirAlfresco"); 
      } else {
        uploadDir = this.pospConfig.getString("otherDirAlfresco");
      } 
      Part[] parts = { (Part)new FilePart("filedata", 
            String.valueOf(String.valueOf(metaData.get("agentId").asText())) + "_" + file.getName(), 
            file, filetype, null), 
          (Part)new StringPart("filename", 
            String.valueOf(String.valueOf(metaData.get("agentId").asText())) + 
            "_" + file.getName()), 
          (Part)new StringPart("Title", metaData.get("agentId")
            .asText()), 
          (Part)new StringPart("Description", metaData.get(
              "description").asText()), 
          (Part)new StringPart("username", 
            this.pospConfig.getString("userName")), 
          (Part)new StringPart("password", 
            this.pospConfig.getString("password")), 
          (Part)new StringPart("siteid", this.pospConfig.getString(
              "siteid")), 
          (Part)new StringPart("containerid", this.pospConfig.getString(
              "containerid")), 
          (Part)new StringPart("uploaddirectory", uploadDir) };
      this.log.info("File Parts Generated :  " + uploadDir);
      postUploadDoc.setRequestEntity((RequestEntity)new MultipartRequestEntity(parts, 
            postUploadDoc.getParams()));
      int status = client.executeMethod((HttpMethod)postUploadDoc);
      if (status == 200) {
        String responseBody = postUploadDoc.getResponseBodyAsString();
        JsonNode responseNode = this.objectMapper.readTree(responseBody);
        postUploadDoc.releaseConnection();
        String nodeRef = responseNode.get("nodeRef").asText();
        nodeRef = nodeRef.substring(nodeRef.lastIndexOf("/") + 1, 
            nodeRef.length());
        String downloadDocURL = genrateDocumentDownloadURL(nodeRef);
        int metaDataStatus = updateDocumentProperties(nodeRef, metaData);
        this.log.info("Document Properties Updation Status : " + 
            metaDataStatus);
        System.out.println("Document Properties Updation Status : " + 
            metaDataStatus);
        this.log.info("Document Download URL POSP : " + downloadDocURL);
        System.out.println("Document Download URL POSP : " + 
            downloadDocURL);
        return downloadDocURL;
      } 
      System.out.println("Something Went Wrong : Http STATUS : " + 
          status);
      return null;
    } catch (Exception e) {
      this.log.error("Exception at ECMSManagerAPI in uploadPolicyDocument method ", 
          e);
      e.printStackTrace();
      return null;
    } 
  }
  
  public String getFileType(String filePath, String fileName) {
    try {
      return FilenameUtils.getExtension(fileName);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public JsonObject getContentConfiguration() {
    try {
      JsonDocument jsonDocument = this.PospDataService.getDocBYId("PospContentManagementConfig");
      if (jsonDocument != null)
        return (JsonObject)jsonDocument.content(); 
      this.log.error("unable to load Configuraion from DB : PospContentManagementConfig ");
      return null;
    } catch (Exception e) {
      this.log.error(
          "unable to load Configuraion from DB : PospContentManagementConfig ", 
          e);
      return null;
    } 
  }
  
  public String genrateDocumentDownloadURL(String documentId) {
    String docDownloadURL = this.pospConfig.get("downloadDocRestURL")
      .toString();
    docDownloadURL = docDownloadURL.replace("<documentId>", documentId);
    this.log.info("Document download URL : " + docDownloadURL);
    return docDownloadURL;
  }
  
  public int updateDocumentProperties(String documentId, JsonNode metaData) {
    try {
      ObjectNode metaDataNode = this.objectMapper.createObjectNode();
      metaDataNode.put("posp:agentId", metaData.get("agentId").asText()
          .trim());
      metaDataNode.put("cm:title", metaData.get("agentId").asText()
          .trim());
      metaDataNode.put("cm:description", metaData.get("agentId").asText()
          .trim());
      metaDataNode.put("posp:documentType", metaData.get("documentType")
          .asText());
      ObjectNode finalMetaDataNode = this.objectMapper.createObjectNode();
      finalMetaDataNode.put("properties", (JsonNode)metaDataNode);
      String metaDataString = this.objectMapper
        .writeValueAsString(finalMetaDataNode);
      StringRequestEntity requestEntity = new StringRequestEntity(
          metaDataString);
      String loginURL = this.pospConfig.get(
          "loginURL").toString();
      HttpClient client = new HttpClient();
      GetMethod getAuth = new GetMethod(loginURL);
      client.executeMethod((HttpMethod)getAuth);
      String ticketId = getAuth.getResponseBodyAsString();
      ticketId = ticketId.substring(ticketId.indexOf("<ticket>") + 8, 
          ticketId.indexOf("</ticket>"));
      String uploadMetaDataDocURL = this.pospConfig.get(
          "updateMetaDataURL").toString();
      System.out
        .println("uploadMetaDataDocURL : " + uploadMetaDataDocURL);
      uploadMetaDataDocURL = uploadMetaDataDocURL.replace("<documentId>", 
          documentId);
      uploadMetaDataDocURL = String.valueOf(String.valueOf(uploadMetaDataDocURL)) + ticketId;
      PostMethod postUploadDocMetaData = new PostMethod(
          uploadMetaDataDocURL);
      postUploadDocMetaData.addRequestHeader("Content-Type", 
          "application/json");
      postUploadDocMetaData.setRequestEntity((RequestEntity)requestEntity);
      int status = client.executeMethod((HttpMethod)postUploadDocMetaData);
      this.log.info("Alfresco Docment Properties Updation status : " + status);
      String responseBody = postUploadDocMetaData.getResponseBodyAsString();
      return status;
    } catch (Exception e) {
      this.log.error("Unable to process request updateDocumentProperties() : ", e);
      return 0;
    } 
  }
  
  public String generatePOSPDocAuthTicket(String url) throws HttpException, IOException {
    HttpClient client = new HttpClient();
    GetMethod getAuth = new GetMethod(url);
    client.executeMethod((HttpMethod)getAuth);
    String ticketId = getAuth.getResponseBodyAsString();
    ticketId = ticketId.substring(ticketId.indexOf("<ticket>") + 8, ticketId.indexOf("</ticket>"));
    this.log.info("TicketID : " + ticketId);
    if (!ticketId.equals(null))
      return ticketId; 
    return ticketId;
  }
}
