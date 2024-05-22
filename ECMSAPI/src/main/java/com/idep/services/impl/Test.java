package com.idep.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.httpclient.HttpException;

public class Test {
  public static void main(String[] args) throws HttpException, IOException {
    String jsonString = "{\"fetchDocumentNameResponseElement\": {\"userCode\": \"POLICY365\",\"indexType\": [{\"index\": \"2272700\",\"type\": \"POLICY\"},{\"index\": \"2272702\",\"type\": \"POLICY\"},{\"index\": \"2272701\",\"type\": \"POLICY\"}],\"docs\": [{\"value\": \"PC_COLLECTIONRECEIPT_34157722.pdf\",\"name\": \"PC_COLLECTIONRECEIPT_34157722.pdf\"},{\"value\": \"POLICY_DOCUMENT_240531383531052017.pdf\",\"name\": \"POLICY_DOCUMENT_240531383531052017.pdf\"},{\"value\": \"PC_NIAPOLICYSCHEDULECIRTIFICATEPC_34157723.pdf\",\"name\": \"PC_NIAPOLICYSCHEDULECIRTIFICATEPC_34157723.pdf\"}],\"policyId\": \"240531383531052017\"}}";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode reqNode = objectMapper.readTree(jsonString);
    System.out.println("Req Node : " + reqNode);
    JsonNode indexArray = reqNode.get("fetchDocumentNameResponseElement").get("indexType");
    JsonNode docsArray = reqNode.get("fetchDocumentNameResponseElement").get("docs");
    System.out.println("IndexArray : " + indexArray);
    System.out.println("DocsArray : " + docsArray);
    int policyIndexNumber = 0;
    String fieldString = "{\"fieldNameReplacement\": [\"<policyNumber>\",\"<indexId>\"]}";
    JsonNode fieldNode = objectMapper.readTree(fieldString);
    for (int i = 0; i <= docsArray.size(); i++) {
      if (docsArray.get(i).get("name").asText().substring(0, 15).equals("POLICY_DOCUMENT")) {
        policyIndexNumber = indexArray.get(i).get("index").asInt();
        System.out.println("policyIndexNumber :" + policyIndexNumber);
        System.out.println("iterating : " + i);
        break;
      } 
    } 
    for (JsonNode str : fieldNode.get("fieldNameReplacement"))
      System.out.println("Str : " + str.asText()); 
    System.out.println("index found : " + policyIndexNumber);
  }
}
