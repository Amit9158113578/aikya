package com.idep.proposal.util;

public class ProposalConstants {
	
	  public static final String  DEFAULT_LOG = "defaultLog";
	  public static final String BASE_ENV_STATUS 		= "baseEnvStatus";
	  public static final String SUCC_CONFIG_CODE 		= "successCode";
	  public static final String SUCC_CONFIG_MSG 		= "successMessage";
	  public static final String FAIL_CONFIG_CODE 		= "failureCode";
	  public static final String FAIL_CONFIG_MSG 		= "failureMessage";
	  public static final String ERROR_CONFIG_CODE 		= "errorCode";
	  public static final String ERROR_CONFIG_MSG 		= "errorMessage";
	  public static final String REQUESTFLAG 							= "reqFlag";
	  public static final String PROPOSAL_ERROR_CODE 		= "proposalErrorCode";
	  public static final String PROPOSAL_ERROR_MSG 		= "proposalErrorMessage";
	  public static final String FALSE 								= "False";
	  public static final String TRUE 								= "True";
	  public static final String QUOTE_RES_CODE = "responseCode";
	  public static final String QUOTE_RES_MSG = "message";
	  public static final String QUOTE_RES_DATA = "data";
	  public static final String PROPOSAL_RES_CODE 		= "responseCode";
	  public static final String PROPOSAL_RES_MSG 		= "message";
	  public static final String PROPOSAL_RES_DATA 		= "data";
	  
	  public static final String CARRIER_INPUT_REQ 		= "healthProposalRequest";
	  public static final String CARRIER_PROP_RES		= "healthProposalResponse";
	  public static final String HLTHPOLICY_INPUT_REQ 	= "healthPolicyRequest";
	  public static final String HLTHPOLICYDOC_INPUT_REQ= "healthPolicyDocumentRequest";
	  public static final String HLTHPOLICYDOC_CONFIG	= "healthPolicyDocumentConfig";
	  public static final String CARRIER_REQ_MAP_CONF 	= "carrierReqMapConf";
	  public static final String PLAN_ID 				= "planId";
	  public static final String CARRIER_ID 			= "carrierId";
	  public static final String PROPOSALCONF_REQ		= "Carrier-HealthProposalREQCONF-";
	  public static final String PROPOSALCONF_RES		= "Carrier-HealthProposalRESCONF-";
	  public static final String CARRIER_RESPONSE		=  "carrierResponse";
	  public static final String REFERENCE_ID			=  "referenceId";
	  public static final String CARRIERSOAPCONFIG      = "carrierSOAPConfig";
	  public static final String CAMEL_HTTP_PATH		=  "CamelHttpPath";
	  public static final String CAMEL_HTTP_URI			=  "CamelHttpUri";
	  
	  public static final String 	PROPOSAL_DOCID 		= "proposal";
	  public static final String 	PROPOSAL_SEQ 		= "SEQHEALTHPROPOSAL";
	  public static final String 	DOCID_CONFIG		= "DocumentIDConfig";
	  public static final String	HEALTHPROPOSAL_DOCELEMENT = "healthProposalId";
	  public static final String 	PROPOSAL_ID 		= "proposalId";
	  
	  public static final String 	EXTRA_FIELDS 		= "extraFields";
	  public static final String CARRIER_MAPPER_REQ 	= "carrierRequestForm";
	  public static final String DOCUMENT_TYPE_VALUE 	= "proposalResponse";
	  public static final String DOCUMENT_TYPE 			= "documentType";
	  public static final String PROPOSAL_SERVICE_HEADER = "healthproposalService";
	  public static final String SERVICE_URL_CONFIG_DOC  = "HealthProposalServiceURLConfig";
	  public static final String POLICYCONF_REQ			= "HealthPolicyREQCONF-";
	  public static final String POLICYCONF_RES			= "HealthPolicyRESCONF-";
	  public static final String RESPONSE_CONFIG_DOC 	= "ResponseMessages";
	  public static final String PROPOSALREQTYPE		= "requestType";
	  public static final String PROPOSALREQ_CONFIG 	= "proposalConfiguration";
	  public static final String PROPSALMEMBER = "Member";
	  public static final String HEALTHPLAN = "HealthPlan";
	  public static final String DOC_ID_CONFIG = "DocumentIDConfig";
	  public static final String ABHI_QUOTE_ID = "ABHIQuoteId";
	  public static final String PLANTYPE = "planType";
	  public static final String POLICYBOND = "HealthPolicyBond";
	  public static final String LOG_REQ = "logReq";
	  public static final String PROPOSALREQ              = "PROPOSALREQ";
	  public static final String SERVICEINVOKE           = "SERVICEINVOKE";
	  public static final String POLICYSERVICE           = "POLICYSERVICEINVOKE";
	  public static final String MAPPERREQ           = "MAPPERREQ";
	  public static final String RIDERPROCESS           = "RIDERPROCESS";
	  public static final String MAPPERRES           = "MAPPERRES";
	  public static final String PROPOSALRES              = "PROPOSALRESPONSE";
	  public static final String PAYMENTRES              = "PAYMENTRES";
	  public static final String POLICYREQ                = "POLICYREQ";
	  public static final String POLICYDOCREQ                = "POLICYDOCREQ";
	  public static final String POLICYDOCRES                = "POLICYDOCRES";
	  public static final String POLICY_RESPONSE           = "POLICY RESPONSE";
	  public static final String USERPROFILE           = "USERPROFILE";
	  public static final String INSUREDMEMBERS="insuredMembers";
	  public static final String HEALTHPROADDRESSPRO           = "HEALTHPROADDRESSPRO";
	  public static final String PROPSALFAMILYQUERY = "select planCode from ProductData where documentType='HealthFamilyPremium' and carrierId= $1 and planId=$2 and adultCount=$3 and childCount =$4 and minInsuredAge <= $5 and maxInsuredAge >=$5 and minAllowedSumInsured=$6 and policyTerm=$7";
	  
	  public static final String PROPSALINDIVIDUALQUERY = "select planCode from ProductData where documentType='HealthindividualPremium' and carrierId=$1 and planId=$2 and minInsuredAge <= $3 and maxInsuredAge >=$3 and minAllowedSumInsured=$4 and policyTerm=$5";
	  
	  public static final String CARRIERCITYQUERY = "select serv.* from ServerConfig serv where documentType='CityDetails' and businessLineId=4 and carrierId=$1  and pinCode=$2 and lower(city)=$3";
	  
	  public static final String USER_PROFILE_QUERY		= " select meta(p).id as documentId,p.proposalDetails from PolicyTransaction p "
														+ " where p.documentType='userProfileDetails'"
														+ " and p.secretKey=$1";
	  public static final String RESECODESUCESS = "1000";
	  public static final String RESECODEFAIL = "1002";
	  public static final String RESECODEERROR = "1010";
	  public static final String RESEMSGSUCESS = "success";
	  public static final String RESEMSGEFAIL = "failure";
	  public static final String RESEMSGEERROR = "error";
	  public static final String TRANSSTATUSINFO = "transactionStausInfo";
	  public static final String TRANSSTATUSCODE = "transactionStatusCode";
	  public static final String PRODUCTID = "productId";
	 
	  public static final String PROPOSAL_REQUEST        =   "proposalRequest";
	  public static final String PAYMENT_RESPROCESSOR        =   "paymentResponse";
	  public static final String HEALTHPOLICYREQPREPROCE           = "FutureHealthPolicyProcessor";
	  public static final String POLICYREQUEST_CONF		= "HealthPolicyRequest-";
	  public static final String PROPOSAL_MEMBER_CODE_CONF		= "HealthRealtionCodeMappingConf-60";
	  public static final String PRODUCT_INFO = "productInfo";
	  public static final String POLICYREQ_CONF = "healthProposalConfigNode";
	  public static final String PROPOSAL_UPDATE_CONF_RES = "HealthProposalUPDTRESCONF";
	  public static final String RSPROPOSALSAVEDDETAILSRES = "proposalSaveDetails";
	  public static final String  SAVE_POLICY_REQ_THRESHOLD = "threshold";
	  public static final String STATUS	 				="status";
	  public static final String STAGE	 				="stage";
	  public static final String POLICY_UKEYPKEY = "carrierUkeyPkeyInputRequest";
	  public static final String HEALTH_POLICY_DOC_DOWNLOAD_CONFIG ="HealthPolicyDocDownloadConfig";
	  public static final String POLICY_FETCHDOC_INPUTREQ ="carrierFetchDocInputRequest";
	  public static final String HEALTH_POLICY_DOC_REQUEST ="HealthPolicyDocRequest-";
	  public static final String 	ENCRYPT_PROPOSAL_ID = "encryptedProposalId";
	  public static final String 	ENCRYPT_QUOTE_ID = "encryptedQuoteId";
	  public static final String 	QUOTE_ID = "QUOTE_ID";
	  public static final int   CARRIER_RES_CODE                = 1010;

}
