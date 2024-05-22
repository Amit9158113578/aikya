package com.idep.couchbase.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import rx.Observable;
import rx.functions.Func1;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.ViewDoesNotExistException;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlParams;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.couchbase.client.java.view.Stale;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.util.APIConstants;
import com.idep.couchbase.api.util.Database;

/**
 * 
 * @author sandeep.jadhav
 * couchbase services
 */
public class CBService
{
  private final Database config;
  private final Bucket bucket;
  ObjectMapper objectMapper = new ObjectMapper();
  static Logger log = Logger.getLogger(CBService.class.getName());
  N1qlParams n1qlparam = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false).maxParallelism(APIConstants.MAX_PARALLEL_QUERY);
  N1qlParams n1qlparamBounded = N1qlParams.build().consistency(ScanConsistency.REQUEST_PLUS).adhoc(false).maxParallelism(APIConstants.MAX_PARALLEL_QUERY);
  N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).maxParallelism(APIConstants.MAX_PARALLEL_QUERY);
 
  
  public CBService(Database conf)
  {
	  
		  this.config = conf;
		  bucket = CBClusterConfiguration.getClusterEnv().openBucket(config.getBucket(),config.getPassword());
		  log.info("bucket instance created  using authenticate : config.getBucket()");
  }
  
  public void preDestory()
  {
    if (CBClusterConfiguration.getClusterEnv() != null)
    {
      this.bucket.close();
      CBClusterConfiguration.getClusterEnv().disconnect();
    }
  }
  
  public void closeBucket()
  {
    if (this.bucket != null) {
      this.bucket.close();
    }
  }
  
 
  public String updateDocument(String docid, final HashMap<Object, Object> parameters) throws InterruptedException,Exception
	{
		bucket
	    .async()
	    .get(docid)
	    .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
	        public Observable<JsonDocument> call(final JsonDocument loaded) {
	        	/*
	        	 * accept parameters in hashmap format to update the values
	        	 */
	        	for (Map.Entry<Object, Object> entry : parameters.entrySet())
	        	{
	        		   loaded.content().put((String) entry.getKey(),(Object)entry.getValue());	
	        		   // eg. loaded.content().put("age", 54);
	        	}
	      
	            return bucket.async().replace(loaded);
	        }
	    })
	    .toBlocking().single();
		
		return "doc_updated";
	}	
	
  
  
  public String replaceDocument(String docid, JsonObject jsonobj)
  {
    try
    {
      JsonDocument doc = JsonDocument.create(docid, jsonobj);
      this.bucket.upsert(doc);
      return "doc_replaced";
    }
    catch (Exception e)
    {
      log.error("Exception while replacing document : "+docid, e);
      return "exception";
    }
    
  }
  
   
  public String createDocument(String docid, JsonObject jsonobj)
  {
    JsonDocument doc = JsonDocument.create(docid, jsonobj);
    try
    {
      this.bucket.insert(doc);
      return "doc_created";
    }
    catch (DocumentAlreadyExistsException e)
    {
      return "doc_exist";
    }
    
  }
  
  public String createDocument(JsonObject jsonobj)
  {
    JsonDocument doc = JsonDocument.create(jsonobj.toString());
    try
    {
      this.bucket.upsert(doc);
      return "doc_created";
    }
    catch (DocumentAlreadyExistsException e)
    {
      return "doc_exist";
    }
    
  }
  
  /* insert documents asynchronously */
  public String createAsyncDocument(String docid, JsonObject jsonobj)
  {
	try
	{
	List<JsonDocument> documents = new ArrayList<JsonDocument>();
	documents.add(JsonDocument.create(docid, jsonobj));
	
    Observable
    .from(documents)
    .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
        @Override
        public Observable<JsonDocument> call(final JsonDocument docToInsert) {
            return bucket.async().insert(docToInsert);
        }
    })
   .cache()
   .last()
   .toBlocking()
   .single();
    
    return "doc_created";
    
	}
	
  catch (Exception e)
    {
	  log.error("Async Document Creation failed : ", e);
	  return "exception";
    }
	
    
  }
  
  /* replace documents asynchronously */
  public String replaceAsyncDocument(String docid, JsonObject jsonobj)
  {
	try
	{
	List<JsonDocument> documents = new ArrayList<JsonDocument>();
	documents.add(JsonDocument.create(docid, jsonobj));
	
    Observable
    .from(documents)
    .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
        @Override
        public Observable<JsonDocument> call(final JsonDocument docToInsert) {
            return bucket.async().upsert(docToInsert);
        }
    })
   .cache()
   .last()
   .toBlocking()
   .single();
    
    return "doc_replaced";
    
	}
	
  catch (Exception e)
    {
	  log.error("unable to replace document asynchronously : "+docid, e);
      return "exception";
    }
	
    
  }
  
  public String bulkAsyncDocument(List<JsonDocument> documents)
  {
		try
		{
			//List<JsonDocument> documents = new ArrayList<JsonDocument>();
			//documents.add(JsonDocument.create(docid, jsonobj));
			
		    Observable
		    .from(documents)
		    .flatMap(new Func1<JsonDocument, Observable<JsonDocument>>() {
		        @Override
		        public Observable<JsonDocument> call(final JsonDocument docToInsert) {
		            return bucket.async().upsert(docToInsert);
		        }
		    })
		   .cache()
		   .last()
		   .toBlocking()
		   .single();
		    
		    return "doc_created";
	    
		}
		
	  catch (Exception e)
	    {
			  log.error("Exception while inserting bulk documents : ",e);
		      return "exception";
	    }
	
    
  }
  
  public JsonDocument getDocBYId(String docid)
  {
    
	JsonDocument jsondoc = null;
    
    try
    {
    	jsondoc = bucket.get(docid);
    	
    	//jsondoc = bucket.getFromReplica(docid, ReplicaMode.ALL).get(0);
    	//jsondoc = this.bucket.getFromReplica(docid, ReplicaMode.ALL).parallelStream().findAny().get();
    }
   
    catch (Exception e)
    {
      log.error("unable to fetch document : "+docid);
      
     /* try
      {
    	 List<JsonDocument> docList = bucket.getFromReplica(docid, ReplicaMode.FIRST);
    	 if(!docList.isEmpty())
    	 {
    		 jsondoc = docList.get(0);
    		 log.info("Replica document fetch successful");
    	 }
    	 else
    	 {
    		 log.error("Replica document list is empty");
    		 jsondoc = null;
    	 }
    	 
      }
      catch(Exception e1)
      {
    	  jsondoc = null;
    	  log.error("document fetch from replica failed : ",e1);
      }*/
      
    }
    
    return jsondoc;
    
  }
  
  public String removeDocument(String docid)
  {
    try
    {
      this.bucket.remove(docid);
      return "doc_deleted";
    }
    catch (DocumentDoesNotExistException e)
    {
      log.error("Document Not Found :"+docid);
      return "doc_notexist";
    }
    
  }
  
  public long updateSequence(String sequenceName) {
	  
	  try {
		  
		  synchronized (this)
		    {
		      JsonObject seqObject = (JsonObject)getDocBYId("sequence").content();
		      long seq = seqObject.getLong(sequenceName).longValue();
		      seqObject.put(sequenceName, (seq + 1));
		      String docStatus = replaceDocument("sequence", seqObject);
		      if (docStatus.equals("doc_replaced"))
		      {
		    	  log.info(sequenceName + " sequence updated :" + (seq + 1));
		    	  return seq;
		      }
		      else
		      {
		    	  seq = -1;
		    	  log.error("unable to update sequence document");
		    	  return seq;
		      }
		      
		    }  
		  
	  }
	  catch(Exception e)
	  {
		  log.error("Exception at updateSequence: ", e);
		  return -1;
	  }
	  
    
  }
  
//separate document for each sequence.
 public long updateDBSequence(String sequenceName) {
	  
	  try {
		    synchronized (this)
		    {
		      JsonObject seqObject = (JsonObject)getDocBYId(sequenceName).content();
		      long seq = seqObject.getLong("value").longValue();
		      seqObject.put("value", (seq + 1));
		      String docStatus = replaceDocument(sequenceName, seqObject);
		      if (docStatus.equals("doc_replaced"))
		      {
		    	  log.debug(sequenceName + " sequence updated : " + (seq + 1));
		    	  return seq;
		      }
		      else
		      {
		    	  seq = -1;
		    	  log.error("unable to update sequence document : "+sequenceName);
		    	  return seq;
		      }
		      
		    }
	  	}
	  catch(Exception e)
	  {
		  log.error("Exception at updateDBSequence : ", e);
		  return -1;
	  }
	}
  
  public List<Map<String, Object>> executeQuery(String statement)
  {
    List<Map<String, Object>> content = null;
    N1qlQueryResult result=null;
    
    try
    {

      N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false);
//      N1qlQueryExecutor executor = new N1qlQueryExecutor(core, "ProductData", "");
      N1qlQuery query = N1qlQuery.simple(statement,param);
      result = this.bucket.query(query);
      
      content = new ArrayList<Map<String, Object>>();
      if (result.finalSuccess())
      {
        for (N1qlQueryRow row : result) {
          content.add(row.value().toMap());
        }
      }
      else
      {
        content = null;
        log.error("Query returned with errors:" + result.errors());
        System.out.println("Query returned with errors:" + result.errors());
      }
    }
    catch (Exception e)
    {
      content = null;
      log.error("Exception occurred while executing query ",e);
      System.out.println("Failed Query :  "+statement);
      e.printStackTrace();
      
    }
    return content;
  }
  
  
  public List<Map<String, Object>> executeParamQuery(String statement,JsonObject obj)
  {
    List<Map<String, Object>> content = null;
    N1qlQueryResult result=null;
    
    try
    {

      //N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);
      N1qlQuery query = N1qlQuery.parameterized(statement,obj, n1qlparam);
      result = this.bucket.query(query, APIConstants.TIMEOUT_VALUE, TimeUnit.SECONDS);
      content = new ArrayList<Map<String, Object>>();
      
      if (result.finalSuccess())
      {
        for (N1qlQueryRow row : result) {
          content.add(row.value().toMap());
        }
      }
      else
      {
        content = null;
        log.error("Query returned with errors:" + result.errors());
      }
    }
    catch (Exception e)
    {
      content = null;
      log.error("Exception occurred while executing query ",e);
      
    }
    return content;
  }
  
  public List<Map<String, Object>> executeParamArrQuery(String statement,JsonArray arr)
  {
    List<Map<String, Object>> content = null;
    N1qlQueryResult result=null;
    
    try
    {

      //N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);
      N1qlQuery query = N1qlQuery.parameterized(statement,arr, n1qlparam);
      result = this.bucket.query(query, APIConstants.TIMEOUT_VALUE, TimeUnit.SECONDS);
      content = new ArrayList<Map<String, Object>>();
      
      if (result.finalSuccess())
      {
        for (N1qlQueryRow row : result) {
          content.add(row.value().toMap());
        }
      }
      else
      {
        content = null;
        log.error("Query returned with errors:" + result.errors());
      }
    }
    catch (Exception e)
    {
      content = null;
      log.error("Exception occurred while executing query ");
      
    }
    return content;
  }
  
  public List<Map<String, Object>> executeQueryCouchDB(String statement)
  {
    List<Map<String, Object>> content = null;
    N1qlQueryResult result=null;
    
    try
    {
	      //N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);	
	      N1qlQuery query = N1qlQuery.simple(statement,param);
	      result = this.bucket.query(query);
      
	      content = new ArrayList<Map<String, Object>>();
	      if (result.finalSuccess())
	      {
	    	  for (N1qlQueryRow row : result) {
	    		  content.add(row.value().toMap());
	    	  }
	      }
	      else
	      {
	        content = null;
	        log.error("Query returned with errors:" + result.errors());
	      }
    }
    catch (Exception e)
    {
    		content = null;
    		log.error("Exception occurred while executing query ",e);
      
    }
    return content;
  }
  
  public List<ObjectNode> executeQueryCouchDB1(String statement)
  {
    List<ObjectNode> listNode = new ArrayList<ObjectNode>();
    try
    {
      N1qlQuery query = N1qlQuery.simple(statement);
      query.params().consistency(ScanConsistency.NOT_BOUNDED);
      
      N1qlQueryResult result = this.bucket.query(query);
      if (result.finalSuccess())
      {
        for (N1qlQueryRow row : result)
        {
          ObjectNode objectNode = this.objectMapper.createObjectNode();
          objectNode.putObject(row.value().toString());
          
          listNode.add(objectNode);
        }
      }
      else
      {
        listNode = null;
        log.error("Query returned with errors:" + result.errors());
      }
    }
    catch (Exception e)
    {
      listNode = null;
      log.error("Exception occurred while executing query : ",e);
    }
    return listNode;
  }
  
  public List<JsonObject> executeConfigQuery(String statement, String display)
  {
    List<JsonObject> content = null;
    N1qlQueryResult result = null;
    		
    try
    {
	      //N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);	
	      N1qlQuery query = N1qlQuery.simple(statement,param);
	      result = this.bucket.query(query);
	      content = new ArrayList<JsonObject>();
	      if (result.finalSuccess())
	      {
	        for (N1qlQueryRow row : result) {
	          content.add(row.value());
	        }
	      }
	      else
	      {
	        content = null;
	        log.error("Query returned with errors:" + result.errors());
	      }
	      
    }
    catch (Exception e)
    {
	      content = null;
	      log.error("Exception occurred while executing query ",e);
	      log.error("Query returned with errors:" + result.errors());
    }
    return content;
  }
  
  public List<JsonObject> executeConfigParamQuery(String statement,JsonObject obj)
  {
    List<JsonObject> content = null;
    N1qlQueryResult result = null;
    		
    try
    {
        //N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);
        N1qlQuery query = N1qlQuery.parameterized(statement,obj, n1qlparam);
        result = this.bucket.query(query, APIConstants.TIMEOUT_VALUE, TimeUnit.SECONDS);
        content = new ArrayList<JsonObject>();
      
        if (result.finalSuccess())
        {
        	for (N1qlQueryRow row : result) {
        		content.add(row.value());
        	}
        }
        else
        {
        	content = null;
        	log.error("Query returned with errors:" + result.errors());
        }
        
    }
    catch (Exception e)
    {
      content = null;
      log.error("Exception occurred while executing query ",e);
      log.error("Query returned with errors:" + result.errors());
    }
    return content;
  }
  
  
  public List<JsonObject> executeConfigParamArrQuery(String statement,JsonArray arr)
  {
    List<JsonObject> content = null;
    N1qlQueryResult result = null;
    		
    try
    {
       // N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);
        N1qlQuery query = N1qlQuery.parameterized(statement,arr, n1qlparam);
        result = this.bucket.query(query, APIConstants.TIMEOUT_VALUE, TimeUnit.SECONDS);
        content = new ArrayList<JsonObject>();
      
        if (result.finalSuccess())
        {
        	for (N1qlQueryRow row : result) {
        		content.add(row.value());
        	}
        }
        else
        {
        	content = null;
        	log.error("Query returned with errors:" + result.errors());
        }
        
    }
    catch (Exception e)
    {
      content = null;
      log.error("Exception occurred while executing query ",e);
      log.error("Query returned with errors:" + result.errors());
    }
    return content;
  }
  
  public List<JsonObject> executeConsistentConfigParamArrQuery(String statement,JsonArray arr)
  {
    List<JsonObject> content = null;
    N1qlQueryResult result = null;
    		
    try
    {
       // N1qlParams param = N1qlParams.build().consistency(ScanConsistency.NOT_BOUNDED).adhoc(false).maxParallelism(CouchConstants.MAX_PARALLEL_QUERY);
        N1qlQuery query = N1qlQuery.parameterized(statement,arr, n1qlparamBounded);
        result = this.bucket.query(query, APIConstants.TIMEOUT_VALUE, TimeUnit.SECONDS);
        content = new ArrayList<JsonObject>();
      
        if (result.finalSuccess())
        {
        	for (N1qlQueryRow row : result) {
        		content.add(row.value());
        	}
        }
        else
        {
        	content = null;
        	log.error("Query returned with errors:" + result.errors());
        }
        
    }
    catch (Exception e)
    {
      content = null;
      log.error("Exception occurred while executing query ",e);
      log.error("Query returned with errors:" + result.errors());
    }
    return content;
  }
  
  public void createPrimaryIndex(String bucket)
  {
    Index.createPrimaryIndex().on(bucket);
    Index.buildIndex().on(bucket);
  }
  
  public List<Object> fetchConfigView(String design_doc, String viewname, String key, int limit, String field)
  {
    ViewQuery citylist = ViewQuery.from(design_doc, viewname);
    //citylist.stale(Stale.FALSE);
    citylist.stale(Stale.TRUE);
    citylist.startKey(key);
    citylist.endKey(key + "?");
    citylist.limit(limit);
    
    ViewResult result = this.bucket.query(citylist, APIConstants.VIEW_TIMEOUT_VALUE, TimeUnit.SECONDS);
    List<Object> content = new ArrayList<Object>();
    for (ViewRow row : result) {
    	content.add(row.document().content().getString(field));
      //content.add(((JsonObject)row.document().content()).getString(field));
    }
    return content;
  }
  
  public List<ObjectNode> fetchServerConfigkeyStringView(String viewname, String key,JsonArray param)
  {
    List<ObjectNode> listNode = new ArrayList<ObjectNode>();
    
    try
    {
      ViewQuery data = ViewQuery.from(APIConstants.DESIGN_DOC, viewname);
      data.stale(Stale.TRUE);
      data.inclusiveEnd(true);
      //data.key(param);
      //data.reduce();
      //data.descending();
      data.limit(10);
      
      JsonArray arr = JsonArray.create();
	  JsonObject obj = JsonObject.create();
	  //JsonArray arrobj = JsonArray.create();
       
    	 
    	arr.add(param);
    	arr.add(obj);
        data.startKey(param);
        data.endKey(arr);
        
        
    
      ViewResult result = this.bucket.query(data, APIConstants.VIEW_TIMEOUT_VALUE, TimeUnit.SECONDS);
     
      for (ViewRow row : result)
      {
        ObjectNode objectNode = this.objectMapper.createObjectNode();
        objectNode.put("key", row.key().toString());
        objectNode.put("value", row.value().toString());
        listNode.add(objectNode);
      }
      System.out.println("setting up keys : "+param);
     // System.out.println("setting up end keys : "+arr);
      System.out.println("view results : "+listNode);
      System.out.println("size : "+result.allRows().size());
     
    }
    catch (ViewDoesNotExistException e)
    {
      log.error("view does not exist : " + viewname);
      System.out.println("cvxvzxfgsdgs");
    }
    catch (Exception e)
    {
      log.error("Exception while executing view : " + viewname);
      System.out.println("cvxvzx");
    }
    return listNode;
  }
  
  public List<ObjectNode> fetchServerConfigkeyIntView(String viewname, int key)
  {
    List<ObjectNode> listNode = new ArrayList<ObjectNode>();
    
    try
    {
      ViewQuery data = ViewQuery.from(APIConstants.DESIGN_DOC, viewname);
      //data.stale(Stale.FALSE);
      data.stale(Stale.TRUE);
      if (key != -1)
      {
        data.startKey(key);
        data.endKey(key + "?");
      }
      ViewResult result = this.bucket.query(data, APIConstants.VIEW_TIMEOUT_VALUE, TimeUnit.SECONDS);
      for (ViewRow row : result)
      {
        ObjectNode objectNode = this.objectMapper.createObjectNode();
        objectNode.put("key", (Integer)row.key());
        objectNode.put("value", row.value().toString());
        listNode.add(objectNode);
      }
    }
    catch (ViewDoesNotExistException e)
    {
      log.error("view does not exist : " + viewname);
    }
    catch (Exception e)
    {
      log.error("Exception while executing view : " + viewname);
    }
    return listNode;
  }
  
  public List<JsonDocument> getBulkDocumentList(final Collection<String> ids) {
      return Observable
          .from(ids)
          .flatMap(new Func1<String, Observable<JsonDocument>>() {
              @Override
              public Observable<JsonDocument> call(String id) {
                  return bucket.async().get(id);
              }
          })
          .toList()
          .toBlocking()
          .single();
  }
  
  public static void main(String[] a)
    throws Exception
  {
    try
    {
    	
    	Database config = new Database();
        
        config.setBucket("ServerConfig");
        config.setPassword("");
        
        CBService cb = new CBService(config);
        
       /* JsonArray param = JsonArray.create();
        param.add("TN11");
        param.add("TN11");
        cb.fetchServerConfigkeyStringView("RTOList", "",param);*/
        for(int i=1;i<=10;i++)
        {
        	long lStartTime = System.currentTimeMillis();
        	 System.out.println("DocumentIDConfig : "+cb.getDocBYId("DocumentIDConfig"));
             long endTime = System.currentTimeMillis();
             System.out.println("elapsed time : "+(endTime-lStartTime));
        }
       
      
          
        
        
    
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
