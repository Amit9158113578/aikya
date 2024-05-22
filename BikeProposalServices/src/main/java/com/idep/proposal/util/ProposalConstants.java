package com.idep.proposal.util;

public class ProposalConstants {
  public static final String BASE_ENV_STATUS = "baseEnvStatus";
  
  public static final String DOCUMENT_TYPE = "documentType";
  
  public static final String DOCID_CONFIG = "DocumentIDConfig";
  
  public static final String RESPONSE_CONFIG_DOC = "ResponseMessages";
  
  public static final String PROPOSAL_SEQ = "proposal";
  
  public static final String PROPOSAL_ID = "proposalId";
  
  public static final String ENCRYPT_PROPOSAL_ID = "encryptedProposalId";
  
  public static final String QUOTE_ID = "QUOTE_ID";
  
  public static final String POLICYNO = "policyNo";
  
  public static final String ENCRYPT_QUOTE_ID = "encryptedQuoteId";
  
  public static final String BIKEPROPOSAL_DOCELE = "bikeproposal";
  
  public static final String EXTRA_FIELDS = "extraFields";
  
  public static final String CARRIER_INPUT_REQ = "carrierInputRequest";
  
  public static final String CARRIER_REQ_MAP_CONF = "carrierReqMapConf";
  
  public static final String PROPOSALCONF_REQ = "BikeProposalREQCONF-";
  
  public static final String CARRIER_ID = "carrierId";
  
  public static final String PRODUCT_ID = "productId";
  
  public static final String QUOTE_BUCKET = "QuoteData";
  
  public static final String INSURANCE_DTLS = "insuranceDetails";
  
  public static final String INSURANCE_TYPE = "insuranceType";
  
  public static final String PROPOSALINTFORMATCONF_REQ = "BikeProposalIntFormatREQCONF-";
  
  public static final String CARRIER_MAPPER_REQ = "carrierRequestForm";
  
  public static final String QUOTE_UPDATE_CONF_RES = "BikeQUOTEUPDTRESCONF-";
  
  public static final String REQUESTFLAG = "reqFlag";
  
  public static final String FALSE = "False";
  
  public static final String TRUE = "True";
  
  public static final String PROPOSAL_RES_CODE = "responseCode";
  
  public static final String PROPOSAL_RES_MSG = "message";
  
  public static final String PROPOSAL_RES_DATA = "data";
  
  public static final String CARRIER_RESPONSE = "carrierResponse";
  
  public static final String PROPOSALCONF_RES = "BikeProposalRESCONF-";
  
  public static final String SUCC_CONFIG_CODE = "successCode";
  
  public static final String SUCC_CONFIG_MSG = "successMessage";
  
  public static final String SUCC_MSG = "success";
  
  public static final String QUOTE_NOT_FOUND_CODE = "ResponseCodeQuoteInfoNotFound";
  
  public static final String QUOTE_NOT_FOUND_MSG = "ResponseMsgQuoteInfoNotFound";
  
  public static final String PRE_POLICY_EXP_MSG = "ResponseMsgPrePolicyExp";
  
  public static final String QUOTE_INFO_NOT_FOUND_MSG = "ResponseMsgQuoteRequestInfoNotFound";
  
  public static final String PREMIUM_DETAILS_NOT_FOUND_MSG = "ResponseMsgPremiumInfoNotFound";
  
  public static final String ERROR_CONFIG_CODE = "ResponseCodeFailure";
  
  public static final String ERROR_CONFIG_MSG = "failureMsg";
  
  public static final String PROPOSAL_SERVICE_HEADER = "bikeproposalService";
  
  public static final String SERVICE_URL_CONFIG_DOC = "BikeProposalServiceURLConfig";
  
  public static final String PROPOSAL_REQ_TYPE = "BikeProposalRequest";
  
  public static final String PROPOSAL_POLICY_TYPE = "policyType";
  
  public static final String PROPOSAL_PREMIUM_DETAILS = "premiumDetails";
  
  public static final String BIKE_PROPOSER_ADD_QUERY = "";
  
  public static final String POLICYCONF_REQ = "BikePolicyREQCONF-";
  
  public static final String POLICY_REQ = "BikePolicyRequest-";
  
  public static final String POLICYCONF_RES = "BikePolicyRESCONF-";
  
  public static final String NEWINDIA_SAVE_POL_HOL = "BikePolicyHolder";
  
  public static final String POLICY_HOLDER_SUCCESS = "policyHolderSave";
  
  public static final String USER_PROFILE_QUERY = " select meta(p).id as documentId,p.proposalDetails,p.lastName,p.pincode,p.gender,p.mobile,p.dateOfBirth,p.emailId,p.aadharId, p.firstName,p.addressLine1,p.addressLine2,p.maritalStatus from PolicyTransaction p  where p.documentType='userProfileDetails' and p.secretKey=$1";
  
  public static final String POLICYVIEW_CONFIG = "Carrier-PolicyViewREQCONF-";
  
  public static final String BIKEPOLICYDOCREQ = "BikePolicyDocumentRequest-";
  
  public static final String BIKEGSTCONF = "GSTConfig-";
  
  public static final String PROPOSAL_DETAILS = "proposerDetails";
  
  public static final String PAYMENTRES = "PAYMENTRES";
  
  public static final String TRANSSTATUSCODE = "transactionStatusCode";
  
  public static final String NO_RECORDS_CODE = "noRecordsCode";
  
  public static final String NO_RECORDS_MSG = "noRecordsMessage";
  
  public static final String TRANSSTATUSINFO = "transactionStausInfo";
  
  public static final int CARRIER_RES_CODE = 1010;
  
  public static final String CARRIER_RES_MSG = "error";
  
  public static final String CARREGPATTERNDOCUMENT = "RegistrationNumberPatternDoc";
  
  public static final String CARRIERPROSTATUS = "carrierProposalStatus";
  
  public static final String NEW = "new";
  
  public static final String CARRIERPROPREQUEST = "BikeCarrierProposalRequest";
  
  public static final String CARRIERPROPRESPONSE = "BikeCarrierProposalResponse";
  
  public static final String CARRIERPREPROPREQUEST = "BikeCarrierPrePaymentRequest";
  
  public static final String CARRIERPREPROPRESPONSE = "BikeCarrierPrePaymentResponse";
  
  public static final String CARRIERPOLICYREQUEST = "BikeCarrierPolicyRequest";
  
  public static final String CARRIERPOLICYPRESPONSE = "BikeCarrierPolicyResponse";
  
  public static final String REQUEST = "request";
  
  public static final String RESPONSE = "response";
  
  public static final String PROPOSALCREATEDATE = "proposalCreatedDate";
  
  public static final String POLICYCREATEDATE = "policyCreatedDate";
  
  public static final String POLICYDOCRES = "POLICYDOCRES";
  
  public static final String POLICYBOND = "Bike Policy";
  
  public static final String RSPROPOSALRESPONSEMSG = "Quote Approved,Proceed Buy Policy";
  
  public static final String LOG_REQ = "logReq";
  
  public static final String PROPOSAL = "PROPOSAL";
  
  public static final String DEFAULT_LOG = "defaultLog";
  
  public static final String BIKEPROPOSAL_REQ = "BikeProposalRequest-";
  
  public static final String ORIENTALBIKEPROPREQPROCESS = "ORIENTALBIKEPROPREQPROCESS";
  
  public static final String CARRIER_FETCH_DOC_INPUT_REQ = "carrierFetchDocInputRequest";
  
  public static final String CARRIER_INPUT_REQUEST = "carrierInputRequest";
  
  public static final String UIRESPONSE = "UIResponse";
  
  public static final String VARIANTID = "variantId";
  
  public static final String PROPOSER_VEHICLE_DETAILS = "vehicleDetails";
  
  public static final String PROPOSER_NOMINEE_DETAILS = "nominationDetails";
  
  public static final String LEAD_PROFILE_QUERY = "select messageId from PolicyTransaction where documentType = 'leadProfileRequest' and mobileNumber = '";
  
  public static final String POLICY_DOWNLOAD_CONFIG_DOC = "BikePolicyDocDownloadConfig";
  
  public static final String CARRIER_UKEY_PKEY_INPUT_REQ = "carrierUkeyPkeyInputRequest";
  
  public static final String BIKE_QUOTE_RESPONSE = "bikeQuoteResponse";
  
  public static final String BIKE_QUOTE_REQUEST = "bikeQuoteRequest";
  
  public static final String PA_COVER_DETAILS = "PACoverDetails";
  
  public static final String REGISTRATION_YEAR = "regYear";
  
  public static final String DATE_OF_REG = "dateOfRegistration";
  
  public static final String RTO_CODE = "RTOCode";
  
  public static final String VARIANT_ID = "variantId";
  
  public static final String REGIS_DATE = "registrationDate";
  
  public static final String VEHICLE_INFO = "vehicleInfo";
  
  public static final String PRE_PAYMENT_REQUEST = "PrePaymentRequest-";
  
  public static final String POLICYREQUEST_CONF = "BikePolicyRequest-";
  
  public static final String PRE_POLICY_EXP_DATE = "PreviousPolicyExpiryDate";
  
  public static final String VALIDATE_DOC = "ProposalValidationDocConfig";
  
  public static final String PRE_INSURER = "preInsurerNotFoundMsg";
  
  public static final String DISTRICT = "distrctDetailsNotFoundMsg";
  
  public static final String CITYDETA = "cityDetailsNotFoundMsg";
  
  public static final String UPDATE_SEQ = "SEQBIKEPROPOSAL";
  
  public static final String CAMEL_ACCEPT_CONTENT_TYPE = "CamelAcceptContentType";
  
  public static final String DROOLS_ACCEPT_TYPE = "application/json";
  
  public static final String CONTENT_TYPE = "content-type";
  
  public static final String CAMEL_HTTP_METHOD = "CamelHttpMethod";
  
  public static final String DROOLS_HTTP_METHOD = "POST";
  
  public static final String WEB_SERVICE_TYPE = "webserviceType";
  
  public static final String URI = "requestURL";
  
  public static final String SERVICE_DOWN_CODE = "P365RES103";
  
  public static final String SERVICE_DOWN_MSG = "invoke service down";
  
  public static final String INVOKE_SERVICE = "invokeservice";
  
  public static final String PROPOSAL_REQUEST = "proposalRequest";
  
  public static final String POLICY_REQUEST = "policyRequest";
  
  public static final String AP_PREFER_ID = "apPreferId";
  
  public static final String DOWNLOAD_URL = "downloadUrl";
  
  public static final String VALIDATE_POLICY_RESPONSE_NODE = "bikePolicyResponse";
  
  public static final String POLICY_ISSUED_FLAG = "policyIssuedFlag";
  
  public static final String REST_URL = "http://localhost:1201/cxf/restapiservice/config/restcall/integrate/invoke?httpClient.soTimeout=10000";
}


