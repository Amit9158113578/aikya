package com.idep.PBQ.updateReq;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PBQuoteUpdateRequestProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(PBQuoteUpdateRequestProcessor.class.getName());
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			
			JsonNode request = objectMapper.readTree(exchange.getIn().getBody(String.class));
			
			if(request.has("quoteParam")){
				if(request.get("quoteParam").has("quoteType")){
					if(request.get("quoteParam").get("quoteType").asText().equalsIgnoreCase("1")){
						exchange.getIn().setHeader("quoteType", "life");	
						exchange.setProperty("ConfigdocId", "LifeQuoteUpdateRequestConfig");
					}else if(request.get("quoteParam").get("quoteType").asText().equalsIgnoreCase("2")){
						exchange.getIn().setHeader("quoteType", "bike");	
						exchange.setProperty("ConfigdocId", "BikeQuoteUpdateRequestConfig");
					}else if(request.get("quoteParam").get("quoteType").asText().equalsIgnoreCase("3")){
						exchange.getIn().setHeader("quoteType", "car");	
						exchange.setProperty("ConfigdocId", "CarQuoteUpdateRequestConfig");
					}else if(request.get("quoteParam").get("quoteType").asText().equalsIgnoreCase("4")){
						exchange.getIn().setHeader("quoteType", "health");	
						exchange.setProperty("ConfigdocId", "HealthQuoteUpdateRequestConfig");
					}else if(request.get("quoteParam").get("quoteType").asText().equalsIgnoreCase("5")){
						exchange.getIn().setHeader("quoteType", "travel");	
						exchange.setProperty("ConfigdocId", "TravelQuoteUpdateRequestConfig");
					}
				}
			}
		}catch(Exception e){
			log.error("Unable to process request : ",e);
		}
	}
}
