package com.idep.proposal.carrier.req.processor;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class TestProcessor implements Processor {
	
	  Logger log = Logger.getLogger(TestProcessor.class.getName());
	  CBService service =  CBInstanceProvider.getServerConfigInstance();
	  ObjectMapper objectMapper = new ObjectMapper();
	  
	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
			String s = service.getDocBYId("TestHealthSoap").content().getString("request");
			log.info("TestHealthSoap : "+ s);
			exchange.getIn().setBody(s);
		}
		catch(Exception e)
		{
			log.error(e);
		}
	
	//

}
	
	public String JsonToXML (String request)
	{
		JSON json = JSONSerializer.toJSON( request );
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.setTypeHintsEnabled(false);
		xmlSerializer.setSkipNamespaces(true);
		xmlSerializer.setTypeHintsCompatibility( false );
		String response=xmlSerializer.write( json );
		return response;
	}
	
	public String XMLToJSON (String request)
	{
			
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSON json = xmlSerializer.read(request);
		
		return json.toString();
	}
	
	
	public static void main(String[] args) {
		
		TestProcessor t = new TestProcessor();
		CBService service =  CBInstanceProvider.getServerConfigInstance();
		String request = service.getDocBYId("TestHealthSoap").content().getString("request");
		System.out.println(" XML request : "+request);
		String response  = t.XMLToJSON(request);
		System.out.println(" JSON response : "+response);
		
		String xmlResponse = t.JsonToXML(response);
		System.out.println(" XML Response :"+xmlResponse);
		
	}

}