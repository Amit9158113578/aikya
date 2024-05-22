package com.idep.service.vehicleinfo.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

public class VehicleInfoResponseHandler implements Processor {
	Logger log = Logger.getLogger(VehicleInfoResponseHandler.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {
		log.info("Inside VehicleInfoResponseHandler - response creation Started");
		String vehicleInfoResponse = exchange.getIn().getBody(String.class);
		JsonNode vehicleInfo = this.objectMapper.readTree(vehicleInfoResponse);
		log.info("Inside VehicleInfoResponseHandler - vehicleInfo : " + vehicleInfo);

		int carrierResponseStatus = vehicleInfo.get("response").get("status").asInt();

		if(carrierResponseStatus == 100){
			ObjectNode requestDocNode = this.objectMapper.createObjectNode();
			JsonNode carrierResponse = vehicleInfo.get("response").get("result").get("vehicle");
			log.info("Vehicle Info request sent to Mapper : " + carrierResponse);
			requestDocNode.put(VehicleInfoConstant.VEHICLE_MAPPER_CARRIER_RESP_KEY, carrierResponse);
			exchange.setProperty(VehicleInfoConstant.VEHICLE_MAPPER_CONFIG_KEY, VehicleInfoConstant.VEHICLERESCONFIGDOCUMENT);
			exchange.getIn().setBody(this.objectMapper.writeValueAsString(requestDocNode));
		}else{
			log.info("Inside VehicleInfoResponseHandler - Error : " + carrierResponseStatus);
		}

		log.info("Inside VehicleInfoResponseHandler - response creation Completed");
	}
}
