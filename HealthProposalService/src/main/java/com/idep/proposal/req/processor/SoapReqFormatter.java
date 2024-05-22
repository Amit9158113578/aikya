package com.idep.proposal.req.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.proposal.util.ProposalConstants;

public class SoapReqFormatter implements Processor{ 
	Logger log = Logger.getLogger(SoapReqFormatter.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		// TODO Auto-generated method stub


	try {
	  String methodName	= null;
	  String methodParam = null;
	  String request  = exchange.getIn().getBody(String.class);
	  exchange.getIn().removeHeader("CamelHttpPath");
	  exchange.getIn().removeHeader("CamelHttpUri");
	  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
	  log.info("modified XML request : "+request);
	  String response = "";
	  
	  JsonNode configData = exchange.getProperty(ProposalConstants.POLICYREQ_CONF,JsonNode.class);
	  log.info("configData : "+configData);
	  /**
	   * Carrier Quote configuration information
	   */
	  if (configData.has("carrierSOAPConfig"))
	  {
		  if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodName"))
		  {
			  methodName = configData.get("carrierSOAPConfig").get("reqConfig").get("methodName").asText();
			  log.info("methodName : "+methodName);
		  }
		  
		  if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodParam"))
		  {
			  methodParam = configData.get("carrierSOAPConfig").get("reqConfig").get("methodParam").asText();
			  log.info("methodParam : "+methodParam);
		  }
		  
		  if(configData.has("carrierSOAPConfig"))
		  {
			  
			  JsonNode schemaLocMap = configData.get("carrierSOAPConfig").get("reqConfig").get("schemaLocMap");
			  log.info("schemaLocMap : "+schemaLocMap);
			  @SuppressWarnings("unchecked")
			  Map<String,String> schemaMap = objectMapper.readValue(schemaLocMap.toString(), Map.class);
			  JsonNode clientReqAttrList = configData.get("clientReqAttrList");
			  log.info("clientReqAttrList : "+clientReqAttrList);
			  ObjectNode appkeyList = objectMapper.createObjectNode();
			  for(JsonNode list : clientReqAttrList)
			  {  
				  appkeyList.put(list.get("appKey").asText(), list.get("clientKey").asText());  
			  }
			  log.info("Genrated ClientReq Attr Before map converion : "+appkeyList);
			  @SuppressWarnings("unchecked")
			  Map<String,String> ClientReqAttrList = objectMapper.readValue(appkeyList.toString(), Map.class);
			  response  = soapService.prepareSoapRequest(methodName, methodParam, request, schemaMap,ClientReqAttrList);
			  log.info("Future response: "+response );
			  response = "<req><Product>HealthTotal</Product><XML>"+response+"</XML></req>";
			  log.info("Response after appending XML" +response);	 
			  response  = soapService.prepareSoapRequest(methodName, "req", response, schemaMap);
			  response =response.replaceAll("<clientMethod:req>", "")
					   .replaceAll("</clientMethod:req>", "")
					   .replaceAll("<req>", "")
					   .replaceAll("</req>", "")
					   .replaceAll("<ClientID/>", "<ClientID></ClientID>");					   
			  log.info("Final Future response: "+response ); 		  
		  }
	  }
	  
	  exchange.getIn().setBody(response);
		
	}
	catch(Exception e)
	{
		log.error("Exception at SOAPRequestFormatter of Health Proposal : ",e);
	}
	
}

}

