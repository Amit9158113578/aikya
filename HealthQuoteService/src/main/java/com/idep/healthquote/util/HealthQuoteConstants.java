package com.idep.healthquote.util;

public class HealthQuoteConstants {
	
      public static final String DEFAULT_LOG = "defaultLog";
	  public static final String INPUT_MESSAGE = "inputMessage";
	  public static final String UNIQUE_KEY = "uniqueKey";
	  public static final String QUOTE_ID = "QUOTE_ID";
	  public static final String ENCRYPT_QUOTE_ID = "encryptedQuoteId";
	  public static final String BUSINESSLINE_ID = "businessLineId";
	  public static final String UI_QUOTEREQUEST = "healthQuoteInputRequest";
	  public static final String JMSCORRELATION_ID = "JMSCorrelationID";
	  public static final String CORRELATION_ID = "correlationId";
	  public static final String SOURCEQUEUENAME = "sourceQName";
	  public static final String HEALTH_CARRIERS_Q = "HealthCarrierQList";
	  public static final String RES_Q_LIST = "carrierResQ";
	  public static final String REQ_Q_LIST = "carrierReqQ";
	  public static final String QUOTE_VALIDATIONS = "QuoteValidations";
	  public static final String HEALTH_SUM_ASSURED_CALC = "HealthSumAssuredCalc";
	  public static final String MIN_BASIC_PREMIUM = "minBasicPremium";
	  public static final String MAX_BASIC_PREMIUM = "maxBasicPremium";
	  public static final String ADULT_PREMIUM = "adultPremium";
	  public static final String CHILD_PREMIUM = "childPremium";
	  public static final String ADULT_COUNT = "adultCount";
	  public static final String CHILD_COUNT = "childCount";
	  public static final String QNAME = "qname";
	  public static final String MESSAGE_ID = "messageId";
	  public static final String HEALTH_RATINGS = "HealthProductRatings";
	  public static final String DOCID_CONFIG			= "DocumentIDConfig";
	  
	  public static final String N = "N";
	  public static final String PRE_EXISTING_DISEASES = "preExistingDisease";
	  public static final String CRITICAL_ILLNESS = "criticalIllness";
	  public static final String ORGAN_DONOR = "organDonar";
	  public static final String IS_SPOUSE = "isSpouse";
	  public static final String IS_SELF = "isSelf";
	  public static final String SELF_AGE = "selfAge";
	  public static final String SPOUSE_AGE = "spouseAge";
	  public static final String MIN_INSURED_AGE = "minInsuredAge";
	  public static final String MAX_INSURED_AGE = "maxInsuredAge";
	  public static final String TOTAL_COUNT = "totalCount";
	  public static final String RIDERS = "riders";
	  public static final String RIDER_ID = "riderId";
	  public static final String RIDER_NAME = "riderName";
	  public static final String SUCC_CONFIG_CODE = "successCode";
	  public static final String SUCC_CONFIG_MSG = "successMessage";
	  public static final String FAIL_CONFIG_CODE = "failureCode";
	  public static final String FAIL_CONFIG_MSG = "failureMessage";
	  public static final String ERROR_CONFIG_CODE = "errorCode";
	  public static final String ERROR_CONFIG_MSG = "errorMessage";
	  public static final String PRD_NOT_FOUND_CODE = "quotePrdNotFoundCode";
	  public static final String PRD_NOT_FOUND_MSG = "quotePrdNotFoundMessage";
	  public static final String QUOTE_DATA_MISSING_CODE = "quoteInputMissingCode";
	  public static final String QUOTE_DATA_MISSING_MSG = "quoteInputMissingMessage";
	  public static final String QUOTE_DATA_RANGE_CODE = "quoteInputRangeValCode";
	  public static final String QUOTE_DATA_RANGE_MSG = "quoteInputRangeValMessage";
	  public static final String RESPONSE_CONFIG_DOC = "ResponseMessages";
	  public static final String DROOL_URL_CONFIG = "ExternalServiceURLConfig";
	  public static final String DROOL_SUMINSURED_CONFIG = "droolSumInsuredConfig";
	  public static final String LIFE_QUOTE_TYPE = "DroolQuoteType1";
	  public static final String BIKE_QUOTE_TYPE = "DroolQuoteType2";
	  public static final String CAR_QUOTE_TYPE = "DroolQuoteType3";
	  public static final String HEALTH_QUOTE_TYPE = "DroolQuoteType4";
	  public static final String QUOTE_REQ_PROPERTY = "quoteRequest";
	  public static final String QUOTE_RES_CODE = "responseCode";
	  public static final String QUOTE_RES_MSG = "message";
	  public static final String QUOTE_RES_DATA = "data";
	  public static final String REQUESTFLAG = "reqFlag";
	  public static final String FALSE = "False";
	  public static final String TRUE = "True";
	  public static final String UNCHECKED = "unchecked";
	  public static final String VALIDATION_ACTIVE = "validationActive";
	  public static final String Y = "Y";
	  public static final String CARRIER_REQUEST_FORM = "carrierRequestForm";
	  public static final String STAGE = "stage";
	  public static final String DROOLS_DALYCASHLIMIT = "dailyCashLimit";
	  public static final String DROOLS_NOOFDAYS = "noOfDays";
	  public static final String ISTOPUP = "isTopUp";
	  public static final String ISCRITICALILLNESS = "isCriticalIllness";
	
	  
	  public static final String LOG_REQ = "logReq";
	  public static final String QUOTE_TYPE = "DroolQuoteType";
	  public static final String QUOTE_URL = "quoteURL";
	  public static final String DROOLS_SHOWROOM_PRICE = "showRoomPrice";
	  public static final String DROOLS_INSURER_INDEX = "insurerIndex";
	  public static final String DROOLS_SUM_INSURED = "sumInsured";
	  public static final String DROOLS_RESULTS_NODE = "results";
	  public static final String DROOLS_RESULT_NODE = "result";
	  public static final String DROOLS_ELEMENT_NODE = "element";
	  public static final String DROOLS_VALUE_NODE = "value";
	  public static final int DROOLS_RESULT_ITEM_NO = 1;
	  public static final String DROOLS_AUTH_HEADER = "Authorization";
	  public static final String DROOLS_AUTH_DETAILS = "Basic a2llc2VydmVyOmtpZXNlcnZlcjEh";
	  public static final String DROOLS_CONTENT_TYPE_HEADER = "X-KIE-ContentType";
	  public static final String DROOLS_CONTENT_TYPE = "JSON";
	  public static final String DROOLS_HTTP_METHOD = "POST";
	  public static final String CAMEL_HTTP_METHOD = "CamelHttpMethod";
	  public static final String CAMEL_HTTP_PATH = "CamelHttpPath";
	  public static final String CAMEL_ACCEPT_CONTENT_TYPE = "CamelAcceptContentType";
	  public static final String DROOLS_ACCEPT_TYPE = "application/json";
	  public static final String SERVICE_QUOTE_TYPE = "quoteType";
	  public static final String SERVICE_QUOTE_PARAM = "quoteParam";
	  public static final String SERVICE_PERSINFO_PARAM = "personalInfo";
	  public static final String MIN_HOSP_LIMT = "minHospitalisationLimit";
	  public static final String MAX_HOSP_LIMT = "maxHospitalisationLimit";
	  public static final String DROOLS_SECONDEOPINION = "secondEopinion";
	  public static final String DROOLS_WELLNESSCOUCH = "wellnessCoach";
	  public static final String RELATION = "relation";
	  public static final String DEDUCTIBLE = "deductible";
	  public static final String HDFC_PIN_CODES = "HDFCHealthBlockedPinCodes";
	  public static final String MIN_SUM_INSURED = "minsumInsured";
	  
	  public static final String SERVICE_VEHICLE_PARAM = "vehicleInfo";
	  public static final String SERVICE_HEALTH_RATING_PARAM = "ratingParam";
	  public static final String SERVICE_CARRIER_PRODUCT_PARAM = "carrierProductInfo";
	  public static final String DROOLS_CARRIER_SERVICE_APPLICABLE = "carrierServiceApplicable";
	  public static final String DROOLS_CARRIER = "carrier";
	  public static final String DROOLS_CARRIERID = "carrierId";
	  public static final String DROOLS_PRODUCT_ID = "productId";
	  public static final String STATUS	 =	"status";
	  public static final String DROOLS_INSURANCE_TYPE = "insuranceType";
	  public static final String DROOLS_AGE_TYPE = "ageType";
	  public static final String DROOLS_PLANID = "planId";
	  public static final String DROOLS_PLAN_TYPE = "planType";
	  public static final String DROOLS_PLAN_NAME = "planName";
	  public static final String I = "I";
	  public static final String F = "F";
	  public static final String DROOLS_HLTHQUOTE_REQUEST_PART1 = "{\"insert\" : {\"object\" :{\"com.sutrr.quote.healthquotecalc.QuoteRequest\":{\"quoteParamRequestList\":[";
	  public static final String DROOLS_HLTHQUOTE_REQUEST_PART2 = "],\"isExecuted\":false}},\"disconnected\" : false,\"out-identifier\" : \"0\",\"return-object\" : true,\"entry-point\" : \"DEFAULT\"}},";
	  public static final String DROOLS_HLTHQUOTE_REQUEST_PART3 = "{\"lookup\" : \"quoteksession\",\"commands\" : [ ";
	  public static final String DROOLS_HLTHQUOTE_REQUEST_PART4 = "{\"fire-all-rules\" : {\"max\" : -1,\"out-identifier\" : null}} , {\"get-objects\" : {\"out-identifier\" : \"objects\"}}]}";
	  public static final String FEATURES = "Features";
	  public static final String RATINGS = "Ratings";
	  public static final String PRODUCTS = "products";
	  public static final String RATINGS_LIST = "ratingsList";
	  public static final String FEATURES_LIST = "featuresList";
	  public static final String CATEGORY_MAP = "categoryMap";
	  public static final String DEFAULT_RISK_TYPE = "DefaultRiskType";
	  public static final String RISKS = "risks";
	  public static final String QUOTES = "quotes";
	  public static final String CAR_MASTER_RESPONSE = "com.sutrr.quote.carquotecalc.MasterResponse";
	  public static final String BIKE_MASTER_RESPONSE = "com.sutrr.quote.bikequotecalc.BikeMasterResponse";
	  public static final String QUOTE_RESPONSE_NODE = "quoteResponse";
	  public static final String ARRAY_LIST_NODE = "java.util.ArrayList";
	  public static final String DISCOUNT_LIST_NODE = "discountList";
	  public static final String EXCLUSIVE_DISCOUNT = "exclusiveDiscounts";
	  public static final String CARRIER_NAME = "carrierName";
	  public static final String EXCEPTION = "Exception";
	  public static final String QUOTE_RESPONSE = "com.sutrr.quote.healthquotecalc.QuoteResponse";
	  public static final String DISCOUNT_DETAILS = "discountDetails";
	  public static final String RIDER_LIST = "riderList";
	  public static final String PLAN_RIDER_LIST = "planAsRiders";
	  public static final String RIDER_TYPE = "riderType";
	  public static final String RIDER_TYPE_VALUE = "R";
	  public static final String RIDER_DETAILS= "riderDetails";
	  public static final String IS_RIDER = "isRider";
	  public static final String PRODUCT_INFO = "productInfo";
	  public static final String SUM_INSURED_CALCULATED = "calSumInsured";
	  public static final String CARRIER_QUOTE_REQ_MAP_CONF = "HealthQuoteReqMapConf";
	  public static final String CARRIER_HEALTH_QUOTE_REQ_CONF = "HealthQuoteRequest-";
	  public static final String CARRIER_REQ_MAP_CONF = "carrierReqMapConf";
	  public static final String CARRIER_HEALTH_REQ_CONF = "Carrier-HealthREQCONF-";
	  public static final String CARRIER_HEALTH_RES_CONF = "Carrier-HealthRESCONF-";
	  public static final String HEALTH_RELATIONCODE_MAPPING_CONF = "HealthRealtionCodeMappingConf-";
	  public static final String HEALTH_RELATIONCODE_MEMBERLIST = "memberList";
	  public static final String HEALTH_RELATIONCODE_MEMBERCODED_CONFIG = "memberCodeConfig";
	  public static final String CARRIER_INPUT_REQUEST = "carrierInputRequest";
	  public static final String CARRIER_RESPONSE = "carrierResponse";
	  public static final String MAPPER_SUM_INSURED = "MapperSumInsured";
	  public static final String CARRIER_SUM_INSURED = "carrierSumInsured";
	  public static final String START_VALUE = "startValue";
	  public static final String END_VALUE = "endValue";
	  public static final String SUM_INSURED_ID = "sumInsuredId";
	  public static final String DROOL_CHILDPLANID = "childPlanId";
	  public static final String PRODUCT_CARRIERID = "productCarrierId";
	  public static final String PRODUCT_PLANID = "productPlanId";
	  public static final String PRODUCT_CHILDPLANID = "productChildPlanId";
	  public static final String CARRIER_TRANSFORMREQ="carrierTransformedReq";
	  public static final String CARRIER_PLAN = "HealthPlan-";
	  public static final String SERVICETAXDOC = "HealthServiceTaxConfiguration";
	  public static final String SERVICETAX = "serviceTax";
	  public static final String QUOTE_RESECODEFAIL = "1002";
	  public static final String QUOTE_RESEMSGEFAIL="failure";
	  public static final String QUOTE_VALIDATION_ERRORCODE="healthQuoteValidationCode";
	  public static final String QUOTE_VALIDATION_ERRORMSG="healthQuoteVaidationMsg";
	  public static final String PLANTYPE="planType";
	  public static final String CARRIERCITYQUERY = "select serv.* from ServerConfig serv where documentType='CityDetails' and businessLineId=4 and carrierId=$1  and pinCode=$2 and lower(city)=$3";
	  public static final String INDVIDUALPLAN_QUERY=" select P.* from ProductData P where documentType='HealthindividualPremium' ";
	  public static final String FAMILYPLAN_QUERY=" select P.* from ProductData P where documentType='HealthFamilyPremium' ";
	  public static final String ALL_PRODUCTS_PLANS_QUERY =   " SELECT a.carrierName,a.insurerIndex,a.claimIndex,a.claimRatio,b.carrierId,b.planId,b.planType,b.plans,b.UIFeatures as Features FROM ProductData b INNER JOIN ProductData a"
															+ " ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) "
															+ " where b.documentType='HealthPlan'"
															+ " and b.isProductActive=$1"
															//+ " and b.planType=$2"
															+ " and a.documentType='Carrier'" 
															;
	  
	  public static final String PRODUCTS_PLANRIDERS_QUERY =  " SELECT a.carrierName,a.insurerIndex,b.carrierId,a.claimIndex,a.claimRatio,b.planId,b.planType,b.planName,b.riderApplicable,b.riderList as riderList,b.UIFeatures as Features FROM ProductData b INNER JOIN ProductData a"
			    											+ " ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) "
			    											+ " where b.documentType='HealthPlan'"
			    											+ " and b.carrierId=$1"
			    											+ " and b.planId=$2"
			    											+ " and a.documentType='Carrier'" 
			    											;
	  public static final String PRODUCTS_RATINGS_QUERY = "select p.DefaultRiskType,p.products AS products from ProductData p where "
													      + " p.documentType='HealthCategoryRating' "
													      + " and p.businessLineId=4 "
													      + " and p.criticalIllness=$1" 
													      + " and p.organDonar=$2" 
													      + " and p.isSpouse=$3" 
													      + " and p.isCities=$4" 
													      + " and p.preExistingDisease=$5"
													      + " and p.isSelf=$6"  
													      + " and p.minSelf<=$7"
													      + " and p.selfAge>=$7" 
													      + " and p.minSpouse<=$8" 
													      + " and p.spouseAge>=$8" 
													      + " and p.minMinInsuredAge<=$9" 
													      + " and p.minMaxInsuredAge>=$9" 
													      + " and p.minInsuredAge<=$10" 
													      + " and p.maxInsuredAge>=$10" 
													      + " and p.minTotalCount<=$11" 
													      + " and p.totalCount>=$11" 
													      ;
	  
	  public static final String ALL_FAMILY_PRODUCTS_QUERY =
															  " SELECT a.carrierName,a.insurerIndex,a.claimRatio,b.carrierId,b.planId,b.planType,b.planName,b.riderApplicable,b.riderList as riderList,b.UIFeatures as Features FROM ProductData b INNER JOIN ProductData a"
														    + " ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) "
														    + " where b.documentType='HealthPlan'"
														    + " and b.planType='F'"
														    + " and b.preExistingDiseaseApp=$1"
														    + " and b.maxAllowedAdult>=$2" 
														    + " and b.maxAllowedChild>=$3" 
														    + " and b.minAllowedAdult<=$2"
															+ " and b.minAllowedChild<=$3"
														    + " and b.maxAllowedSumInsured >=$4" 
														    + " and b.maxAllowedSumInsured >=$5"
														    + " and a.documentType='Carrier' " 
														    ;
	  public static final String ALL_INDVIDUAL_PRODUCTS_QUERY =
															  " SELECT a.carrierName,a.insurerIndex,a.claimRatio,b.carrierId,b.planId,b.planType,b.planName,b.riderApplicable,b.riderList as riderList,b.UIFeatures as Features FROM ProductData b INNER JOIN ProductData a"
														    + " ON KEYS \"Carrier-\"||TOSTRING(b.carrierId) "
														    + " where b.documentType='HealthPlan'"
														    + " and b.planType='I'"
														    + " and b.preExistingDiseaseApp=$1"
														    + " and b.maxAllowedSumInsured >=$4" 
														    + " and b.maxAllowedSumInsured >=$5"
														    + " and a.documentType='Carrier' " 
														    ;
	}										
