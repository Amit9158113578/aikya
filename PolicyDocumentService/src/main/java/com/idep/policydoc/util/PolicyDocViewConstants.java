package com.idep.policydoc.util;

public class PolicyDocViewConstants {
	
	  public static final String SUCC_CONFIG_CODE 		= "successCode";
	  public static final String SUCC_CONFIG_MSG 		= "successMessage";
	  public static final String FAIL_CONFIG_CODE 		= "failureCode";
	  public static final String FAIL_CONFIG_MSG 		= "failureMessage";
	  public static final String ERROR_CONFIG_CODE 		= "errorCode";
	  public static final String ERROR_CONFIG_MSG 		= "errorMessage";
	  public static final String NO_RECORDS_CODE 		= "noRecordsCode";
	  public static final String NO_RECORDS_MSG 		= "noRecordsMessage";
	  public static final String RESPONSE_MSG 			= "ResponseMessages";
	  public static final String LOG_REQ                = "logReq";
	  
	  public static final String PROPOSAL_RES_CODE 		= "responseCode";
	  public static final String PROPOSAL_RES_MSG 		= "message";
	  public static final String PROPOSAL_RES_DATA 		= "data";
	  public static final String REQUESTFLAG 			= "reqFlag";
	  public static final String FALSE 					= "False";
	  public static final String TRUE 					= "True";
	  public static final String CARRIER_MAPPER_REQ 	= "carrierRequestForm";
	  public static final String CARRIER_INPUT_REQ 		= "carrierInputRequest";
	  public static final String CARRIER_PROPOSAL_PRO 	= "carrierProposal";
	  public static final String USER_PROFILE_QUERY		= " select meta(p).id as documentId,p.proposalDetails,p.lastName,p.pincode,p.gender,p.mobile,p.dateOfBirth,p.emailId,p.aadharId,"
	  													+ " p.firstName,p.addressLine1,p.addressLine2,p.maritalStatus from PolicyTransaction p "
	  													+ " where p.documentType='userProfileDetails'"
	  													+ " and p.secretKey=$1";
	  public static final String CARRIER_REQ_MAP_CONF 	= "carrierReqMapConf";
	  public static final String PRODUCT_ID 			= "productId";
	  public static final String POLICYVIEW_CONFIG		= "Carrier-PolicyViewREQCONF-";
	  public static final String CARRIER_ID 			= "carrierId";
	  public static final String GSTCONF     		    = "GSTConfig-";
	  
	  public static final String CAMEL_HTTP_PATH		=  "CamelHttpPath";
	  public static final String CAMEL_HTTP_URI			=  "CamelHttpUri";
	  
	  public static final String USER_PROPOSALDTLS_QUERY= " select meta(p).id as documentId,p.proposalDetails from PolicyTransaction p "
														+ " where p.documentType='userProfileDetails'"
														+ " and p.secretKey=$1";
	  
	  public static final String POLICYDOCVIEWREQPROCE           = "POLICYDOCVIEWREQPROCE";
	  public static final String POLICY_SIGN                = "POLICY SIGN";
	  public static final String POLICYDOCVIEWMAPPER           = "POLICYDOCVIEWMAPPER";
	  public static final String POLICYDOCVIEWRESHANDL           = "POLICYDOCVIEWRESHANDL";
	  public static final String POLICYDOCJASPPRO           = "POLICYDOCJASPPRO";
	  public static final String SERVICEINVOKE           = "SERVICEINVOKE";
	  public static final String POLICYDOCRESPRO           = "POLICYDOCRESPRO";
	  public static final String POLICY_SIGN_RESPONSE           = "POLICY SIGN RESPONSE";
	  public static final String SERVICE_DATE_FORMAT = "dd/MM/yyyy";

	  
	  

}
