package com.idep.posp.request;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class ConfigServiceUrlProcessor implements Processor {
	
	ObjectMapper objectMapper = new ObjectMapper();
	Logger log = Logger.getLogger(CreateMoodleUser.class.getName());
	CBService ServerCofig = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			String request = exchange.getIn().getBody(String.class).toString();
			request = request.replaceAll("5B", "[").replaceAll("5D", "]");
			JsonNode reqNode = objectMapper.readTree(request); 
			JsonNode moodleConfig =null;
			if(ServerCofig!=null){
				moodleConfig = objectMapper.readTree(ServerCofig.getDocBYId("POSPExternalServiceUrl").content().toString());
			}
			String serviceUrl= moodleConfig.get("moodleServiceUrl").asText();
			if(exchange.getProperty("documentId")!=null){
			serviceUrl = serviceUrl.replace("<wstoken>", moodleConfig.get("wstoken").asText())
						.replace("<wsfunction_name>", moodleConfig.get("functionList").get(exchange.getProperty("documentId").toString()).asText());
			}else{
				log.info("unable to find documentId in property :"+reqNode);
			}
				log.info("modle service url generated : "+serviceUrl);
			/**
			 * request url updating for WebConsumer
			 * */
				log.info("modle service url generated Request : "+reqNode);
			exchange.getIn().setHeader("requestURL", serviceUrl);
			exchange.getIn().setBody(reqNode);
		}catch(Exception e){
			log.error("unable to configure url for moodle service : ",e);
		}
	}
}
