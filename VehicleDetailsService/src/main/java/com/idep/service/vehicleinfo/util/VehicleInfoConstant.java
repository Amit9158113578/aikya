package com.idep.service.vehicleinfo.util;

public class VehicleInfoConstant {
	public static final String VEHICLE_RES_CODE = "responseCode";
	public static final String VEHICLE_RES_MSG = "message";
	public static final String VEHICLE_RES_DATA = "data";
	public static final String VEHICLE_DETAILS = "vehicleDetails";
	public static final String CAMEL_HTTP_METHOD = "camelHttpMethod";
	public static final String CAMEL_HTTP_PATH = "camelHttpPath";
	public static final String CAMEL_HTTP_URI = "camelHttpUri";
	public static final String CAMEL_ACCEPT_CONTENT_TYPE = "CamelAcceptContentType";
	public static final String AUTH_HEADER = "Authorization";
	public static final String URL = "url";
	public static final String VEHICALENUMBER="registrationNumber";
	public static final String VEHICALENO="registrationNo";
	public static final String VEHICLEREQCONFIGDOCUMENT = "VehicleCarReqConfig";
	public static final String VEHICLEREQCONFIGREQDOC = "VehicleCarReqConfig";
	public static final String VEHICLERESCONFIGDOCUMENT = "VEHICLERTORESCONFIG";
	public static final String VEHICLE_MAPPER_CARRIER_RESP_KEY = "carrierResponse";
	public static final String VEHICLE_MAPPER_CONFIG_KEY = "carrierReqMapConf";
	public static final String DOCUMENTID="documentId";
	public static final Integer VEHICLE_RES_SUCCESS_CODE = 1000;
	public static final Integer VEHICLE_RES_FAILED_CODE = 1001;
	public static final String DOCUMENT_TYPE="carRtoDetails";
	public static final String DESCRIPTION="Description";
	public static final String REGISTRATIONYEAR="RegistrationYear";
	public static final String CARMAKE="CarMake";
	public static final String CARMODEL="CarModel";
	public static final String ENGINESIZE="EngineSize";
	public static final String MAKEDESCRIPTION="MakeDescription";
	public static final String MODELDESCRIPTION="ModelDescription";
	public static final String VEHICLEIDENTNUMBER="VechileIdentificationNumber";
	public static final String NOOFSEATS="NumberOfSeats";
	public static final String COLOUR="Colour";
	public static final String ENGINENUMBER="EngineNumber";
	public static final String FUELTYPE="FuelType";
	public static final String REGDATE="RegistrationDate";
	public static final String UIRESPONSE="UIResponse";
	public static final String LOCATION="Location";
	public static final String VARIANTID="variantId";
	public static final String VARIANT="variant";
	public static final String P365MODEL_VARIANT="modelVariant";
	public static final String P365MAKE_MODEL_VARIANT="makeModelVariant";
	public static final String CAR_REG_API_RESPONSE="registrationAPIResponse";
	public static final String MODEL="model";
	public static final String UMAKE="uMake";
	public static final String MAKE="make";
	public static final String CUBICCAPACITY="cubicCapacity";
	public static final String DISPLAYVEHICLE="displayVehicle";
	public static final String UFUELTYPE="fuelType";
	public static final String CARDETAILS="carDetails";
	public static final String QUERY="select s.* from ServerConfig s ";
	public static final String GETDOCQUERY="select pt.*,meta().id from ServerConfig pt where documentType='carRtoDetails' and registrationNo=$1  ";


	
	
	
	
}