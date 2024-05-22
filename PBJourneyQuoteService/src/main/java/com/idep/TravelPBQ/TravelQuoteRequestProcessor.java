package com.idep.TravelPBQ;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.BikePBQService.BikeQuoteRequestProcessor;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class TravelQuoteRequestProcessor implements Processor{
	Logger log = Logger.getLogger(BikeQuoteRequestProcessor.class.getName());
	static ObjectMapper objectMapper = new ObjectMapper();
	static CBService serverConfig = null ;
	static JsonNode defaultQuoteParam =null;
	
	
	static{
		if(serverConfig==null){
			serverConfig = CBInstanceProvider.getServerConfigInstance();
		}
		try {
			defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultTravelQuoteParam").content().toString());
		} catch (Exception e) {
			Logger.getLogger(BikeQuoteRequestProcessor.class.getName()).error("unable to fetch Deafult travel quote param document : ",e);
		}		
	}
	
	@Override
	public void process(Exchange exchange) throws Exception {
		try{
			JsonNode PBQReqNode = objectMapper.readTree(exchange.getIn().getBody(String.class));
			exchange.setProperty("PBQuoteRequest", PBQReqNode);
			((ObjectNode)PBQReqNode).put("requestType", "PBQuoteRequest");
			if(!PBQReqNode.has("lob")){
				((ObjectNode)PBQReqNode).put("lob", "Travel");
			}
			if(defaultQuoteParam!=null){
				((ObjectNode)PBQReqNode).put("defaultQuoteParam",defaultQuoteParam);
			}else{
				defaultQuoteParam = objectMapper.readTree(serverConfig.getDocBYId("defaultLifeQuoteParam").content().toString());
				((ObjectNode)PBQReqNode).put("defaultQuoteParam",defaultQuoteParam);
			}
			exchange.getIn().setBody(PBQReqNode);
	
		}catch(Exception e){
			log.error("unable to process Request : ",e);
		}
	}
	}


