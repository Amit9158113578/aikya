package com.idep.leadutility.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class ICRMDBConnection {
	private static Properties nodeconfig = new Properties();
	Logger log = Logger.getLogger(ICRMDBConnection.class.getName());
	
	public Connection getCRMDBConnection() throws Exception
	{
		log.info("In connectiona class");
		Connection connection = null;
		try
		{
			InputStream inputStream = new FileInputStream(System.getProperty("MYSQL_CONN_PROPS"));
			log.info("mysql Configuration file path : " + System.getProperty("MYSQL_CONN_PROPS"));
			nodeconfig.load(inputStream);
	        String connectionURL = nodeconfig.getProperty("mysql.crmConnectionUrl");
			String driverName=nodeconfig.getProperty("mysql.driverName"); 
			String userName=nodeconfig.getProperty("mysql.userName"); 
			String password=nodeconfig.getProperty("mysql.crmPassword"); 
			try{
				Class.forName(driverName).newInstance();
			}
			catch(ClassNotFoundException e)
			{
				log.error("MySql driver not found...",e);
			}
			inputStream.close();
			connection = (Connection) DriverManager.getConnection(connectionURL,userName,password);
			log.info("Connection Created With CRM database...!!!");
		} catch (Exception e)
		{
			log.error("Error in Creating CRM DB Connection...!!",e);
		}
		
		return connection;	
	}

}
