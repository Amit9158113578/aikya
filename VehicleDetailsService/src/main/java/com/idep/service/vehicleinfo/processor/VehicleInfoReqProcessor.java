package com.idep.service.vehicleinfo.processor;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;
import com.idep.service.vehicleinfo.VehicleResponseFormat;
import com.idep.service.vehicleinfo.impl.VehicleInfoResponseFormatter;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

/**
 * @author pravin.jakhi
 *
 */
public class VehicleInfoReqProcessor implements Processor {

	Logger log = Logger.getLogger(VehicleInfoReqProcessor.class.getName());
	ObjectMapper objectMapper = new ObjectMapper();
	CBService policy = CBInstanceProvider.getPolicyTransInstance();
	
	@Override
	public void process(Exchange exchange) throws Exception {
	
		try{
			String vehicleInfoReq = exchange.getIn().getBody(String.class);
			JsonNode vehicleInfoReqNode = objectMapper.readTree(vehicleInfoReq);
			
			if(!vehicleInfoReqNode.get(VehicleInfoConstant.VEHICALENUMBER).asText().equalsIgnoreCase("")){
				
				exchange.setProperty(VehicleInfoConstant.VEHICALENUMBER, vehicleInfoReqNode.get(VehicleInfoConstant.VEHICALENUMBER).asText());
				JsonDocument docBYId = policy.getDocBYId(vehicleInfoReqNode.get(VehicleInfoConstant.VEHICALENUMBER).asText());
				if(docBYId!=null)
				{
					JsonNode registrationNumberDoc = objectMapper.readTree(docBYId.content().toString());
					if(registrationNumberDoc.has(VehicleInfoConstant.UIRESPONSE))
					{
						((ObjectNode)vehicleInfoReqNode).put(VehicleInfoConstant.UIRESPONSE, registrationNumberDoc.get(VehicleInfoConstant.UIRESPONSE));
						exchange.getIn().setHeader("carInfoPresent", "true");
						exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1000);
					}
					else
					{
						exchange.setProperty("lob", vehicleInfoReqNode.get("lob").textValue());
						exchange.getIn().setHeader("carInfoPresent", "false");
					}
				}
				else
				{
					exchange.setProperty("lob", vehicleInfoReqNode.get("lob").textValue());
					exchange.getIn().setHeader("carInfoPresent", "false");
				}
				exchange.getIn().setBody(vehicleInfoReqNode.toString());
				log.info("Car Rto Details Document fetch query :  "+vehicleInfoReqNode);
			
		/**
		 * Header in set the request Document for Mapper 
		 * */
			exchange.getIn().setHeader("documentId", VehicleInfoConstant.VEHICLEREQCONFIGREQDOC);
			exchange.setProperty("vehicleInpReq", vehicleInfoReqNode);
			}	
		}catch(Exception e){
			ObjectNode responseNode = objectMapper.createObjectNode();
			exchange.getIn().setHeader(VehicleInfoConstant.VEHICLE_RES_CODE,1001);
			  responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, VehicleInfoConstant.VEHICLE_RES_FAILED_CODE);
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, "failure");
			    responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA, exchange.getIn().getBody(String.class));
				exchange.getIn().setBody(objectMapper.writeValueAsString(responseNode));
			log.error("Error at VehicleInfoReqProcessor :",e);
		}
	}

}
