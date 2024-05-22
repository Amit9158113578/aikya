package com.idep.urlshortner.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.polr.service.PolrService;
import com.idep.urlshortner.util.URLShortnerConstant;

public class URLShortnerResProcessor implements Processor{
	PolrService polrService = new PolrService();
	public void process(Exchange exchange) throws Exception {
		Logger log = Logger.getLogger(URLShortnerResProcessor.class.getName());
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode shortURLRequest = objectMapper.createObjectNode();
		JsonNode p365ShortnerResponse = objectMapper.createObjectNode();
		JsonNode responseNode = objectMapper.createObjectNode();
		String request = exchange.getIn().getBody().toString();
		JsonNode reqNode = objectMapper.readTree(request);
		log.info("URL Shortner request :"+reqNode);
		if(reqNode.has("longURL") && reqNode.get("longURL") != null){
			((ObjectNode) shortURLRequest).put("url",reqNode.get("longURL").asText());
			((ObjectNode) shortURLRequest).put("is_secret",reqNode.get("is_secret").asBoolean());
			p365ShortnerResponse = polrService.getShortURL(shortURLRequest);
		}
		if(p365ShortnerResponse == null || p365ShortnerResponse.get(URLShortnerConstant.SHORT_URL).asText().equalsIgnoreCase(URLShortnerConstant.ERROR)){
			((ObjectNode) responseNode).put("responseCode",URLShortnerConstant.FAILURE_CODE);
			((ObjectNode) responseNode).put("message",URLShortnerConstant.FAILURE_MESSAGE);
			((ObjectNode) responseNode).put("data","");
		}else{
			JsonNode responseDataNode = objectMapper.createObjectNode();
			((ObjectNode) responseDataNode).put(URLShortnerConstant.SHORT_URL,p365ShortnerResponse.get(URLShortnerConstant.SHORT_URL).asText());
			((ObjectNode) responseNode).put("responseCode",URLShortnerConstant.SUCCESS_CODE);
			((ObjectNode) responseNode).put("message",URLShortnerConstant.SUCCESS_MESSAGE);
			((ObjectNode) responseNode).put("data",responseDataNode);
		}
		exchange.getIn().setBody(responseNode);
	}
}
