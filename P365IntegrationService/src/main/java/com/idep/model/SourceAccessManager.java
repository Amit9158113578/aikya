package com.idep.model;
import org.apache.log4j.Logger;
import com.idep.dbaccess.SourceAccess;
import com.fasterxml.jackson.databind.JsonNode;

public class SourceAccessManager {

	public JsonNode validateSource(String deviceId) throws Exception
	{  
		JsonNode response=null;
	    Logger log = Logger.getLogger(SourceAccessManager.class.getName());
		try
		{   log.info("Inside validateSource()--SourceAccessManager");
			SourceAccess access = new SourceAccess();
			 response= access.validateSource(deviceId);	
			 log.info("response in SourceAccessmanager -- "+response);
		}
		catch(Exception e)
		{
			log.info("Error in SourceAccessManager!!");
			e.printStackTrace();
		}
		return  response;
	}
	
}
