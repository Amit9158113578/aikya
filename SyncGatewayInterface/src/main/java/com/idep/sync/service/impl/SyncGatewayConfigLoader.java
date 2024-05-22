package com.idep.sync.service.impl;

import com.couchbase.client.java.document.json.JsonObject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;

public class SyncGatewayConfigLoader {
  static String syncTransAdminURL = null;
  
  static String syncTransPublicURL = null;
  
  static String syncConfigAdminURL = null;
  
  static String syncConfigPublicURL = null;
  
  static JsonObject syncViewConfigNode = null;
  
  static String syncAdminBaseURL = null;
  
  static String syncPublicBaseURL = null;
  
  static String syncPospDataAdminURL = null;
  
  static String syncPospDataPublicURL = null;
  
  private static Properties nodeconfig = new Properties();
  
  private static Logger log = Logger.getLogger(SyncGatewayConfigLoader.class.getName());
  
  static {
    try {
      InputStream inputStream = new FileInputStream(System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
      log.info("Couchbase Configuration file path : " + System.getProperty("COUCHBASE_CLUSTER_CONFIG"));
      nodeconfig.load(inputStream);
      syncAdminBaseURL = nodeconfig.getProperty("syncGatewayAdminBaseURL");
      syncPublicBaseURL = nodeconfig.getProperty("syncGatewayPublicBaseURL");
      syncTransAdminURL = String.valueOf(syncAdminBaseURL) + nodeconfig.getProperty("syncPolicyTransDBName") + "/";
      syncTransPublicURL = String.valueOf(syncPublicBaseURL) + nodeconfig.getProperty("syncPolicyTransDBName") + "/";
      syncConfigAdminURL = String.valueOf(syncAdminBaseURL) + nodeconfig.getProperty("syncServerConfigDBName") + "/";
      syncConfigPublicURL = String.valueOf(syncPublicBaseURL) + nodeconfig.getProperty("syncServerConfigDBName") + "/";
      syncPospDataAdminURL = String.valueOf(syncAdminBaseURL) + nodeconfig.getProperty("syncPospDataDBName") + "/";
      syncPospDataPublicURL = String.valueOf(syncPublicBaseURL) + nodeconfig.getProperty("syncPospDataDBName") + "/";
    } catch (FileNotFoundException e) {
      log.error("Couchbase Configuration file not found");
      e.printStackTrace();
    } catch (NullPointerException e) {
      log.error("set COUCHBASE_CLUSTER_CONFIG system variable to read properties file");
      e.printStackTrace();
    } catch (IOException e) {
      log.error("unable to load Couchbase Configuration properties file : ", e);
      e.printStackTrace();
    } catch (Exception e) {
      log.error("unable to load Couchbase Configuration properties file : ", e);
      e.printStackTrace();
    } 
  }
  
  public static String getProperty(String key) {
    if (key != null) {
      String value = nodeconfig.getProperty(key.trim());
      if (value != null)
        return value.trim(); 
      return null;
    } 
    return null;
  }
}
