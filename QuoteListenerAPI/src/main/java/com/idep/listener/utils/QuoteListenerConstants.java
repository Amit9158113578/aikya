package com.idep.listener.utils;

public class QuoteListenerConstants {
	public static final String DOCIDVALUESEPERATOR 			= "-";
	public static final String OCCUPATIONMAPPING_TAG		= "OccupationMapping";
	public static final String OCCUPATION_ID				= "occupationId";
	public static final String RTO_CODE						= "RTOCode";
	public static final String CARRIER_RTO_INFO 			= "carrierRTOInfo";
	public static final String ZONE_DETAILS 			     = "zoneDetails";
	public static final String CARRIER_EXSHOWROOM_INFO 		= "carrierEXSHOWROOMInfo";
	public static final String BLOCKEDRTODOCTYPE			="BlockedRTO";
	public static final String CARQUOTEVALIDATIONDOCTYPE	="CarQuoteValidation";
	public static final String BIKEQUOTEVALIDATIONDOCTYPE	="BikeQuoteValidation";
	public static final String VEHICLEBLOCKEDKEY 			= "vehicleBlocked";
	public static final String VEHICLERTOBlOLCKED 			= "vehicleRTOBlocked";
	public static final String ISRTOBLOCKED 			      = "isRTOBlocked";
	public static final Integer VALIDATIONFAILEDCODE 		=1048;
	public static final Integer SUCCESSCODE 				=1000;
	public static final String MSG_VALIDATIONFAIL			="validation failed.";
	public static final String MSG_SUCCESS 					="validation success";
	public static final String CORRELATION_ID 				= "uniqueKey";
	public static final String QUOTE_RES_CODE 				= "responseCode";
	public static final String QUOTE_RES_MSG 				= "message";
	public static final String QUOTE_RES_DATA 				= "data";
	public static final String QUOTE_ID 					= "QUOTE_ID";
	
	//Vehicle fields from mapping documents
	public static final String DB_MAKE						="make";
	public static final String DB_MAKECODE					="makeCode";
	public static final String DB_MODEL						="model";
	public static final String DB_MODELCODE					="modelCode";
	public static final String DB_VARIANT					="variant";
	public static final String DB_VARIANTCODE				="variantCode";
	public static final String DB_CUBICCAPACITY				="cubicCapacity";
	public static final String DB_SEATINGCAPACITY			="seatingCapacity";
	public static final String DB_VEHICLEBLOCKED			="vehicleBlocked";
	public static final String DB_FUELTYPE 					="fuelType";
	public static final String DB_VALIDATION 				="validations";
	public static final String DB_VEHICLEINFONODE 			="carrierVehicleInfo";
	public static final String DB_VALIDATIONID 				="validationId";
	
	public static final String DB_RTODETAILS 				="RTODetails";
	//Field names from UI
	public static final String UI_VARIANTID 				="variantId";
	public static final String UI_PRODUCTID 				="productId";
	public static final String UI_PRODUCTINFO 				="productInfo";
	public static final String UI_CARRIERID					="carrierId";
	public static final String UI_RTOCODE					="RTOCode";
	public static final String UI_CARINSURERMAPPING			="CarInsurerMapping";
	public static final String UI_CARRELATIONSHIPMAPPING	="RelationshipMapping";
	public static final String UI_DISTRICTMAPPING			="DistrictMapping";
	public static final String UI_QUOTETYPE					="quoteType";
	public static final String UI_EXSHOWROOMPRICE			="ExShowRoomPrice";
	
	//Validation Nubers
	public static final Integer V_BLOCKEDVEHICLE			=1;
	public static final Integer V_BLOCKEDRTO				=2;
	public static final Integer V_BLOCKEVEHICLEBYRTO	    =3;
	public static final String BUSINESSSLINEID     	        ="3";
	//Node names
	public static final String ND_QUOTEPARM					="quoteParam";
	public static final String ND_VEHICLEINFO				="vehicleInfo";
	public static final String ND_INPUTMESSAGE 				= "inputMessage";
	public static final String ND_OCCUPATIONINFO            = "occupationInfo";
	public static final String ND_QUOTEPARM_RIDERS			= "riders";
	public static final String ND_QUOTEPARM_RIDER_ID		= "riderId";
	public static final String BLOCK_RIDERS					= "blockedRiders";
	
	
	public static final String CITY					        = "city";
	public static final String ZONE					        = "zone";
}
