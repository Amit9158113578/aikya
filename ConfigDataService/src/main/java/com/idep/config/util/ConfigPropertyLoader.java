package com.idep.config.util;

/*import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.log4j.Logger;*/

public class ConfigPropertyLoader
{
  /*private static final String DATA_CONFIG_FILE = "data-config.properties";
  static Logger log = Logger.getLogger(ConfigPropertyLoader.class.getName());
  private static Properties dataview = new Properties();
  
  static
  {
    InputStream inputStream = ConfigPropertyLoader.class.getClassLoader()
      .getResourceAsStream(DATA_CONFIG_FILE);
    try
    {
      log.info("AppConfig finder service properties file loading started");
      dataview.load(inputStream);
      log.info("AppConfig finder service properties file loading completed");
    }
    catch (NullPointerException e)
    {
      log.error("AppConfig finder Service properties file not found....");
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      log.error("unable to read AppConfig properties file due to IOException");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      log.error("Exception occurred while loading AppConfig properties, Analyze logs for more details");
    }
  }
  
  public static String getProperty(String key)
  {
    if (key != null)
    {
      String value = dataview.getProperty(key.trim());
      if (value != null) {
        return value.trim();
      }
    }
    return null;
  }*/
}
