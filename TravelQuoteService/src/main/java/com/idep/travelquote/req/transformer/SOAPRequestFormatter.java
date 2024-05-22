package com.idep.travelquote.req.transformer;


import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.api.impl.SoapConnector;
import com.idep.travelquote.util.TravelQuoteConstants;

public class SOAPRequestFormatter implements Processor {

	Logger log = Logger.getLogger(SOAPRequestFormatter.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {

		try {

			String request  = exchange.getIn().getBody(String.class);
			exchange.getIn().removeHeader("CamelHttpPath");
			exchange.getIn().removeHeader("CamelHttpUri");
			request = request.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>","");
			log.debug("modified XML request : "+request);
			Map<String, String> tnsMap =  new HashMap<String,String>();
			tnsMap.put("SessionData", "http://schemas.cordys.com/bagi/b2c/emotor/bpm/1.0");
			tnsMap.put("Vehicle", "http://schemas.cordys.com/bagi/b2c/emotor/2.0");
			tnsMap.put("Quote", "http://schemas.cordys.com/bagi/b2c/emotor/2.0");
			tnsMap.put("Client", "http://schemas.cordys.com/bagi/b2c/emotor/2.0");
			tnsMap.put("parentTns", "http://schemas.cordys.com/gateway/Provider");
			SoapConnector  soapService = new SoapConnector();
			String response  = soapService.prepareSoapRequest("serve", "SessionDoc", request, tnsMap);
			exchange.getIn().setHeader("soapaction", "");
			exchange.getIn().setBody(response);

		}
		catch(Exception e)
		{
			log.error(exchange.getProperty(TravelQuoteConstants.LOG_REQ).toString()+TravelQuoteConstants.CARRIERREQ+"|"+"SOAP Request formation failed :",e);

		}

	}

}
