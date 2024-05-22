package com.idep.travelquote.req.transformer;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.travelquote.exception.processor.ExecutionTerminator;
import com.idep.travelquote.util.TravelQuoteConstants;

public class FutureGenSoapReqFormatter implements Processor {
	Logger log = Logger.getLogger(SOAPRequestFormatter.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	SoapConnector  soapService = new SoapConnector();

	@Override
	public void process(Exchange exchange) throws Exception {
		String methodName	= null;
		String methodParam = null;
		String response = "";
		try {
			Map<String,Object> inputParamMap =  new HashMap<String,Object>();
			String request  = exchange.getIn().getBody(String.class);
			JsonNode configData = exchange.getProperty(TravelQuoteConstants.CARRIER_REQ_MAP_CONF,JsonNode.class);
			/**
			 * Carrier Quote configuration information
			 */
			log.debug("ConfigData ::::::::::::::"+configData);
			
			if (configData.has("carrierSOAPConfig"))
			{
				if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodName"))
				{
					methodName = configData.get("carrierSOAPConfig").get("reqConfig").get("methodName").asText();
					log.debug("methodName::::::::::::-----"+methodName);
				}

				if(configData.get("carrierSOAPConfig").get("reqConfig").has("methodParam"))
				{
					methodParam = configData.get("carrierSOAPConfig").get("reqConfig").get("methodParam").asText();
					log.debug("methodParam::::::"+methodParam);
				}

				if(configData.has("carrierSOAPConfig"))
				{

					JsonNode schemaLocMap = configData.get("carrierSOAPConfig").get("reqConfig").get("schemaLocMap");
					@SuppressWarnings("unchecked")
					Map<String,String> schemaMap = objectMapper.readValue(schemaLocMap.toString(), Map.class);

					JsonNode clientReqAttrList = configData.get("clientReqAttrList");
					JsonNode appkeyList = objectMapper.createObjectNode();
					for(JsonNode list : clientReqAttrList)
					{  
						((ObjectNode)appkeyList).put(list.get("appKey").asText(), list.get("clientKey").asText());  
					}

					@SuppressWarnings("unchecked")
					Map<String,String> ClientReqAttrList = objectMapper.readValue(appkeyList.toString(), Map.class);
					response  = soapService.prepareSoapRequest(methodName, methodParam, request, schemaMap,ClientReqAttrList);
					log.debug("preparesoaprequest method gives response:::::::::---"+response);
					response = "<req><Product>Travel</Product><XML>"+response+"</XML></req>";
					response  = soapService.prepareSoapRequest(methodName, "req", response, schemaMap);
					response =response.replaceAll("<clientMethod:req>", "")
							.replaceAll("</clientMethod:req>", "")
							.replaceAll("<req>", "")
							.replaceAll("</req>", "");
							//.replaceAll("<e>","")
							//.replaceAll("</e>","");
							


					log.debug("Final Future response: "+response ); 		  
				}
			}
			exchange.getIn().setBody(response);

		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.FUTUREGENSOAPREQFORM+"|ERROR|"+" Exception at SOAPRequestFormatter for request :"+response,e);
			throw new ExecutionTerminator();

		}

	}

}
