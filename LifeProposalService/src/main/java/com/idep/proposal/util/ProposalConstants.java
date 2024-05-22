package com.idep.proposal.util;

public class ProposalConstants {

	public static final String BASE_ENV_STATUS 		= "baseEnvStatus";
	
	public static final String SUCC_CONFIG_CODE 		= "successCode";
	public static final String SUCC_CONFIG_MSG 		= "successMessage";
	public static final String RESECODESUCESS       = "1000";
	public static final String RESEMSGSUCESS        = "success";
	
	public static final String FAIL_CONFIG_CODE 		= "failureCode";
	public static final String FAIL_CONFIG_MSG 		= "failureMessage";
	
	public static final String ERROR_CONFIG_CODE 		= "errorCode";
	public static final String ERROR_CONFIG_MSG 		= "errorMessage";
	public static final String RESECODEERROR            = "1010";
	public static final String RESECODE_ERROR            = "1002";
	public static final String RESEMSGEERROR            = "error"; 
	
	public static final String PROPOSER_ADD_ERROR_CODE 		= "proposerAddressErrorCode";
	public static final String PROPOSER_ADD_ERROR__MSG 		= "proposerAddressErrorMessage";
	public static final String PROPOSER_VEHICLE_DETAILS = "vehicleDetails";
	public static final String PROPOSAL_REQUEST        =   "proposalRequest";
	public static final String PAYMENT_RESPROCESSOR        =   "paymentResponse";
	public static final String QUOTE_ID                  =   "QUOTE_ID";
	public static final String 	ENCRYPT_QUOTE_ID = "encryptedQuoteId";
	public static final String 	ENCRYPT_PROPOSAL_ID = "encryptedProposalId";
	public static final String QUOTE_BUCKET              = "QuoteData";
	public static final String PRODUCT_BUCKET              = "ProductData";

	public static final String PAYMENTRES             = "PAYMENTRES";
	public static final String TRANSSTATUSCODE 		= "transactionStatusCode";
	public static final String PROPOSAL_RES_CODE 		= "responseCode";
	public static final String PROPOSAL_RES_MSG 		= "message";
	public static final String PROPOSAL_RES_DATA 		= "data";
	public static final String RESPONSE_CONFIG_DOC 	= "ResponseMessages";
	public static final String DOCID_CONFIG			= "DocumentIDConfig";

	public static final String REQUESTFLAG 			= "reqFlag";
	public static final String POLICY_HOLDER_SUCCESS  = "policyHolderSave";
	public static final String FALSE 					= "False";
	public static final String TRUE 					= "True";
	public static final String LOG_REQ                = "logReq";
	public static final int   CARRIER_RES_CODE                = 1010;
	public static final String CARRIER_RES_MSG                = "error";

	public static final String PROPOSAL_SERVICE_HEADER = "proposalService";
	public static final String LIFE_SERVICE_URL_CONFIG_DOC  = "LifeProposalServiceURLConfig-Copy";
	public static final String POLICY_DOWNLOAD_CONFIG_DOC ="CarPolicyDocDownloadConfig";
	public static final String CONTENT_MGMT_CONFIG_DOC = "ContentManagementConfig";
	public static final String Request_Type = "requestType";

	public static final String CARRIER_INPUT_REQ 		= "carrierInputRequest";
	public static final String CARRIER_FETCH_DOC_INPUT_REQ 		= "carrierFetchDocInputRequest";
	public static final String CARRIER_UKEY_PKEY_INPUT_REQ 		= "carrierUkeyPkeyInputRequest";
	public static final String CARRIER_POLICY_DOC_INPUT_REQ 		= "carrierPolicyDocInputRequest";
	public static final String CARRIER_REQ_MAP_CONF 	     = "carrierReqMapConf";
	public static final String PRODUCT_ID 			= "productId";
	public static final String CARRIER_ID 			= "carrierId";
	public static final String PROPOSALINTFORMATCONF_REQ		= "CarProposalIntFormatREQCONF-";
	public static final String PROPOSALCONF_REQ		= "LifeProposalREQCONF-";
	public static final String PROPOSALXPATHCONF_REQDOC		= "LifeProposalRequest-";
	public static final String PROPOSALXPATHCONF_REQ		= "LifeProposalRequestConfigDoc";
	public static final String PROPOSALCONF_RES		= "LifeProposalRESCONF-";
	public static final String QUOTE_UPDATE_CONF_REQ	= "CarQUOTEUPDTREQCONF-";
	public static final String QUOTE_UPDATE_CONF_RES	= "CarQUOTEUPDTRESCONF-";
	public static final String POLICYCONF_REQ			= "CarPolicyREQCONF-";
	public static final String POLICYREQUEST_CONF		= "CarPolicyRequest-";
	public static final String PROPOSAL_RESP			= "LifeProposalResponse-";

	public static final String POLICYCONF_RES			= "CarPolicyRESCONF-";
	public static final String CARRIER_RESPONSE		=  "carrierResponse";
	public static final String REFERENCE_ID			=  "referenceId";
	public static final String NEWINDIA_SAVE_POL_HOL  =  "CarPolicyHolder";
	public static final String NEWINDIA_COLLECT_PREM  =  "CarCollectPremium";

	public static final String CAR_PROPOSAL_MULTIJSON	=  "CarPROPOSALJSON-";
	public static final String INSURANCE_TYPE			=  "insuranceType";
	public static final String INSURANCE_DTLS			=  "insuranceDetails";

	public static final String CAMEL_HTTP_PATH		=  "CamelHttpPath";
	public static final String CAMEL_HTTP_URI			=  "CamelHttpUri";

	public static final String 	PROPOSAL_DOCID 		= "proposal";
	public static final String 	PROPOSAL_SEQ 		= "SEQLIFEPROPOSAL";
	public static final String 	PROPOSAL_ID 		= "proposalId";
	public static final String 	POLICY_NUMBER 		= "policyNo";
	public static final String 	LIFEPROPOSAL_DOCELE = "lifeproposal";
	public static final String 	EXTRA_FIELDS 		= "extraFields";
	public static final String 	CARRIER_MAPPER_REQ 	= "carrierRequestForm";
	public static final String 	DOCUMENT_TYPE_VALUE = "lifeProposalRequest";
	public static final String 	CARPROPOSAL_REQ =     "CarProposalRequest-";
	public static final String 	DOCUMENT_TYPE 		= "documentType";
	public static final String 	SHA_ALGO 			= "SHA-512";
	public static final String 	UTF8 				= "UTF-8";
	public static final String    GET_USER_POLICY_DETAILS  = " select p.* from PolicyTransaction p "
			+ " where p.documentType='userProfileDetails'"
			+ " and p.secretKey=$1";

	public static final String TRANSSTATUSINFO = "transactionStausInfo";
	public static final String CARREGPATTERNDOCUMENT           = "RegistrationNumberPatternDoc";
	public static final String CARRIER_PROPOSAL_REQUEST       ="CarrierProposalRequest-";
	public static final String CARRIER_PROP_REQ ="CarrierProposalRequest";

	public static final String CARRIER_PROPOSAL_RESPONSE       ="CarrierProposalResponse-";
	public static final String CARRIER_PROP_RES ="CarrierProposalResponse";

	public static final String CARRIER_SPECIFIC_CONFIGURATION		="CarrierConfiguration-";
	public static final String CARRIER_CONFIG		="CarrierConfiguration";
	// Store logs
	public static final String CARRIERPREPROPREQUEST                  = "LifeCarrierPrePaymentRequest";
	public static final String REQUEST                              = "request";
	public static final String PROPOSALCREATEDATE                   = "proposalCreatedDate";
	public static final String CARRIERPREPROPRESPONSE                 = "LifeCarrierPrePaymentResponse";


	/////////////////////////LOGGER NAMES///////////////////////////////////////////////////

	public static final String PROPOSAL                = "PROPOSAL";
	public static final String CARPROPREQPROCESS       = "CARPROPREQPROCESS";
	public static final String LIFEPRODBREQPROCESS      = "LIFEPRODBREQPROCESS";
	public static final String LIFEPRODBSTORE           = "LIFEPRODBSTORE";
	public static final String LIFEPROREQHANDLER        = "LIFEPROREQHANDLER";
	public static final String SERVICEINVOKE           = "SERVICEINVOKE";
	public static final String KOTAKPROREQDBSTO        = "KOTAKPROREQDBSTO";
	public static final String LIFEPRORESHANDL          = "LIFEPRORESHANDL";
	public static final String CARPRORESPROCESSOR      = "CARPRORESPROCESSOR";
	public static final String PROPOSALRES              = "PROPOSALRESPONSE";
	public static final String PROPOSALREQ              = "PROPOSALREQ";
	public static final String POLICYREQ                = "POLICYREQ";
	public static final String PAYRESPONSE              = "PAYRESPONSE";
	public static final String CARPOLICYREQPROCE        = "CARPOLICYREQPROCE";
	public static final String CARPOLICYRESHANDL         = "CARPOLICYRESHANDL";
	public static final String USERPROREQPRO             = "USERPROREQPRO";
	public static final String CARPOLICYRESPRO           = "CARPOLICYRESPRO";
	public static final String POLICY_RESPONSE           = "POLICY RESPONSE";
	public static final String POLICY_SIGN                = "POLICY SIGN";
	public static final String CARPROPDATALOADER           = "CARPROPDATALOADER";
	public static final String CAREMAILTEMPLOADER           = "CAREMAILTEMPLOADER";
	public static final String KOTAKREQFORM                = "KOTAKREQFORM";
	public static final String KOTAKPRODBUPDATE           = "KOTAKPRODBUPDATE";
	public static final String KOTAKPROSENDER               = "KOTAKPROSENDER";
	public static final String KOTAKPOLICYDOCPRO           = "KOTAKPOLICYDOCPRO";
	public static final String CARRSPROPREQPRO           = "CARRSPROPREQPRO";
	public static final String CARMULTJSONSUPP           = "CARMULTJSONSUPP";
	public static final String RSCARPRORESPROCE           = "RSCARPRORESPROCE";
	public static final String CARPROADDRESSPRO           = "CARPROADDRESSPRO";
	public static final String RSCARPROUPDATERESPROCE     = "RSCARPROUPDATERESPROCE";
	public static final String SOAPREQFORMATTER           = "SOAPREQFORMATTER";
	public static final String BHARTIAXARESTRANS           = "BHARTIAXARESTRANS";
	public static final String SOAPRESFORMATTER           = "SOAPRESFORMATTER";
	public static final String FUTUREGENSOAPREQFORM           = "FUTUREGENSOAPREQFORM";
	public static final String XMLCHARTEMI                     = "XMLCHARTEMI";
	public static final String FUTUREGENCARPROPREQPROCESS     = "FUTUREGENCARPROPREQPROCESS";
	public static final String FUTUREGENSOAPRESFORM           = "FUTUREGENSOAPRESFORM";
	public static final String CARPOLICYREQPREPROCE           = "PolicyReqPrepareProcessor";
	public static final String KOTAKLIFEREQTRANSFORMER        = "KotakLifeRequestTransformer";
	public static final String LIFEPROADDRESSPRO              = "KotakLifeRequestTransformer";
	public static final String 	PROPOSAL_SCREEN_CONFIG         = "proposalScreenConfig";
	public static final String 	LIFE_PROPOSAL_REQUEST         = "LifeProposalRequest";
	public static final String 	LIFE_PROPOSAL_REDIRECTION_CONFIG   = "LifeProposalRedirectionConfig";
	public static final String 	ADDRESS_DETAILS   = "addressDetails";
	public static final String 	COMMUNICATION_ADDRESS   = "communicationAddress";
	public static final String 	PERMANENT_ADDRESS   = "permanentAddress";
	

	
	
}
