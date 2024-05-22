package com.idep.url.reroute.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

public class SyncGatewayURLLoader {
	
	public static String syncGateTransAdminURL = null;
	public static String syncGateAdminBaseURL = null;
	public static String syncGatePublicBaseURL = null;
	public static String syncGateTransPublicURL = null;
	public static String syncGateConfigAdminURL = null;
	public static String syncGateConfigPublicURL = null;
	
	
	private static Properties nodeconfig = new Properties();
	static Logger log = Logger.getLogger(SyncGatewayURLLoader.class.getName());
	 static
	  {
		    try
		    {
				  InputStream inputStream = new FileInputStream(System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
				  log.info("Couchbase Configuration file path : "+System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
				  nodeconfig.load(inputStream);
				  
				  syncGateAdminBaseURL = nodeconfig.getProperty("syncGatewayAdminBaseURL");
				  syncGatePublicBaseURL = nodeconfig.getProperty("syncGatewayPublicBaseURL");
				  syncGateTransAdminURL = syncGateAdminBaseURL+nodeconfig.getProperty("syncPolicyTransDBName");
				  syncGateTransPublicURL = syncGatePublicBaseURL+nodeconfig.getProperty("syncPolicyTransDBName");
				  syncGateConfigAdminURL = syncGateAdminBaseURL+nodeconfig.getProperty("syncServerConfigDBName");	
				  syncGateConfigPublicURL = syncGatePublicBaseURL+nodeconfig.getProperty("syncServerConfigDBName");
				  
		    }
		    catch (FileNotFoundException e)
		    {
		    	log.error("Couchbase Configuration file not found");
		    	e.printStackTrace();
		    }
		    catch (NullPointerException e)
		    {
		    	log.error("set COUCHBASE_CLUSTER_CONFIG system variable to read properties file");
		    	e.printStackTrace();
		    }
		    catch (IOException e)
		    {
		    	log.error("unable to load Couchbase Configuration properties file : ",e);
		    	e.printStackTrace();
		    }
		    catch (Exception e)
		    {
		    	log.error("unable to load Couchbase Configuration properties file : ",e);
		    	e.printStackTrace();
		    }
	    
	  }
	  
	  public static String getProperty(String key)
	  {
	    if (key != null)
	    {
	      String value = nodeconfig.getProperty(key.trim());
	      if (value != null) {
	        return value.trim();
	      }
	      else
	      {
	    	  return null;
	      }
	    }
	    else
	    {
	    	return null;
	    }
	    
	  }
	 
	public static void main(String[] args) {
		
		
		System.out.println("testing");
		
	}
	/*static
	{
		
		CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
		JsonDocument SyncURLConfigDocument = serverConfig.getDocBYId(APIConstants.SYNC_GATEWAY_URL_CONFIG);
		if(SyncURLConfigDocument != null)
		{
			JsonObject SyncURLConfigNode = SyncURLConfigDocument.content();
			log.info("Sync URL Configuration : "+SyncURLConfigNode);
			syncTransAdminURL = SyncURLConfigNode.getString("SyncGateTransAdminURL");
			syncTransPublicURL = SyncURLConfigNode.getString("SyncGateTransPublicURL");
			syncConfigAdminURL = SyncURLConfigNode.getString("SyncGateConfigAdminURL");	
			syncConfigPublicURL = SyncURLConfigNode.getString("SyncGateConfigPublicURL");
			syncViewConfigNode = SyncURLConfigNode.getObject("syncGateViewURL");
		}
		else
		{
			log.error(APIConstants.SYNC_GATEWAY_URL_CONFIG+" config document not found");
		}
		
	}
	*/

}
