package com.idep.couchbase.api.impl;

import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.idep.couchbase.api.util.APIPropertyProvider;

public class CBClusterConfiguration
{
  private static Logger log = Logger.getLogger(CBClusterConfiguration.class.getName());
  private static Cluster cluster = null;
  private static int DEFAULT_MEM_CACHED_PORT=11210;
  private static int DEFAULT_ADMIN_PORT=8091;
  private static String DEFAULT_MEM_CACHED_PORT_KEY="MEMCACHEDPORT";
  private static String DEFAULT_ADMIN_PORT_KEY="DEFAULTADMINPORT";
  
  static{
	  
	  String memCachedPort = APIPropertyProvider.getProperty(DEFAULT_MEM_CACHED_PORT_KEY);
	  
	  if(memCachedPort != null  && !memCachedPort.trim().equals(""))
	  {
		  DEFAULT_MEM_CACHED_PORT=Integer.parseInt(memCachedPort);
	  }
	  String defaultAdmin = APIPropertyProvider.getProperty(DEFAULT_ADMIN_PORT_KEY);
	  if(defaultAdmin != null  && !defaultAdmin.trim().equals(""))
	  {
		  DEFAULT_ADMIN_PORT=Integer.parseInt(defaultAdmin);
	  }
  		try
	    {
			log.info("Couchbase Environment Building Process Initiated"); //https://developer.couchbase.com/documentation/server/4.0/sdks/java-2.2/env-config.html
	        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
	        .bootstrapCarrierDirectPort(DEFAULT_MEM_CACHED_PORT)
	        .bootstrapHttpDirectPort(DEFAULT_ADMIN_PORT)
	        .build();
	        List<String> SEED_IPS = Arrays.asList(APIPropertyProvider.getProperty("node"));
	        cluster = CouchbaseCluster.create(env, SEED_IPS);

	        log.info("Couchbase Environment Building Process Completed");
	      
	    }
	    catch (Exception e)
	    {
	      log.error("Failed to set up couchbase environment : ",e);
	    }
  }
  public static Cluster getClusterEnv()
  {
	   return cluster;
    
  }
}
