package com.idep.url.reroute.processor;

import java.util.HashMap;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.idep.url.reroute.util.SyncGatewayURLLoader;

public class ServerConfigReqHandler implements Processor {

Logger log = Logger.getLogger(ServerConfigReqHandler.class.getName());
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try {
			
				String request = exchange.getIn().getBody(String.class);
				log.info("server config sync request : "+request);
				log.info("Request Headers : "+exchange.getIn().getHeaders());
				/**
				 * form a URL based on configuration and received headers
				 */
				//"http://192.168.0.10:8095/serverconfig";
				//String url = SyncGatewayURLLoader.syncGateConfigAdminURL;
				String url = SyncGatewayURLLoader.syncGateConfigPublicURL;
				if(exchange.getIn().getHeader("CamelHttpPath")!=null)
				{
					String camelHttpPath = exchange.getIn().getHeader("CamelHttpPath").toString();
					url = url+camelHttpPath;
				}
				if(exchange.getIn().getHeader("CamelHttpQuery")!=null)
				{
					String camelHttpQuery =	exchange.getIn().getHeader("CamelHttpQuery").toString();
					url = url+"?"+camelHttpQuery;
				}
				 
				String camelHttpMethod = exchange.getIn().getHeader("CamelHttpMethod").toString();
				HashMap<String,Object> headers = new HashMap<String,Object>();
				exchange.getIn().setHeaders(headers);
				/**
				 * set required headers
				 */
				exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "application/json");
				exchange.getIn().setHeader(Exchange.HTTP_METHOD, camelHttpMethod);
				exchange.getIn().setHeader(Exchange.ACCEPT_CONTENT_TYPE, "application/json");
				
				log.info("Request Headers revised : "+exchange.getIn().getHeaders());
				exchange.getIn().setHeader("serverconfigurl", url);
				
			log.info("URL:::"+url);
		}
		catch(Exception e)
		{
			log.error(e);
		}

	}
}
