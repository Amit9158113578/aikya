package com.idep.service.vehicleinfo.res;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.service.vehicleinfo.processor.VehicleInfoReqProcessor;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

public class VehicleInfoResposneProcessor implements Processor {
	Logger log = Logger.getLogger(VehicleInfoResposneProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	@Override
	public void process(Exchange exchange) throws Exception {
		ObjectNode responseNode = objectMapper.createObjectNode();
	try{
		JsonNode reqBody =  objectMapper.readTree(exchange.getIn().getBody(String.class));
		log.info("reqBody final Body : "+reqBody);
		if(exchange.getIn().getHeader(VehicleInfoConstant.VEHICLE_RES_CODE).toString().equalsIgnoreCase("1000")){
		log.info("Status Code / ResponseCode in Success  : "+exchange.getIn().getHeader(VehicleInfoConstant.VEHICLE_RES_CODE));
		responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, exchange.getIn().getHeader(VehicleInfoConstant.VEHICLE_RES_CODE).toString());
	    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, "success");
	    if(exchange.getProperty("p365CarVariantMaster")!=null)
	    {
	    	 JsonNode readTree = objectMapper.readTree(exchange.getProperty("p365CarVariantMaster").toString());
	    	((ObjectNode)reqBody.get("UIResponse")).put("veriantData",readTree);
	    }
	      responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA, reqBody.get("UIResponse"));
		}
		else
		{
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, VehicleInfoConstant.VEHICLE_RES_FAILED_CODE);
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, "failure");
			    if(reqBody.has(VehicleInfoConstant.CAR_REG_API_RESPONSE))
			    {
			       ((ObjectNode)reqBody).remove(VehicleInfoConstant.CAR_REG_API_RESPONSE);
			    }
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA,reqBody);
		}
		log.info("responseNode : "+responseNode);
		exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
		}catch(Exception e){
			log.error("Error At VehicleInfoResposneProcessor :  ",e);
		}
	}
}
