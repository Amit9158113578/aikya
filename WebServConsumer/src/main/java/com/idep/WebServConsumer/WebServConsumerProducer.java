package com.idep.WebServConsumer;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.log4j.Logger;

import com.idep.webservice.consume.service.WebServiceInvoke;

/**
 * The WebServiceConsumer producer.
 * Consume web service by setting headers and URL based on configuration
 */
public class WebServConsumerProducer extends DefaultProducer {
    
	Logger log = Logger.getLogger(WebServConsumerProducer.class);
    @SuppressWarnings("unused")
    private WebServConsumerEndpoint endpoint;
    WebServiceInvoke invoke = new WebServiceInvoke();
   

    public WebServConsumerProducer(WebServConsumerEndpoint endpoint) {
    	
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws SocketException,SocketTimeoutException,Exception 	{
    	
    	String request = exchange.getIn().getBody(String.class);
    	Map<String,Object>reqHeadersList = exchange.getIn().getHeaders();
    	String url = "";
    	String response = "";
    	
    	if(reqHeadersList.containsKey("requestURL"))
    	{
    		url = exchange.getIn().getHeader("requestURL").toString();
    	}
    	else if(reqHeadersList.containsKey("mapperQuoteURL"))
    	{
    		url = exchange.getIn().getHeader("mapperQuoteURL").toString();
    	}
    	
    	log.info("Web Service URL : "+url);
    	
    	if(exchange.getIn().getHeader("CamelHttpMethod").toString().equalsIgnoreCase("GET"))
    	{
    		log.info("Invoking GET Service call");
    		response = invoke.sendGETHTTPRequest(request, url, reqHeadersList);

    	}
    	else if(exchange.getIn().getHeader("CamelHttpMethod").toString().equalsIgnoreCase("PUT"))
    	{
    		log.info("Invoking PUT Service call");
    		response = invoke.sendHTTPPUTRequest(request, url, reqHeadersList);

    	}
    	else
    	{	if(exchange.getIn().getHeader("webserviceType").toString().equalsIgnoreCase("REST"))//webserviceType
    	{
    		response = invoke.sendRESTHTTPRequest(request, url, reqHeadersList);
    	}
    	else
    	{
    		response = invoke.sendHTTPSOAPRequest(request, url, reqHeadersList);
    	}
    }	
    	exchange.getIn().setBody(response);
    	
    }


}
