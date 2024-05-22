package com.idep.proposal.req.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.api.impl.SoapConnector;
import com.idep.proposal.util.ProposalConstants;

public class FutureGenraliSoapReqFormatter implements Processor{ 
	Logger log = Logger.getLogger(FutureGenraliSoapReqFormatter.class);
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
	  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","").replace("<Session>","").replace("</Session>", "");
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
			  ObjectNode appkeyList = objectMapper.createObjectNode();
			  for(JsonNode list : clientReqAttrList)
			  {  
				  appkeyList.put(list.get("appKey").asText(), list.get("clientKey").asText());  
			  }
			  log.debug("Genrated ClientReq Attr Before map converion : "+appkeyList);
			  @SuppressWarnings("unchecked")
			  Map<String,String> ClientReqAttrList = objectMapper.readValue(appkeyList.toString(), Map.class);
			  response  = new FutureGenraliSoapReqFormatter().prepareSoapRequest(methodName, methodParam, request, schemaMap,ClientReqAttrList);
			  log.info("Future response: "+response );
			  response = "<req><Product>Travel</Product><XML>"+response+"</XML></req>";
			  log.info("Response after appending XML" +response);	 
			  response  = new FutureGenraliSoapReqFormatter().prepareSoapRequest(methodName, "req", response, schemaMap);
			  response =response.replaceAll("<clientMethod:req>", "")
					   .replaceAll("</clientMethod:req>", "")
					   .replaceAll("<req>", "")
					   .replaceAll("</req>", "")
					   .replaceAll("<ClientID/>", "<ClientID></ClientID>");					   
			  log.info("Final Future Travel response: "+response ); 		  
		  }
	  }
	  
	  exchange.getIn().setBody(response);
		
	}
	catch(Exception e)
	{
		log.error("Exception at SOAPRequestFormatter of Travel Proposal : ",e);
	}
	
}
	public String prepareSoapRequest(String methodName,String methodParam,String request,Map<String,String> tnsMap, Map<String,String> clientReqAttrList) throws ParserConfigurationException, SAXException, IOException, TransformerException 
	{
		try
		{
		//log.info("Request " + request);
		CDATA cdata=null;
        Set<Entry<String, String>> clientEntrySet = clientReqAttrList.entrySet();
		Iterator<Entry<String, String>> clientItr = clientEntrySet.iterator();
		while(clientItr.hasNext())
		{
			Entry<String, String> clientEntry = clientItr.next();
			String key = clientEntry.getKey().trim();
			String value = clientEntry.getValue().trim();
			request= request.replaceAll(key, value);			
		}
		//log.info("Request before appending CDATA: "+request );
		cdata = DocumentHelper.createCDATA(request);
		request = cdata.asXML();
		//log.info("Request after appending CDATA: "+request );
		}
		
		catch(Exception e)
		{
		log.info("Error at createCDATARequest method : ",e);	
		}
		return request;
	}    
	
	 public String prepareSoapRequest(String methodName, String methodParam, String request, Map<String, String> tnsMap)
			    throws ParserConfigurationException, SAXException, IOException, TransformerException
			  {
			    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			    Document document = dBuilder.parse(new InputSource(new StringReader(request)));
			    Set<Map.Entry<String, String>> tnsEntrySet = tnsMap.entrySet();
			    Iterator<Map.Entry<String, String>> tnsItr = tnsEntrySet.iterator();
			    while (tnsItr.hasNext())
			    {
			      Map.Entry<String, String> tnsEntry = (Map.Entry)tnsItr.next();
			      String key = ((String)tnsEntry.getKey()).trim();
			      if (!key.equalsIgnoreCase("parentTns"))
			      {
			        Element element = (Element)document.getElementsByTagName(key).item(0);
			        element.setAttribute("xmlns", ((String)tnsEntry.getValue()).trim());
			      }
			    }
			    TransformerFactory transformerFactory = TransformerFactory.newInstance();
			    Transformer transformer = transformerFactory.newTransformer();
			    DOMSource source = new DOMSource(document);
			    StringWriter outWriter = new StringWriter();
			    StreamResult result = new StreamResult(outWriter);
			    transformer.transform(source, result);
			    StringBuffer sb = outWriter.getBuffer();
			    
			    String finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
			    finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
			    Map<String, Object> paramMap = new HashMap();
			    paramMap.put(methodParam, finalstring);
			    String response = soapService.prepareSoapRequest(methodName, ((String)tnsMap.get("parentTns")).toString().trim(), paramMap);
			    response = StringEscapeUtils.unescapeXml(response);
			    
			    return response;
			  }
	

}

