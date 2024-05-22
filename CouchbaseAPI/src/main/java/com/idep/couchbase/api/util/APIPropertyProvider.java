package com.idep.couchbase.api.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * 
 * @author sandeep.jadhav
 *
 */
public class APIPropertyProvider {
	
	private static Properties nodeconfig = new Properties();
	static Logger log = Logger.getLogger(APIPropertyProvider.class.getName());
	 static
	  {
		    try
		    {
				  InputStream inputStream = new FileInputStream(System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
				  log.info("Couchbase Configuration file path : "+System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
				  nodeconfig.load(inputStream);
		    }
		    catch (FileNotFoundException e)
		    {
		    	log.error("Couchbase Configuration file not found");
		    }
		    catch (NullPointerException e)
		    {
		    	log.error("set COUCHBASE_CLUSTER_CONFIG system variable to read properties file");
		    }
		    catch (IOException e)
		    {
		    	log.error("unable to load Couchbase Configuration properties file : ",e);
		    }
		    catch (Exception e)
		    {
		    	log.error("unable to load Couchbase Configuration properties file : ",e);
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
	 

}
