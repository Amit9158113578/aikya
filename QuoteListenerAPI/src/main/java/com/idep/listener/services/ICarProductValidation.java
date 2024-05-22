package com.idep.listener.services;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ICarProductValidation {
	boolean isRTOBlocked(String RTOCode,String carrierId);
	boolean isVehicleBlocked(String variantId,String carrierId);
	public ObjectNode isRiderBlocked(ObjectNode input , JsonNode vehicleDetailsNode , JsonNode rtoDetailsNode );
	boolean isVarientBlockedByRTO(JsonObject vehicleDetailsNode , JsonObject rtoDetailsNode );
	public String findMaritalStatus(String gender, String maritalStatus);
}
