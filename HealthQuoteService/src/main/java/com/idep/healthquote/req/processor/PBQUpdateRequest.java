package com.idep.healthquote.req.processor;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PBQUpdateRequest {

	static Logger log = Logger.getLogger(PBQUpdateRequest.class.getName());
	
	public  static void sendPBQUpdateRequest(JsonNode request , Exchange exchange){
		
		try{
			CamelContext camelContext = exchange.getContext();
			ProducerTemplate template = camelContext.createProducerTemplate();
			String uri = "activemqSecondary:queue:pbqupdatereqQ";
			exchange.getIn().setBody(request.toString());
			exchange.setPattern(ExchangePattern.InOnly); // set exchange pattern
			template.send(uri, exchange);
			log.info("Updated for Car Quote request added in activemqSecondary:queue:pbqupdatereqQ : ");
			
		}catch(Exception e){
			log.error("unable to send request to activemqSecondary:queue:pbqupdatereqQ ",e);
		}
	}
	
/*	public static void main(String[] args) {
		ObjectMapper obj = new ObjectMapper();
		
		String req = "\"mappingConfig\":{\"N\":false,\"Y\":true}";
		
		try {
			JsonNode reqNode = obj.readTree("{\"commonInfo\":{\"address\":{\"pincode\":\"411025\"}}}");
			System.out.println("Data: "+reqNode);
			
			System.out.println("sads : "+reqNode.findParent("address").get("address").get("pincode").asText());
			
			JsonNode d = reqNode.findParent("address");
			//((ObjectNode)d.get("address")).put("pnicode", 411051);
			
			
			((ObjectNode)reqNode.findParent("address").get("address")).put("pnicode", 411051);
				/*if(reqNode.get("address").has("N")){
					
					
					System.out.println(reqNode.get("mappingConfig").get("N"));
				}
			System.out.println("reqNode upated : "+reqNode);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
