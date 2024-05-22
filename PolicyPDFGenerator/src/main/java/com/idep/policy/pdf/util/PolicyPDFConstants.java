package com.idep.policy.pdf.util;

public class PolicyPDFConstants {
	
	 public static final String CAMEL_HTTP_PATH			=  "CamelHttpPath";
	 public static final String CAMEL_HTTP_URI			=  "CamelHttpUri";
	 public static final String USER_PROFILE_QUERY		= " select p.proposalDetails,p.mobile,p.emailId"
														+ " from PolicyTransaction p "
														+ " where p.documentType='userProfileDetails'"
														+ " and p.secretKey=$1";
	 
	 public static final String PROPOSAL_RES_CODE 		= "responseCode";
	 public static final String PROPOSAL_RES_MSG 		= "message";
	 public static final String PROPOSAL_RES_DATA 		= "data";
	 public static final String RESPONSE_CONFIG_DOC 	= "ResponseMessages";

}
