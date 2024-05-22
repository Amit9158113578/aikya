package com.idep.policy.req.processor;

/**
 * 
 * @author pravin.jakhi
 * Written date 24-May-2017
 * 
 * */

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class HealthProposalAttributeReqProcessor implements Processor{

	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(HealthProposalAttributeReqProcessor.class.getName());
	SoapConnector soapconnector = new SoapConnector();
	
	public HealthProposalAttributeReqProcessor()  {
		
	}

	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {
		
		
		try{
			String soapRequest=null;
			 String request = exchange.getIn().getBody(String.class);
			 
			 String configdoc=exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG).toString();
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
			 if(carrierSOAPConfig.has("isexpandablePropertiesRequired") && carrierSOAPConfig.findValue("isexpandablePropertiesRequired").asText().equals("Y")){
				 int expandableListSize=0;
				 if(carrierSOAPConfig.has("expandablePropertiesList"))
				 {
					 
					 JsonNode expandableList = carrierSOAPConfig.get("expandablePropertiesList");
					 String[] expandableValueList = new String[carrierSOAPConfig.get("expandablePropertiesList").size()];
					 log.debug("expandablePropertiesList size :"+carrierSOAPConfig.get("expandablePropertiesList").size());
					 ArrayNode expandableListArrNode = (ArrayNode)expandableList;
					 for(JsonNode expandableListValue: expandableListArrNode)
					 {
						 expandableValueList[expandableListSize] = expandableListValue.textValue();
						 expandableListSize++;						 
					 }	
			 			request = soapconnector.CreateAttributeRequest(request,expandableValueList);
			 		}	
			 }
			 	/**
			 	 * carrier required Attribute inside tag then below if verified and convert into tag Attribute 
			 	 * Attribute before @ sign required  (eg. HealthProposalRequest-28-15-sample) 
			 	 **/
			 	if(carrierSOAPConfig.has("isAttributeRequired")){
			 		if(carrierSOAPConfig.findValue("isAttributeRequired").asText().equals("Y")){
			 			request = soapconnector.CreateAttributeRequest(request);
			 		}
			 	}
			 	/**
			 	 * carrier required request in !<[CDATA ... ]> format.  
			 	 *  
			 	 **/
			 	if(carrierSOAPConfig.has("isCDATArequired") && carrierSOAPConfig.findValue("isCDATArequired").asText().equals("Y")){
			 		request=soapconnector.createCDATARequest(request,replacevalmap);
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
			    soapRequest = soapconnector.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), carrierSOAPConfig.findValue("schemaLocation").asText(), inputParamMap );
				}else{
					Map<String, Object> inputParamMap = new HashMap<String,Object>();
				    inputParamMap.put(carrierSOAPConfig.findValue("methodParam").asText(), request);
				    soapRequest = soapconnector.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), "", inputParamMap );
				}
			 }
				exchange.getIn().setBody(soapRequest);
			
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|"+"HealthProposalAttributeReqProcessor : ",e);
			throw new ExecutionTerminator();
		}
		
	}

}
