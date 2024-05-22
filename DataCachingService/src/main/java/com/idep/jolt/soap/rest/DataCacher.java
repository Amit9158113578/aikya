package com.idep.jolt.soap.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class DataCacher {
  static ObjectMapper objectMapper = new ObjectMapper();
  
  static Logger log = Logger.getLogger(DataCacher.class.getName());
  
  static ObjectNode docData = objectMapper.createObjectNode();
  
  static {
    CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
    try {
      log.info("Caching documents required for JOLT, SOAP, REST Service");
      ArrayNode queries = objectMapper.createArrayNode();
      queries.add("select ServerConfig.* from ServerConfig where documentType='JOLTRequestConfiguration'");
      queries.add("select ServerConfig.* from ServerConfig where documentType='JOLTResponseConfiguration'");
      queries.add("select ServerConfig.* from ServerConfig where documentType='SOAPRequestConfiguration'");
      queries.add("select ServerConfig.* from ServerConfig where documentType='SOAPResponseConfiguration'");
      queries.add("select ServerConfig.* from ServerConfig where documentType='RESTRequestConfiguration'");
      queries.add("select ServerConfig.* from ServerConfig where documentType='RESTResponseConfiguration'");
      for (JsonNode query : queries) {
        String eachQuery = query.asText();
        List<Map<String, Object>> configDocNode = serverConfig.executeQuery(eachQuery);
        if (configDocNode.size() <= 0) {
          log.info("documents caching retrying :" + eachQuery);
          Thread.sleep(6L);
          configDocNode = serverConfig.executeQuery(eachQuery);
        } 
        for (Map<String, Object> nodeValue : configDocNode) {
          JsonNode node = (JsonNode)objectMapper.convertValue(nodeValue, JsonNode.class);
          int carrierId = node.get("carrierId").asInt();
          int productId = node.get("productId").asInt();
          int lob = node.get("lob").asInt();
          String stage = node.get("stage").asText();
          String docType = node.get("documentType").asText();
          docType = docType.replace("Configuration", "");
          if (node.findValue("policyType") != null) {
            String policyType = node.get("policyType").asText();
            String str1 = String.valueOf(docType) + "-" + stage + "-" + lob + "-" + carrierId + "-" + productId + "-" + policyType;
            log.info("docId with policyType:" + str1);
            docData.put(str1, node);
            continue;
          } 
          String docId = String.valueOf(docType) + "-" + stage + "-" + lob + "-" + carrierId + "-" + productId;
          log.info("docId without policyType :" + docId);
          docData.put(docId, node);
        } 
      } 
      log.info("Cached documents required for JOLT, SOAP, REST Service :" + docData.size());
    } catch (Exception e) {
      log.info("Failed to cache documents required for JOLT, SOAP, REST Service :" + e);
    } 
  }
  
  public JsonNode docCache(String docId) {
    return docData.get(docId);
  }
}
