package com.idep.sugarcrm.service.impl;

import org.apache.log4j.Logger;

import com.idep.sugarcrm.exception.processor.ExecutionTerminator;

public class SugarSession {
	
	static SugarCRMGatewayImpl sugarCRMService = SugarCRMGatewayImpl.getSugarCRMInstance();
	static Logger log = Logger.getLogger(SugarSession.class.getName());
	static String sessionId = "";
	
	static
	{
		try {
			getSession();
		} catch (ExecutionTerminator e) {
			log.info("Exception In Getting Session");
		}
		log.info("New Session Id :"+sessionId);
	}
	
	public static void getSession() throws ExecutionTerminator
	{
		sessionId = sugarCRMService.loginSugarCRM();
		
		
	}
}
