package com.idep.summaryUtils;


import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.couchbase.api.util.Database;
import org.apache.log4j.Logger;

public class DBConnection
{
  public static Database config = new Database();
  static Logger log = Logger.getLogger(DBConnection.class.getName());
  public static CBService serverConfig = null;
  public static CBService PolicyTransaction = null;
  CBService docservice = new CBService(config);
  
  public static CBService getCouchBaseInstance()
  {
    try
    {
      if (serverConfig == null)
      {
        serverConfig = CBInstanceProvider.getServerConfigInstance();
        log.info("database instance created");
      }
      if (PolicyTransaction == null)
      {
    	  PolicyTransaction = CBInstanceProvider.getPolicyTransInstance();
        log.info("database instance created");
      }
    }
    catch (Throwable any)
    {
      any.printStackTrace();
      
      log.info("database instance not created");
    }
    return serverConfig;
  }
}
