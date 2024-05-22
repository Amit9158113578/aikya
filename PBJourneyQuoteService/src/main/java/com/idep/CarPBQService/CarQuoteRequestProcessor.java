package com.idep.CarPBQService;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.PBQService.LifeRequestProcessor;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

/***
 * 
 * @author kuldeep.patil
 *
 */
public class CarQuoteRequestProcessor implements Processor {

	Logger log = Logger.getLogger(CarQuoteRequestProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null ;
	static JsonNode defaultQuoteParam =null;
	
	
	static{
		if(serverConfig==null){
			serverConfig = CBInstanceProvider.getServerConfigInstance();
		}
		try {
			defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultCarQuoteParam").content().toString());
		} catch (Exception e) {
			Logger.getLogger(CarQuoteRequestProcessor.class.getName()).error("unable to fetch Deafult car quote param document : ",e);
		}		
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
		String reqString = exchange.getIn().getBody().toString();
		JsonNode requestNode = objectMapper.readTree(reqString);
		exchange.setProperty("PBQuoteRequest", requestNode);
		((ObjectNode)requestNode).put("requestType", "PBQuoteRequest");
		if(defaultQuoteParam!=null){
			((ObjectNode)requestNode).put("defaultQuoteParam",defaultQuoteParam);
		}else{
			defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultCarQuoteParam").content().toString());
			((ObjectNode)requestNode).put("defaultQuoteParam",defaultQuoteParam);
		}
			exchange.getIn().setBody(requestNode);
		}catch(Exception e){
			log.error("unable to process Request : ",e);
		}
	}
	}


