package com.idep.services.util;

import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/**
 * @author vipin.patil
 *
 * May 10, 2017
 */
public class ECMSManager {
	
	Logger log = Logger.getLogger(ECMSManager.class.getName());
	CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	static JsonObject configData=null;
	
	public String getRestURL(String requestKey)
	{
		configData = getConfigDetails();
		if(configData == null)
		{
			log.info("get config Details");
			configData = getConfigDetails();
		}
		String url = configData.get(requestKey).toString(); 
		return url;
	}
	public JsonObject getConfigDetails()
	{
		JsonDocument jsonDocument = serverConfig.getDocBYId(ECMSConstants.ALFRESCO_CONFIG_DOC_NAME);
		 if(jsonDocument != null)
		 {
			 JsonObject jsonObject = jsonDocument.content();
		     return jsonObject;
		 }
		 else
		 {
			 return null;
		 }
	}

	public static void main(String[] args) {
		ECMSManager manager = new ECMSManager();
		manager.getConfigDetails();
	}
}
