package com.idep.proposal.req.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.api.impl.SoapConnector;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.proposal.util.ProposalConstants;

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
			  /*
			  request = request.replace("<o>", "");
			  request = request.replace("</o>", "");
			  
		      Map<String, String> tnsMap =  new HashMap<String,String>();
			  tnsMap.put("policy", "http://intf.insurance.symbiosys.c2lbiz.com/xsd");
			  tnsMap.put("parentTns", "http://relinterface.insurance.symbiosys.c2lbiz.com");
			  */
			  String configdoc=exchange.getProperty(ProposalConstants.PROPOSALREQ_CONFIG).toString();
				 JsonNode Carrierconfigdoc = this.objectMapper.readTree(configdoc);
			  
			  if(Carrierconfigdoc.has("carrierSOAPConfig")){
					 JsonNode carrierSOAPConfig = Carrierconfigdoc.get("carrierSOAPConfig");
				 HashMap<String,String> replacevalmap = objectMapper.readValue(carrierSOAPConfig.get("reqConfig").get("removeAttrList").toString(), HashMap.class);
				 log.debug("MAP HelProposalAttribute Request  : "+replacevalmap.toString());
				 /**
				 	 * carrier required Attribute inside tag then below if verified and convert into tag Attribute 
				 	 * Attribute before @ sign required  (eg. HealthProposalRequest-28-15-sample) 
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
				 		request=new TravelProposalAttributeReqProcessor().createCDATARequest(request,replacevalmap);
				 	}
					/** 
					 * Passing dynamic Values to preparesoapRequest
					 * MethodName = parentTag name
					 * SchemaLocation = carrier schema url  if in case schema location not available then pass "" 
					 * inputParamMap = Child Tag
					 * */
				 	
					if(carrierSOAPConfig.get("reqConfig").has("methodName") && carrierSOAPConfig.get("reqConfig").has("tnsMap")){
				    Map<String, String> tnsParamMap = objectMapper.readValue(carrierSOAPConfig.get("reqConfig").get("tnsMap").toString(), HashMap.class);
				    soapRequest = new SoapReqFormatter().prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), carrierSOAPConfig.findValue("methodParam").asText(),request ,tnsParamMap );//      prepareSoapRequest("createPolicy", "intIO", request, inputParamMap);
					}
					if(soapRequest!=null && soapRequest.length()>0){
					for(Map.Entry<String, String> entry: replacevalmap.entrySet() ){
						  soapRequest=soapRequest.replaceAll(entry.getKey().toString(), entry.getValue().toString());
					  }
					 		
					 }
					log.info("Genrated xml Before process"+ " : "+soapRequest);
					if(carrierSOAPConfig.get("reqConfig").has("methodName") && carrierSOAPConfig.get("reqConfig").has("schemaLocation")){
					    Map<String, Object> inputParamMap = new HashMap<String,Object>();
					    inputParamMap.put(carrierSOAPConfig.findValue("methodParam").asText(), request);
					    soapRequest = soapService.prepareSoapRequest(carrierSOAPConfig.findValue("methodName").asText(), carrierSOAPConfig.findValue("schemaLocation").asText(), inputParamMap );
					}
					else{
						log.error("unable to genrate SoapRequest : "+request);
					}
			  }
			  soapRequest=soapRequest.replace("<partyQuestionDOList><partyQuestionDOList>","<partyQuestionDOList>");
				 soapRequest=soapRequest.replace("</partyQuestionDOList></partyQuestionDOList>","</partyQuestionDOList>");
			 log.info("Genrated Carrier Proposal XML Request : "+soapRequest);
			exchange.getIn().setBody(soapRequest);
			
		}catch(Exception e){
		log.error("error while preparing soap request : ",e);	
		}
		
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
