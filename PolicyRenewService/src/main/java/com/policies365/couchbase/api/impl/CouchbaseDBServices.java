package com.policies365.couchbase.api.impl;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.util.Database;
import com.idep.policyrenew.util.PolicyRenewConstatnt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouchbaseDBServices
{
  private static Map<String, CBService> cbMap = new HashMap();
  
  static
  {
    String[] arrayOfString;
    int j = (arrayOfString = PolicyRenewConstatnt.BUCKET_LIST).length;
    for (int i = 0; i < j; i++)
    {
      String bucket = arrayOfString[i];
      
      Database db = new Database();
      db.setBucket(bucket);
      db.setPassword(bucket);
      System.out.println("Bucket instance is getting created :" + bucket);
      cbMap.put(bucket, new CBService(db));
    }
  }
  
  public String createAsyncDocument(String docId, String doc, String bucket)
  {
    JsonObject jsonObject = JsonObject.fromJson(doc);
    if (((CBService)cbMap.get(bucket)).getDocBYId(docId) != null) {
      return ((CBService)cbMap.get(bucket)).replaceAsyncDocument(docId, jsonObject);
    }
    return ((CBService)cbMap.get(bucket)).createAsyncDocument(docId, jsonObject);
  }
  
  public String createAsyncDocument(String docId, JsonObject jsonObject, String bucket)
  {
    return ((CBService)cbMap.get(bucket)).createAsyncDocument(docId, jsonObject);
  }
  
  public String getDocBYId(String docId, String bucket)
  {
    return ((JsonObject)((CBService)cbMap.get(bucket)).getDocBYId(docId).content()).toString();
  }
  
  public JsonDocument getJsonDocBYId(String docId, String bucket)
  {
    return ((CBService)cbMap.get(bucket)).getDocBYId(docId);
  }
  
  public List<Map<String, Object>> executeQuery(String query, String bucket)
  {
    return ((CBService)cbMap.get(bucket)).executeQuery(query);
  }
}
