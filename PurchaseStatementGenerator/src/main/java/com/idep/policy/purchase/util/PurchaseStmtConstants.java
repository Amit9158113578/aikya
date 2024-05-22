package com.idep.policy.purchase.util;

public class PurchaseStmtConstants {
	
	
	
	  public static final String SUCC_CONFIG_CODE 		= "successCode";
	  public static final String SUCC_CONFIG_MSG 		= "successMessage";
	  public static final String FAIL_CONFIG_CODE 		= "failureCode";
	  public static final String FAIL_CONFIG_MSG 		= "failureMessage";
	  public static final String ERROR_CONFIG_CODE 		= "errorCode";
	  public static final String ERROR_CONFIG_MSG 		= "errorMessage";
	  public static final String NO_RECORDS_CODE 		= "noRecordsCode";
	  public static final String NO_RECORDS_MSG 		= "noRecordsMessage";
	  public static final String RESPONSE_MSG 			= "ResponseMessages";
	  public static final String STMT_REQ_TYPE_CONFIG 	= "STMTREQTYPECONFIG";
	  
	  public static final String PROPOSAL_RES_CODE 		= "responseCode";
	  public static final String PROPOSAL_RES_MSG 		= "message";
	  public static final String PROPOSAL_RES_DATA 		= "data";
	  public static final String REQUESTFLAG 			= "reqFlag";
	  public static final String FALSE 					= "False";
	  public static final String TRUE 					= "True";
	  public static final String CARRIER_MAPPER_REQ 	= "carrierRequestForm";
	  public static final String CARRIER_INPUT_REQ 		= "carrierInputRequest";
	  
	  public static final String USER_PROFILE_QUERY		= " select p.proposalDetails,p.mobile,p.emailId"
	  													+ " from PolicyTransaction p "
	  													+ " where p.documentType='userProfileDetails'"
	  													+ " and p.secretKey=$1";
	  
	  public static final String PRODUCT_ID 			= "productId";
	  public static final String CARRIER_ID 			= "carrierId";
	  

}
