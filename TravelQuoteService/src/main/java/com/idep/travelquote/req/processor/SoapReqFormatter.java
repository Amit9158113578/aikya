package com.idep.travelquote.req.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.travelquote.util.TravelQuoteConstants;
import com.idep.api.impl.SoapConnector;

public class SoapReqFormatter implements Processor{
	Logger log = Logger.getLogger(SoapReqFormatter.class.getName());
	  CBService serverConfig = CBInstanceProvider.getServerConfigInstance();
	  ObjectMapper objectMapper = new ObjectMapper();
	
	  SoapConnector  soapService = new SoapConnector();
	  
	  @Override
	public void process(Exchange exchange) throws Exception {
		try{
			String soapRequest=null;
			  String request  = exchange.getIn().getBody(String.class);
			 
			 request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
			  request = request.replace("<o>", "");
			  request = request.replace("</o>", "");
			  request = request.replaceAll("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
			  request = request.replaceAll("<o>", "");
			  request = request.replaceAll("</o>", "");
			  /*
		      Map<String, String> tnsMap =  new HashMap<String,String>();
			  tnsMap.put("policy", "http://intf.insurance.symbiosys.c2lbiz.com/xsd");
			  tnsMap.put("parentTns", "http://relinterface.insurance.symbiosys.c2lbiz.com");
			  */
	
			  String configdoc=exchange.getProperty(TravelQuoteConstants.CARRIER_REQ_MAP_CONF).toString();
				 JsonNode Carrierconfigdoc = this.objectMapper.readTree(configdoc);
				 log.debug("Carrier Config Document  :  "+Carrierconfigdoc);
				 
				 if(Carrierconfigdoc.has("carrierSOAPConfig")){
				 JsonNode carrierSOAPConfig = Carrierconfigdoc.get("carrierSOAPConfig");
				 HashMap<String,String> replacevalmap = objectMapper.readValue(carrierSOAPConfig.get("reqConfig").get("removeAttrList").toString(), HashMap.class);
				 log.debug("MAP HelProposalAttribute Request  : "+replacevalmap.toString());
				 
				 /**
				  * Code added to expand properties from XML request
				  * expandablePropertiesList specified in Carrierconfigdoc
				  */
				 	/**
				 	 * carrier required Attribute inside tag then below if verified and convert into tag Attribute 
				 	 * Attribute before @ sign required  (eg. TravelProposalRequest-28-1-sample) 
				 	 **/
				 	if(carrierSOAPConfig.has("isAttributeRequired")){
				 		if(carrierSOAPConfig.findValue("isAttributeRequired").asText().equals("Y")){
				 			request = soapService.CreateAttributeRequest(request);
				 		}
				 	}
				 	/**
				 	 * carrier required request in !<[CDATA ... ]> format.  
				 	 *  
				 	 **/
				 	if(carrierSOAPConfig.has("isCDATArequired") && carrierSOAPConfig.findValue("isCDATArequired").asText().equals("Y")){
				 		request=soapService.createCDATARequest(request,replacevalmap);
				 	}
					/** 
					 * Passing dynamic Values to preparesoapRequest
					 * MethodName = parentTag name
					 * SchemaLocation = carrier schema url  if in case schema location not available then pass "" 
					 * inputParamMap = Child Tag
					 * */
					if(carrierSOAPConfig.get("reqConfig").has("methodName") && carrierSOAPConfig.get("reqConfig").has("schemaLocation")){
				    Map<String, Object> inputParamMap = new HashMap<String,Object>();
				    inputParamMap.put(carrierSOAPConfig.findValue("methodParam").asText(), request);
				    soapRequest = soapService.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), carrierSOAPConfig.findValue("schemaLocation").asText(), inputParamMap );
					}else{
						Map<String, Object> inputParamMap = new HashMap<String,Object>();
					    inputParamMap.put(carrierSOAPConfig.findValue("methodParam").asText(), request);
					    soapRequest = soapService.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), "", inputParamMap );
					}
				 }
				 log.debug(" Carrier Quote XML Request  : "+soapRequest);
					exchange.getIn().setBody(soapRequest);
				
			
		}catch(Exception e){
		log.error("error while preparing soap request : ",e);	
		}
		
	}

}
