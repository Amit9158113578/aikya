package com.idep.couchbase.api.impl;

import java.util.HashMap;
import org.apache.log4j.Logger;
import com.idep.couchbase.api.util.APIConstants;
import com.idep.couchbase.api.util.APIPropertyProvider;
import com.idep.couchbase.api.util.Database;

/**
 * 
 * @author sandeep.jadhav
 * couchbase instance provider
 */
public class CBInstanceProvider
{
	private static HashMap<String,CBService> bucketInstance = new HashMap<String,CBService>();
	private static Logger log = Logger.getLogger(CBInstanceProvider.class.getName());
	
	static
	  {
			String bucketList = APIPropertyProvider.getProperty(APIConstants.BUCKET_LIST);
			String bucketNames[] = bucketList.split(",");
			for(int i=0;i<bucketNames.length;i++)
			{
				try
				{
					Database dbConfig = new Database();
					dbConfig.setBucket(bucketNames[i]);
					dbConfig.setPassword(bucketNames[i].toString());
					bucketInstance.put(bucketNames[i], new CBService(dbConfig));
					log.info(bucketNames[i]+" Bucket Instance Created");
				}
				catch(Exception e)
				{
					log.error(bucketNames[i]+" Bucket Instance creation process failed : ",e);
				}
				
			}
			
			log.info("Bucket Instance creation process completed");
	  }

	public static CBService getDomainAssistInstance()
	{
		
		return bucketInstance.get(APIConstants.DOMAINASSIST_BUCKET);
	}

	public static CBService getPolicyTransInstance()
	{
		return bucketInstance.get(APIConstants.POLICYTRANS_BUCKET);
	}

	public static CBService getUserFeedInstance()
	{
		return bucketInstance.get(APIConstants.FEEDBACK_BUCKET);
	}

	public static CBService getAdminAppConfigInstance()
	{
		return bucketInstance.get(APIConstants.ADMINAPP_BUCKET);
	}

	public static CBService getServerConfigInstance()
	{
		return bucketInstance.get(APIConstants.SERVERCONFIG_BUCKET);
	}

	public static CBService getProductConfigInstance()
	{
		return bucketInstance.get(APIConstants.PRODUCTDATA_BUCKET);
	}

	public static CBService getAnalyticsDBInstance()
	{
		return bucketInstance.get(APIConstants.ANALYTICS_BUCKET);
	}

	public static CBService getMasterProductinstance()
	{
		return bucketInstance.get(APIConstants.MASTERPRODUCT_BUCKET);
	}

	public static CBService getMasterServerConfigInstance()
	{
		return bucketInstance.get(APIConstants.MASTERSERVER_BUCKET);
	}
	

	public static CBService getBucketInstance(String bucketName)
	{
		return bucketInstance.get(bucketName);
		
	}
	
}