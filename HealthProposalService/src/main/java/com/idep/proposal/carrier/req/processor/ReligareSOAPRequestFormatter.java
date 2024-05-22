package com.idep.proposal.carrier.req.processor;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.policy.exception.processor.ExecutionTerminator;
import com.idep.proposal.util.ProposalConstants;

public class ReligareSOAPRequestFormatter implements Processor {
	
	  Logger log = Logger.getLogger(ReligareSOAPRequestFormatter.class.getName());
	  SoapConnector  soapService = new SoapConnector();

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		  String request  = exchange.getIn().getBody(String.class);
		  
		  request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
		  request = request.replace("<o>", "");
		  request = request.replace("</o>", "");
		  
	      Map<String, String> tnsMap =  new HashMap<String,String>();
		  tnsMap.put("policy", "http://intf.insurance.symbiosys.c2lbiz.com/xsd");
		  tnsMap.put("parentTns", "http://relinterface.insurance.symbiosys.c2lbiz.com");
		  
		  String response  = soapService.prepareSoapRequest("createPolicy", "intIO", request, tnsMap);
        
		  exchange.getIn().setBody(response);
		    
			
		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|ReligaresSOAPRequestFormatter|",e);
			throw new ExecutionTerminator();
		}
		  
	}
	
	
	public String JsonToXML (String request)
	{
		JSON json = JSONSerializer.toJSON( request );
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.setTypeHintsEnabled(false);
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

}
