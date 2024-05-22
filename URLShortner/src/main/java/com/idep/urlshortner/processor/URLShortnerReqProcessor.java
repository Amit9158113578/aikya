package com.idep.urlshortner.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class URLShortnerReqProcessor implements Processor{
	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(URLShortnerReqProcessor.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("URL Shortner request :"+reqNode);
		if(reqNode.has("isSecret")){
			((ObjectNode) reqNode).put("is_secret", reqNode.get("isSecret").asBoolean());
		}else{
			((ObjectNode) reqNode).put("is_secret", true);
		}
		exchange.getIn().setBody(reqNode);
	}
}
