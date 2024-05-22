package com.idep.proposal.carrier.req.processor;

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
import com.idep.proposal.carrier.req.processor.HDFCErgoHealthPlanCodeProcessor;
import com.idep.proposal.util.HealthGenericFunction;
import com.idep.proposal.util.ProposalConstants;

public class CarrierCityLoader implements Processor {

	Logger log = Logger.getLogger(CarrierCityLoader.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService serverConfigService = CBInstanceProvider.getServerConfigInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		try{
			List<JsonObject> carrierCityList=null;
			String inputReq=exchange.getIn().getBody(String.class);
			ArrayList<Integer> ageCount = null;	
			JsonNode inputReqNode = objectMapper.readTree(inputReq);
			int carrierId=inputReqNode.findValue("carrierId").asInt();
			String pincode=inputReqNode.get("proposerInfo").get("personalInfo").get("pincode").asText();
			String city=inputReqNode.get("proposerInfo").get("personalInfo").get("city").asText();
			JsonArray paramobj = JsonArray.create();
			paramobj.add(carrierId);
			paramobj.add(pincode.toString());
			paramobj.add(city.toLowerCase().toString());
			log.info("CarrierCityLoader  QUERY PARAM : "+paramobj);
			carrierCityList= serverConfigService.executeConfigParamArrQuery(ProposalConstants.CARRIERCITYQUERY,paramobj);
			log.debug("CITY QUERY OUTPUT : "+carrierCityList);
			if(carrierCityList!=null &&  carrierCityList.size()!=0){
				
				JsonNode cityDetails = (objectMapper.readTree(carrierCityList.get(0).toString()));
				
				((ObjectNode)inputReqNode.get("proposerInfo").get("personalInfo")).put("city",cityDetails.get("zone"));
				((ObjectNode)inputReqNode.get("proposerInfo").get("contactInfo")).put("city",cityDetails.get("zone"));
				((ObjectNode)inputReqNode.get("proposerInfo").get("permanentAddress")).put("city",cityDetails.get("zone"));
				
				log.debug("Carrier City Set for : "+inputReq);
			}else{
				
				log.info("Carrier City not found  for  pincode : "+pincode+" : "+inputReq);
				
			}
			exchange.getIn().setBody(inputReqNode);
			
		}catch(Exception e){
			log.error(exchange.getProperty(ProposalConstants.LOG_REQ).toString()+ProposalConstants.SERVICEINVOKE+"|ERROR|Exception at CarrierCityLoader|",e);
		}
		
		
		
	}

}
