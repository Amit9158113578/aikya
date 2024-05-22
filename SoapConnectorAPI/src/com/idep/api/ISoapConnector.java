/**
 * 
 */
package com.idep.api;

import java.util.Map;
import java.util.HashMap;

/**
 * @author vipin.patil
 *
 */
public interface ISoapConnector {
	String prepareSoapRequest (String methodName,String schemaLocation,Map<String,Object> inputParamMap);
	String getSoapResult(String soapResponse,String tagName);
	String getSoapResult(String s,String startTagName, String endTagName, String strArray[], String apppendTag);
	String createCDATARequest(String request);
	String createCDATARequest(String request,HashMap<String,String> inputParamMap);
	String CreateAttributeRequest(String request);
}
