package com.idep.healthquote.carrier.req.processor;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.healthquote.util.HealthQuoteConstants;

public class MaxBupaXmlTagRemoveProcessor implements Processor {

	
	 Logger log = Logger.getLogger(MaxBupaXmlTagRemoveProcessor.class.getName());
	  ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
		
		 JsonNode configData = exchange.getProperty(HealthQuoteConstants.CARRIER_QUOTE_REQ_MAP_CONF,JsonNode.class);
		  log.info("configData : "+configData);
		  String quoteRequest = exchange.getIn().getBody(String.class); 
		  if(configData.get("carrierSOAPConfig").get("reqConfig").has("removeAttrList")){
			                 
			
			  HashMap<String,String> replacevalmap = objectMapper.readValue(configData.get("carrierSOAPConfig").get("reqConfig").get("removeAttrList").toString(), HashMap.class);
				 log.debug("MAP HelProposalAttribute Request  : "+replacevalmap.toString());
			  for (Map.Entry<String, String> entry : replacevalmap.entrySet()) {
				  quoteRequest=quoteRequest.replace(entry.getKey(), entry.getValue());
				}
			  log.info("After Remove Tag Final Soap request : "+quoteRequest);
		  }
		  
		  
		  exchange.getIn().setBody(quoteRequest);
	}

}
