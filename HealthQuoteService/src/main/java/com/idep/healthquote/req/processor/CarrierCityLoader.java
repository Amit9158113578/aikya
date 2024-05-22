package com.idep.healthquote.req.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.healthquote.util.HealthQuoteConstants;


public class CarrierCityLoader implements Processor {

	Logger log = Logger.getLogger(CarrierCityLoader.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			List<JsonObject> carrierCityList=null;
			String inputReq=exchange.getIn().getBody(String.class);
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			int carrierId=inputReqNode.findValue("carrierId").asInt();
			String pincode=inputReqNode.get("personalInfo").get("pincode").asText();
			String city=inputReqNode.get("personalInfo").get("city").asText();
			JsonArray paramobj = JsonArray.create();
			paramobj.add(carrierId);
			paramobj.add(pincode.toString());
			paramobj.add(city.toLowerCase().toString());
			log.info("CITY QUERY PARAM : "+paramobj);
			carrierCityList= serverConfigService.executeConfigParamArrQuery(HealthQuoteConstants.CARRIERCITYQUERY,paramobj);
			log.info("CITY QUERY OUTPUT : "+carrierCityList);
			if(carrierCityList!=null &&  carrierCityList.size()!=0){
				JsonNode cityDetails = (objectMapper.readTree(carrierCityList.get(0).toString()));
				((ObjectNode)inputReqNode.get("personalInfo")).put("city",cityDetails.get("zone"));
				log.info("Carrier City Set for : "+inputReqNode);
			}else{
				
				log.info("Carrier City not found  for  pincode : "+pincode+" : "+inputReq);
				
			}
			exchange.getIn().setBody(inputReqNode);
			
		}catch(Exception e){
			log.error("Exception occure at CarrierCityLoader : ",e);
		}
		
		
		
	}

}
