package com.idep.listener.services;

import com.couchbase.client.java.document.json.JsonObject;

public interface IProductaMetaData {
	JsonObject getRTODetails(String rtoCode,String carrierId, String businessLineId);
	JsonObject getVehicleDetails(String variantId,String carrierId);
	JsonObject getOccupationDetails(String occupationDetails,String carrierId);
	JsonObject getBlockedRTODetails(String rtoCode);
	JsonObject getValidationDetails(String carrierId,String productId);
	JsonObject getCarValidationDetails(String carrierId,String productId);
	JsonObject getPreInsurerMapping(String carrierId,String preInsurerCarrierId);
	JsonObject getNomineeRelMapping(String carrierId, String nominationRelationId);
	JsonObject getDistrictMapping(String carrierId, String city);
	JsonObject getExshowroomPriceDetails(String carrierId, String businessLineId, String modelCode, String stateGroupId);
	JsonObject getRTODetailsForZone(String RTOCode);
}
