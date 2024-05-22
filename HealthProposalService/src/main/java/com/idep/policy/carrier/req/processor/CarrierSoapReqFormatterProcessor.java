
package com.idep.policy.carrier.req.processor;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.proposal.req.processor.SoapReqFormatter;
import com.idep.proposal.util.ProposalConstants;

/**
 * @author pravin.jakhi
 *  this class append some depend on carrier tag.
 *  eg .
 *  <Body>
 *  <tag>..</tag> // this tags append 
 *  <XML>
 *  </XML>
 *  </Body>
 */

public class CarrierSoapReqFormatterProcessor implements Processor {

	Logger log = Logger.getLogger(CarrierSoapReqFormatterProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();
	@Override
	public void process(Exchange exchange) throws Exception {
		  String methodName	= null;
		  String methodParam = null;
		  String request  = exchange.getIn().getBody(String.class);
		  exchange.getIn().removeHeader("CamelHttpPath");
		  exchange.getIn().removeHeader("CamelHttpUri");
		  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
		  log.info("modified XML request : "+request);
		  String response = "";
		  try{
		  JsonNode configData = exchange.getProperty(ProposalConstants.CARRIER_REQ_MAP_CONF,JsonNode.class);
		  if(configData==null){
			  configData = exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG,JsonNode.class); 
		  }
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
				 
				  //response = "<req><Product>HealthTotal</Product><XML>"+response+"</XML></req>";
				  if(configData.get("carrierSOAPConfig").has("appendTag")){
				  
					  if(configData.get("carrierSOAPConfig").get("appendTag").has("rootElementTag")){
						  response="<"+configData.get("carrierSOAPConfig").get("appendTag").get("rootElementTag").asText()+">"+
								  response+"</"+configData.get("carrierSOAPConfig").get("appendTag").get("rootElementTag").asText()+">";
					  } 
				  }
				  
				  if(configData.get("carrierSOAPConfig").has("appendTag")){
					  /**
					   * if appendTag has before then tags will append before 
					   * other wise its default
					   * **/
					  String tagAppend="";
					  ArrayNode tagList = (ArrayNode)configData.get("carrierSOAPConfig").get("appendTag").get("tagList");
						  for(JsonNode tag : tagList){
							  if(tag.has("value")){
							  tagAppend=tagAppend+"<"+tag.get("tagName").asText()+">"+tag.get("value").asText()+"</"+tag.get("tagName").asText()+">";
							  }else{
								  tagAppend=tagAppend+"<"+tag.get("tagName").asText()+"/>";  
							  }
						  }
				      if(configData.get("carrierSOAPConfig").get("appendTag").has("before")){
				    	  if( configData.get("carrierSOAPConfig").get("appendTag").get("before").asText().equalsIgnoreCase("Y")){
				    	  response=	 tagAppend.concat(response);
				    	  }else{
				    		  response=	response.concat(tagAppend);
				    	  }
					  }else{
						  response=	response.concat(tagAppend);
					  }
				  }
				  response="<req>"+response+"</req>";
				  log.info("Response after appending XML" +response);
				  response  = soapService.prepareSoapRequest(methodName, "req", response, schemaMap);
				  response =response.replaceAll("<clientMethod:req>", "")
						   .replaceAll("</clientMethod:req>", "")
						   .replaceAll("<req>", "")
						   .replaceAll("</req>", "")	
						   .replaceAll("<o>", "")
						   .replaceAll("</o>", "");	
				  
				 
				  
				  
				  
				  log.info("Final Carrier  xml response: "+response ); 		  
			  }
		  }
		  
		  exchange.getIn().setBody(response);
			
		}
		catch(Exception e)
		{
			log.error("Exception at SOAPRequestFormatter : ",e);
		}
		
		
		
	}
}
