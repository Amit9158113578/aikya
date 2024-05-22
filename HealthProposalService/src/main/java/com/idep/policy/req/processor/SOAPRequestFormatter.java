package com.idep.policy.req.processor;


import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.XMLParser;
import org.eclipse.jetty.util.ajax.JSONCollectionConvertor;
import org.eclipse.jetty.util.ajax.JSONObjectConvertor;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;

public class SOAPRequestFormatter implements Processor {
	
	  Logger log = Logger.getLogger(SOAPRequestFormatter.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
			
		  String request  = exchange.getIn().getBody(String.class);
		  exchange.getIn().removeHeader("CamelHttpPath");
		  exchange.getIn().removeHeader("CamelHttpUri");
		  exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/xml");
		  exchange.getIn().setHeader(Exchange.ACCEPT_CONTENT_TYPE, "text/xml");
		  exchange.getIn().setHeader(Exchange.HTTP_METHOD, "POST");
		  exchange.getIn().setHeader("SOAPAction", "http://tempuri.org/CalculatePremium");
		 /*exchange.getIn().removeHeader("CamelHttpPath");
		  exchange.getIn().removeHeader("CamelHttpUri");
		  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");*/
		  //log.info("modified XML request : "+request);
	      Map<String, Object> tnsMap =  new HashMap<String,Object>();
	      String methodName="CalculatePremium";
	     String schemaLocation="xmlns=\"http://tempuri.org/\"";
	      SoapConnector  soapService = new SoapConnector();
	      tnsMap.put("str",request);
	      String soapRequest = soapService.prepareSoapRequest(methodName, schemaLocation, tnsMap);
		  log.info("headers list : "+exchange.getIn().getHeaders());
          
			  exchange.getIn().setBody(soapRequest);
		    
		}
		catch(Exception e)
		{
			log.error("Exception at Health SOAPRequestFormatter : "+e);
			throw new ExecutionTerminator();
		}
		  
	}

	
}
