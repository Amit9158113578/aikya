package com.idep.proposal.req.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;


public class XMLCharEmitter implements Processor {
	
	  Logger log = Logger.getLogger(XMLCharEmitter.class.getName());

	@Override
	public void process(Exchange exchange) throws Exception {

		try {
			
		  String request  = exchange.getIn().getBody(String.class);
		  log.info("input XML request in XMLCharEmitter : "+request);
		  String modifiedRequest = request.replaceAll("<o>","")
				  .replaceAll("</o>", "")
				  .replaceAll("<wsGeneratePolicyNumIO>","")//For cigna removed <wsGeneratePolicyNumIO> tag from request
				  .replaceAll("</wsGeneratePolicyNumIO>", "")
				  .replaceAll("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
		  log.info("modified XML request in XMLCharEmitter : "+modifiedRequest);
		 
		  //modifiedRequest = request.replaceAll("<wsGeneratePolicyNumIO>","");
		  //modifiedRequest = modifiedRequest.replaceAll("</wsGeneratePolicyNumIO>", "");
		  exchange.getIn().setBody(modifiedRequest);
			
		}
		catch(Exception e)
		{
			log.error("Exception at XMLCharEmitter : ");
		}
		  
	}

}
