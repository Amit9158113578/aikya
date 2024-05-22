/**
 * 
 */
package com.idep.policydownload.util;

/**
 * @author vipin.patil
 *
 * Apr 27, 2017
 */
public class PolicyDownloadViewConstants {
	
	public static final String RESPONSE_MSG 			= "ResponseMessages";
	public static final String PROPOSAL_RES_CODE 		= "responseCode";
	public static final String PROPOSAL_RES_MSG 		= "message";
	public static final String PROPOSAL_RES_DATA 		= "data";
	public static final String SUCC_CONFIG_CODE 		= "successCode";
	public static final String SUCC_CONFIG_MSG 			= "successMessage";
	public static final String ERROR_CONFIG_CODE 		= "errorCode";
	public static final String ERROR_CONFIG_MSG 		= "errorMessage";
	public static final String USER_PROFILE_QUERY		= " select p.proposalDetails,p.lastName,p.pincode,p.gender,p.mobile,p.dateOfBirth,p.emailId,p.aadharId,"
														    + " p.firstName,p.addressLine1,p.addressLine2,p.maritalStatus from PolicyTransaction p "
														    + " where p.documentType='userProfileDetails'"
														    + " and p.secretKey=$1";

}
