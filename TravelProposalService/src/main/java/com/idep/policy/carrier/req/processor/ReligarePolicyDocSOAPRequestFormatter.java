package com.idep.policy.carrier.req.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.proposal.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ReligarePolicyDocSOAPRequestFormatter implements Processor {
	
	  Logger log = Logger.getLogger(ReligarePolicyDocSOAPRequestFormatter.class.getName());
	  SoapConnector  soapService = new SoapConnector();
	  ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws ExecutionTerminator {

		try {
			
			String soapRequest=null;
			  String request  = exchange.getIn().getBody(String.class);
		  	  
		  log.debug("after removing unwanted strings modified XML request : "+request);
		  
		  String configdoc=exchange.getProperty(ProposalConstants.TRAVPOLICYDOC_CONFIG).toString();
			 JsonNode Carrierconfigdoc = objectMapper.readTree(configdoc);
		  
		  if(Carrierconfigdoc.has("carrierSOAPConfig")){
				 JsonNode carrierSOAPConfig = Carrierconfigdoc.get("carrierSOAPConfig");
			 HashMap<String,String> replacevalmap = objectMapper.readValue(carrierSOAPConfig.get("reqConfig").get("removeAttrList").toString(), HashMap.class);
			 log.debug("MAP HelProposalAttribute Request  : "+replacevalmap.toString());
				/** 
				 * Passing dynamic Values to preparesoapRequest
				 * MethodName = parentTag name
				 * SchemaLocation = carrier schema url  if in case schema location not available then pass "" 
				 * inputParamMap = Child Tag
				 * */
				if(carrierSOAPConfig.get("reqConfig").has("methodName") && carrierSOAPConfig.get("reqConfig").has("tnsMap")){
			    Map<String, String> tnsParamMap = objectMapper.readValue(carrierSOAPConfig.get("reqConfig").get("tnsMap").toString(), HashMap.class);
			    soapRequest = soapService.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), carrierSOAPConfig.findValue("methodParam").asText(),request ,tnsParamMap );//      prepareSoapRequest("createPolicy", "intIO", request, inputParamMap);
				}
				if(soapRequest!=null && soapRequest.length()>0){
				for(Map.Entry<String, String> entry: replacevalmap.entrySet() ){
					  soapRequest=soapRequest.replaceAll(entry.getKey().toString(), entry.getValue().toString());
				  }
				}else{
					log.error("unable to genrate SoapRequest : "+request);
				}
		  }
		 log.info("Genrated Carrier POlicy Document  XML Request : "+soapRequest);
		exchange.getIn().setBody(soapRequest);
		  
		/*  String response  ="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://web.com/\"><soapenv:Header/><soapenv:Body><web:GET_PDF>"
				  +request
				  + "</web:GET_PDF></soapenv:Body></soapenv:Envelope>";
		  
		  exchange.getIn().setBody(response);
		    
		*/	
		}
		catch(Exception e)
		{
			log.error("unable to genrate Travel Policy Document request ",e);
			throw new ExecutionTerminator();
		}
		  
	}
	
}
