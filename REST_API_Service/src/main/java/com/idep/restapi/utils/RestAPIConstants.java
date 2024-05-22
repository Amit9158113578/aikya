package com.idep.restapi.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idep.couchbase.api.impl.CBInstanceProvider;
import com.idep.couchbase.api.impl.CBService;

public class RestAPIConstants {
	public static ObjectMapper objectMapper = new ObjectMapper();

	public static CBService serverConfig = CBInstanceProvider.getServerConfigInstance();

	public static final String CARRIER_ID = "carrierId";

	public static final String POLICY_TYPE = "policyType";

	public static final String ODPREMIUM = "odpremium";

	public static final String TPPREMIUM = "tppremium";

	public static final String PAID_DRIVER_COVER = "paidDriverCover";

	public static final String BASIC_COVERAGE = "basicCoverage";

	public static final String TOTAL_DISCOUNT_AMOUNT = "totalDiscountAmount";

	public static final String RIDERS_LIST = "ridersList";

	public static final String TOTAL_RIDER_AMOUNT = "totalRiderAmount";

	public static final String DISCOUNT_LIST = "discountList";

	public static final String DISCOUNT_AMOUNT = "discountAmount";

	public static final String RIDER_VALUE = "riderValue";

	public static final String NET_PREMIUM = "netPremium";

	public static final String SERVICE_TAX = "serviceTax";

	public static final String GROSS_PREMIUM = "grossPremium";

	public static final String INPUT_REQUEST = "inputRequest";

	public static final String LOB = "lob";

	public static final String PRODUCT_ID = "productId";

	public static final String STAGE = "stage";

	public static final String CARRIER_RESPONSE = "carrierResponse";

	public static final String CONFIGURATION = "configuration";

	public static final String DOCUMENT_FOUND = "documentFound";

	public static final String JOLT_REQUEST_PREFIX = "JOLTRequest-";

	public static final String JOLT_RESPONSE_PREFIX = "JOLTResponse-";

	public static final String REST_REQUEST_PREFIX = "RESTRequest-";

	public static final String REST_RESPONSE_PREFIX = "RESTResponse-";

	public static final String SchemaValidation_PREFIX = "SchemaValidation-";

	public static final String REQUEST = "request";

	public static final String BOOL_FLAG_TRUE = "True";

	public static final String BOOL_FLAG_FALSE = "False";

	public static final String QUOTE = "Quote";

	public static final String URL = "url";

	public static final String JSON = "JSON";

	public static final String CARRIER_DATA = "carrierData";

	public static final String HEADERS = "headers";

	public static final String DYNAMIC_HEADERS = "dynamicHeaders";

	public static final String FAILURE = "Failure";

	public static final String SUCCESS = "Success";

	public static final String RESPONSE_CODE = "responseCode";

	public static final String SUCCESS_RESPONSE_CODE = "P365RES100";

	public static final String SUCCESS_RES = "successRes";

	public static final String REST_DATA_TYPE_SMALL = "restDataType";

	public static final String XML = "XML";

	public static final String REST_XML_CONFIGURATION = "RESTXMLConfiguration";

	public static final String SUB_STAGE = "subStage";

	public static final String CONFIG_DOCUMNET_FOUND = "configDocumentFound";

	public static final String REPLACE_TAGS = "replaceTags";

	public static final String REPLACE_TO = "replaceTo";

	public static final String REPLACE_WITH = "replaceWith";

	public static final String RESPONSE_TAG_NAME = "responseTagName";

	public static final String CONVERT_TO_JSON = "convertToJson";

	public static final String VALIDATION_RESPONSE_NODE = "validateResponseNode";

	public static final String SUCCESS_RESPONSE_KEY = "successResponseKey";

	public static final String SUCCESS_RESPONSE_VALUE = "successResponseValue";

	public static final String DATA_TYPE = "datatype";

	public static final String DROOL_RESPONSE = "droolResponse";

	public static final String DROOL_RESPONSE_VALIDATE_KEY = "droolResponseValidateKey";

	public static final String DROOL_RESPONSE_NODE = "droolResponseNode";

	public static final String RESPONSE_MESSAGES = "ResponseMessages";

	public static final String CONFIG_DOC_MISSING_CODE = "ConfigDocMissingCode";

	public static final String CONFIG_DOC_MISSING_MSG = "ConfigDocMissingMsg";

	public static final String STATUS = "status";

	public static final String MESSAGE = "message";

	public static final String RESPONSE_CODE_FAILURE = "ResponseCodeFailure";

	public static final String FAILURE_MSG = "failureMsg";

	public static final String PROPOER_RES_NOT_RECEIVED_CODE = "properResponseNotReceivedCode";

	public static final String PROPOER_RES_NOT_RECEIVED_MSG = "properResponseNotReceivedMsg";

	public static final String REST_DATA_TYPE = "RestDataType";

	public static final String BUCKET_QUOTEDATA = "QuoteData";

	public static final String BIKE_CARRIER_IDV_CALC_CONFIG = "BikeCarrierIDVCalcConfig";

	public static final String INSURED_DECLARE_VALUE = "insuredDeclareValue";

	public static final String VEHICLE_INFO = "vehicleInfo";

	public static final String IDV = "IDV";

	public static final String BEST_QUOTE_ID = "best_quote_id";

	public static final String BIKE_QUOTE_RESPONSE = "bikeQuoteResponse";

	public static final String PRODUCT_INFO = "productInfo";

	public static final String DATA = "data";

	public static final String QUOTES = "quotes";

	public static final String MIN_IDV_VALUE = "minIdvValue";

	public static final String MAX_IDV_VALUE = "maxIdvValue";

	public static final String MINUS_IDV_PERC_VEHICLE = "minusIDVPercVehicle";

	public static final String PLUS_IDV_PERC_VEHICLE = "plusIDVPercVehicle";

	public static final String BY_PASS_URL = "ByPassSSL";

	public static final String IS_REQUEST_TYPE_GET = "isRequestTypeGET";
	public static final String IS_METHOD_TYPE_GET = "methodType";

	public static final String CHANG_URL = "changeURL";
	public static final String GENERATE_UUID = "GenerateUUID";
	public static final String KEY = "key";
	public static final String GENERATE_JWT_TOKEN = "GenerateJWTToken";
	public static final String SECRET_KEY = "SecretKey";
	public static final String AUTH_KEY = "AuthKey";

	public static final String ENCRYPT_PAYLOAD = "encryptPayload";
	public static final String DECRYPT_PAYLOAD = "decryptPayload";

	public static final String REQUEST_KEY = "requestKey";

	public static final String IV = "IV";

}
