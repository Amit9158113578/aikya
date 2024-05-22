package com.idep.service.vehicleinfo.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.service.vehicleinfo.VehicleResponseFormat;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

public class VehicleInfoResponseFormatter implements Processor {
	Logger log = Logger.getLogger(VehicleInfoRequest.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void process(Exchange exchange) throws Exception {
		log.info("Inside VehicleInfoResponseFormatter Started");
		String vehicleInfoResponse = exchange.getIn().getBody(String.class);
		JsonNode vehicleInfo = this.objectMapper.readTree(vehicleInfoResponse);
		log.info("Inside VehicleInfoResponseFormatter - vehicleInfo : " + vehicleInfo);
		ObjectNode requestDocNode = this.objectMapper.createObjectNode();
		requestDocNode.put(VehicleInfoConstant.VEHICLE_DETAILS, vehicleInfo.get("carrierRequestForm"));
		exchange.getIn().setBody(this.objectMapper.writeValueAsString(VehicleResponseFormat.createResponse(1000, "success", requestDocNode)));
	}
}
