
package com.idep.service.vehicleinfo.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;


public class VehicleInfoRequest implements Processor {
	Logger log = Logger.getLogger(VehicleInfoRequest.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService service = CBInstanceProvider.getServerConfigInstance();
	JsonNode vehicleRtoConfigNode = null;
	SoapConnector soapconnector = new SoapConnector();
	@Override
	public void process(Exchange exchange) throws Exception {

		try{
		String xmlReq = exchange.getIn().getBody(String.class);
		String vehicleInfo = exchange.getProperty("vehicleInpReq").toString();
		String soapRequest=null;
			JsonDocument vehicleReqConfigDoc = service.getDocBYId(VehicleInfoConstant.VEHICLEREQCONFIGREQDOC);  
			if(vehicleReqConfigDoc!=null){
				
				JsonNode  vehicleReqConfigDocNode = objectMapper.readTree(vehicleReqConfigDoc.content().toString());
				/**
				 * loading XML request configuration
				 * */
				if(vehicleReqConfigDocNode.has("carrierSOAPConfig")){
					 JsonNode carrierSOAPConfig = vehicleReqConfigDocNode.get("carrierSOAPConfig");
					 HashMap<String,String> replacevalmap = objectMapper.readValue(carrierSOAPConfig.get("reqConfig").get("removeAttrList").toString(), HashMap.class);
					 /**
						 * Preparing soap request body with 2 field
						 * 1) RegistrationNumber
						 * 2) username
						 * */
					 if(carrierSOAPConfig.get("reqConfig").has("methodName") && carrierSOAPConfig.get("reqConfig").has("schemaLocation")){
						 JsonNode schemaLocMap = carrierSOAPConfig.get("reqConfig").get("schemaLocMap");
						  log.info("schemaLocMap : "+schemaLocMap);
						  @SuppressWarnings("unchecked")
						  Map<String,String> schemaMap = objectMapper.readValue(schemaLocMap.toString(), Map.class); 
						 Map<String, Object> inputParamMap = new HashMap<String,Object>();
						    log.info("XML SoapRequest input : "+xmlReq);
						    inputParamMap.put(carrierSOAPConfig.findValue("methodParam").asText(), xmlReq);
						    log.info("Calling SoapRequest : "+vehicleInfo.toString());
						    soapRequest  = soapconnector.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), carrierSOAPConfig.findValue("methodParam").asText(), xmlReq, schemaMap);
							}else{
								Map<String, Object> inputParamMap = new HashMap<String,Object>();
							    inputParamMap.put(carrierSOAPConfig.findValue("methodParam").asText(), xmlReq);
							    soapRequest = soapconnector.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), "", inputParamMap );
							}
					log.info("VehicleInfoRequest Genrated SOAP Request : "+soapRequest);
					if(soapRequest!=null){
						soapRequest=soapRequest.replaceAll("<o>", "").replaceAll("</o>", "").replaceAll("<clientMethod:xmlreq>", "").replaceAll("</clientMethod:xmlreq>", "");
					}
				}
				if(vehicleReqConfigDocNode.has("URL")){
					exchange.getIn().setHeader("requestURL", vehicleReqConfigDocNode.get("URL").asText());
				}
				exchange.getIn().setBody(soapRequest);
			}else{
				log.error("Unable to load Vehicle Request Config document, DocumentID : "+VehicleInfoConstant.VEHICLEREQCONFIGDOCUMENT);	
			}
		
		}catch(Exception e){
			ObjectNode responseNode = objectMapper.createObjectNode();
			exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1001);
			  responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, VehicleInfoConstant.VEHICLE_RES_FAILED_CODE);
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, "failure");
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA, exchange.getIn().getBody(String.class));
				exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
			log.error("Unable to create request for get Vehicle Registration Details : ",e);
		}
	}
}
