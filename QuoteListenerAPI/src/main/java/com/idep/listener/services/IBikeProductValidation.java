/**
 * 
 */
package com.idep.listener.services;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author vipin.patil
 *
 */
public interface IBikeProductValidation {
	boolean isRTOBlocked(String RTOCode,String carrierId);
	boolean isVehicleBlocked(JsonObject vehicleDetails,String variantId,String carrierId);
	public ObjectNode isRiderBlocked(ObjectNode input , JsonNode vehicleDetailsNode , JsonNode rtoDetailsNode );
	
	public boolean isSegmentTypeBlock(String isBlocked, String segemntType, String isAllow);
	
	

}
