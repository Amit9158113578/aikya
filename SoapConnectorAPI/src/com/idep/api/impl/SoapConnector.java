/**
 * 
 */
package com.idep.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.CDATA;
import org.dom4j.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.sym.Name;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;
import com.idep.api.ISoapConnector;


/**
 * @author vipin.patil
 *
 */
public class SoapConnector implements ISoapConnector {

Logger log = Logger.getLogger(SoapConnector.class.getName());
	
	
public String prepareSoapRequest (String methodName,String schemaLocation,Map<String,Object> inputParamMap)
{
	String methodPrefix = "clientMethod";
	String result = null;
	try{

           MessageFactory factory = MessageFactory.newInstance();
           SOAPMessage soapMsg = factory.createMessage();
           SOAPPart part = soapMsg.getSOAPPart();
           SOAPEnvelope envelope = part.getEnvelope();
           envelope.addNamespaceDeclaration(methodPrefix, schemaLocation);
           SOAPBody body = envelope.getBody();
           SOAPElement methodElement = body.addChildElement(methodName,methodPrefix);
           Set<Entry<String, Object>> inputParamMapEntrySet = inputParamMap.entrySet();
           Iterator<Entry<String, Object>> inputParamMapItr  = inputParamMapEntrySet.iterator();
           SOAPElement childElement = null;
           while(inputParamMapItr.hasNext())
           {
        	   Entry<String, Object> entry = inputParamMapItr.next();
        	   childElement = methodElement.addChildElement(entry.getKey(), methodPrefix);
        	   childElement.addTextNode(entry.getValue().toString());
        	}
           //soapMsg.writeTo(System.out);
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
	       soapMsg.writeTo(baos); 
           result = baos.toString();
           if(result.contains("&gt;") || result.contains("&lt;"))
			{
       	   result = result.replaceAll("&gt;", ">");
       	   result = result.replaceAll("&lt;", "<");
			} 
          if(result.contains("&amp;"))
          {
       	   result = result.replaceAll("&amp;", "&");
          }
          log.debug("Soap MSG : "+result);
       }catch(Exception e){
    	   log.error("Error at prepareSoapRequest : ",e);
       }
	return result;
   }
public String prepareSoapRequest (String request,String schemaLocation)
{
	String result = null;
	try{
		
		   request = request.substring(38, request.length()).trim();
           MessageFactory factory = MessageFactory.newInstance();
           SOAPMessage soapMsg = factory.createMessage();
           SOAPPart part = soapMsg.getSOAPPart();
           SOAPEnvelope envelope = part.getEnvelope();
           envelope.addNamespaceDeclaration("", schemaLocation);
           SOAPBody body = envelope.getBody();
           body.addTextNode(request);
           //body.addTextNode(request);
           //soapMsg.writeTo(System.out);
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
	       soapMsg.writeTo(baos); 
           result = baos.toString();
           if(result.contains("&gt;") || result.contains("&lt;"))
			{
        	   result = result.replaceAll("&gt;", ">");
        	   result = result.replaceAll("&lt;", "<");
			} 
           if(result.contains("&amp;"))
           {
        	   result = result.replaceAll("&amp;", "&");
           }
          //log.info("Soap MSG : "+result);
       }catch(Exception e){
    	   log.error("Error at prepareSoapRequest : ",e);
       }
	return result;
   }
	
	public String getSoapResult(String soapResponse,String tagName)
	{
		try{
		return soapResponse.substring(soapResponse.indexOf("<"+tagName+">")+tagName.length()+2,soapResponse.indexOf("</"+tagName+">"));	
	}catch(Exception e){
 	   log.error("Error at getSoapResult method at prepareSoapRequest : ",e);
 	   return null;
    }
	}
	
	public String prepareSoapResult(String soapResponse,String tagName)
	{
		try{
			soapResponse =soapResponse.replaceAll("<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><env:Header/>","");
			soapResponse = soapResponse.replaceAll(" xmlns:typ=\"http://iims.services/types/\"","");
			soapResponse = soapResponse.replaceAll("typ:", "");
			soapResponse = soapResponse.replaceAll("/typ:", "");
			soapResponse = soapResponse.replaceAll("xsi:", "");
			//log.info("Formatted Soap Result : "+soapResponse.substring(soapResponse.indexOf("<"+tagName+">")+tagName.length()+2,soapResponse.indexOf("</"+tagName+">")));
		return soapResponse.substring(soapResponse.indexOf("<"+tagName+">")+tagName.length()+2,soapResponse.indexOf("</"+tagName+">"));	
	}catch(Exception e){
 	   log.error("Error at getSoapResult method at prepareSoapRequest : ",e);
 	   return null;
    }
	}
	public String retriveSoapResult(String soapResponse,String tagName) throws  SAXException, IOException, ParserConfigurationException, TransformerException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(false);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document document = dBuilder.parse(new InputSource(new StringReader(soapResponse)));
		Node nodeList = document.getElementsByTagName(tagName).item(0);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(nodeList);
        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult( outWriter );
        transformer.transform( source, result );  
        StringBuffer sb = outWriter.getBuffer(); 
        String finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>","");
        finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
        finalstring = finalstring.replaceAll(" xmlns", "xmlns");
       
       return removeXmlStringNamespaceAndPreamble(finalstring);
	}
	public static String removeXmlStringNamespaceAndPreamble(String xmlString) {
		  return xmlString.replaceAll("(<\\?[^<]*\\?>)?", ""). /* remove preamble */
		  replaceAll("xmlns.*?(\"|\').*?(\"|\')", "") /* remove xmlns declaration */
		  .replaceAll("(<)(\\w+:)(.*?>)", "$1$3") /* remove opening tag prefix */
		  .replaceAll("(</)(\\w+:)(.*?>)", "$1$3"); /* remove closing tags prefix */
		}
	public String createCDATARequest(String request) {
		try
		{
			//log.info("Input Request : "+request);
			request = request.substring(38, request.length());
			//log.info("Formatted Request : "+request);
			CDATA cdata = DocumentHelper.createCDATA(request);
			return cdata.asXML();
		}
		catch(Exception e)
		{
		 	log.error("Error at createCDATARequest method at prepareSoapRequest : ",e);
		 	return null;
		}
	}
	/*
	 * method Name : CalculatePremium
	 * method Param : NewHealthCPURL
	 * Request  : Complete Request without CDATA
	 * Tns map : CalculatePremium,http://tempuri.org/
	 * 				
	 * 
	 * */
	
	public String prepareSoapRequest(String methodName,String methodParam,String request,Map<String,String> tnsMap) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document document = dBuilder.parse(new InputSource(new StringReader(request)));
		Set<Entry<String, String>> tnsEntrySet = tnsMap.entrySet();
		Iterator<Entry<String, String>> tnsItr = tnsEntrySet.iterator();
		while(tnsItr.hasNext())
		{
			Entry<String, String> tnsEntry = tnsItr.next();
			String key = tnsEntry.getKey().trim();
			if(!key.equalsIgnoreCase("parentTns"))
			{
				Element  element = (org.w3c.dom.Element) document.getElementsByTagName(key).item(0);
				element.setAttribute("xmlns", tnsEntry.getValue().trim());
			}
	}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StringWriter outWriter = new StringWriter();
        StreamResult result = new StreamResult( outWriter );
        transformer.transform( source, result );  
        StringBuffer sb = outWriter.getBuffer(); 
        
        String finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>","");
        finalstring = sb.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
        Map<String, Object> paramMap =  new HashMap<String, Object>();
        paramMap.put(methodParam, finalstring);
        String response = prepareSoapRequest(methodName,tnsMap.get("parentTns").toString().trim(),paramMap);
        response = StringEscapeUtils.unescapeXml(response);
       // log.info("SOAP Final Message : "+response);
        return response;
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
	
	public String getSoapResult(String s, String startTagName, String endTagName, String testString[], String apppendTag)
	{
		try{
			String soapResponse = s.substring(s.indexOf("<"+startTagName+">")+startTagName.length()+2,s.indexOf("</"+endTagName+">"));
			//StringBuffer soapResponseBuffer = new StringBuffer(soapResponse);
			if(testString!=null){
				if(testString.length>0){
					for(int index=0;index<testString.length;index++)
					{
						if (soapResponse.contains(testString[index]))
						{
							soapResponse = soapResponse.replaceAll(testString[index],"");
						}
					}
				}//testString length if condition END
			}//testString is not null if condition END
			String finalSoapResponse =  "<"+apppendTag+">"+soapResponse+"</"+apppendTag+">";				
			return finalSoapResponse;		
			
	}catch(Exception e){
 	   log.error("Error at getSoapResult method: ",e);
 	   return null;
    }
	}
	
	public String createCDATARequest(String request,HashMap<String, String> attributesList) {
		CDATA cdata=null;
		try
		{
				for (Map.Entry<String, String> entry : attributesList.entrySet()) {
					request=request.replace(entry.getKey(), entry.getValue());
				}
				//log.info("Request Before CDATA  : "+request);
				cdata = DocumentHelper.createCDATA(request);
			return cdata.asXML();
		}
		catch(Exception e)
		{
			log.error("Error at createCDATARequest method at prepareSoapRequest : ",e);
			return cdata.asXML();
		}
	}
	
	
	
	@Override
	public String CreateAttributeRequest(String request) {
		String response=null;
		try{
			  JSON json = JSONSerializer.toJSON( request );
				XMLSerializer xmlSerializer = new XMLSerializer();
				xmlSerializer.setTypeHintsEnabled(false);
				xmlSerializer.setTypeHintsCompatibility( false );
	          response=xmlSerializer.write( json );
			//log.info("Attribute added in request Successfully converted : "+response);
		}catch(Exception e){
			
			log.info("Error in SoapConnector , CretateAttributeRequest() : "+e);
		}
		
		
		return response;
	}

	public String CreateAttributeRequest(String request,String expandableValueList[]) {
		String response=null;
		try{
			  JSON json = JSONSerializer.toJSON( request );
				XMLSerializer xmlSerializer = new XMLSerializer();
				xmlSerializer.setTypeHintsEnabled(false);
				xmlSerializer.setTypeHintsCompatibility( false );
				xmlSerializer.setExpandableProperties(expandableValueList);
	          response=xmlSerializer.write( json );
		}catch(Exception e){
			
			log.info("Error in SoapConnector , CretateAttributeRequest() : "+e);
		}			
		
		return response;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		SoapConnector  soapService = new SoapConnector();
		//String request="<ihpPolicy><criticalIllness>Y</criticalIllness><criticalIllness>Y</criticalIllness><expiryDate>08/09/2018</expiryDate><ihpInsured><item><dateOfBirth>01/07/1982</dateOfBirth><preExistingDisease>N</preExistingDisease><relationToInsured>self</relationToInsured><sumInsured>500000</sumInsured></item></ihpInsured><inceptionDate>08/10/2017</inceptionDate><partnerCode>ITGIIHP001</partnerCode><promoCode>IHP2016</promoCode><roomRentWaiver>N</roomRentWaiver><roomRentWaiver>N</roomRentWaiver><uniqueQuoteId>TY868889</uniqueQuoteId></ihpPolicy>";
		String methodName="CreatePolicy";
	    String methodParam = "XML";
		 String request = "<req><clientMethod:Product>HealthTotal</clientMethod:Product><clientMethod:XML><![CDATA[n  | <Root><Uid>de2c01fb-2d08-4fa9-ba5b-01b082bf8a37</Uid><VendorCode>Eworksite</VendorCode><VendorUserId>Eworksite</VendorUserId><PolicyHeader><PolicyStartDate/><PolicyEndDate/><AgentCode/><BranchCode>10</BranchCode><MajorClass>HTO</MajorClass><ContractType>HTO</ContractType><METHOD>ENQ</METHOD><PolicyIssueType>I</PolicyIssueType><PolicyNo/><ClientID/><ReceiptNo/></PolicyHeader><Client><ClientType/><CreationType/><Salutation/><FirstName/><LastName/><DOB/><Gender/><MaritalStatus/><Occupation/><GSTIN/><AadharNo/><CKYCNo/><EIANo/><Address1><AddrLine1/><AddrLine2/><AddrLine3/><Landmark/><Pincode>400051</Pincode><City>PUNE</City><State>Maharashtra</State><Country/><AddressType/><HomeTelNo/><OfficeTelNo/><FAXNO/><MobileNo>9984751421</MobileNo><EmailAddr/></Address1><Address2><AddrLine1/><AddrLine2/><AddrLine3/><Landmark/><Pincode>000000</Pincode><City/><State/><Country/><AddressType/><HomeTelNo/><OfficeTelNo/><FAXNO/><MobileNo/><EmailAddr/></Address2></Client><Receipt><UniqueTranKey/><CheckType/><BSBCode/><TransactionDate/><ReceiptType/><Amount/><TCSAmount/><TranRefNo/><TranRefNoDate/></Receipt><Risk><PolicyType>HTI</PolicyType><Duration>1</Duration><Installments>FULL</Installments><IsFgEmployee>N</IsFgEmployee><BeneficiaryDetails><Member><MemberId>1</MemberId><InsuredName/><InsuredDob>06/09/1990</InsuredDob><InsuredGender/><InsuredOccpn/><CoverType>VITAL</CoverType><SumInsured>1000000</SumInsured><DeductibleDiscount>0</DeductibleDiscount><Relation>SELF</Relation><NomineeName/><NomineeRelation/><MedicalLoading>0</MedicalLoading><PreExstDisease>N</PreExstDisease><DiseaseMedicalHistoryList><DiseaseMedicalHistory><PreExistingDiseaseCode/><MedicalHistoryDetail/></DiseaseMedicalHistory></DiseaseMedicalHistoryList></Member></BeneficiaryDetails></Risk></Root>n  | ]]></clientMethod:XML></req>";
		    Map<String, Object> inputParamMap = new HashMap<String,Object>();
		    String schemaLocation="http://tempuri.org/";
		    inputParamMap.put("CreatePolicy", request);
		    String soapRequest = soapService.prepareSoapRequest(methodName, schemaLocation, inputParamMap);
		    //String soapRequest = soapService.prepareSoapRequest( request,schemaLocation);
		   // System.out.println("OUTUT : "+soapRequest);
		    
		    /*  if(soapRequest.contains("&gt;") || soapRequest.contains("&lt;"))
				{
					soapRequest = soapRequest.replaceAll("&gt;", ">");
					soapRequest = soapRequest.replaceAll("&lt;", "<");
				}*/
			
		}
	

	
	
}

