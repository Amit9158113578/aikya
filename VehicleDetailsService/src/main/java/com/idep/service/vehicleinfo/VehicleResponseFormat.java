package com.idep.service.vehicleinfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idep.service.vehicleinfo.util.VehicleInfoConstant;

public class VehicleResponseFormat {
	public static ObjectNode createResponse(int responseCode, String message, JsonNode node)
	  {
	    JsonNodeFactory factory = new JsonNodeFactory(true);
	    ObjectNode responseNode = new ObjectNode(factory);
	    responseNode.put(VehicleInfoConstant.VEHICLE_RES_CODE, responseCode);
	    responseNode.put(VehicleInfoConstant.VEHICLE_RES_MSG, message);
	    responseNode.put(VehicleInfoConstant.VEHICLE_RES_DATA, node);
	    return responseNode;
	  }
}
