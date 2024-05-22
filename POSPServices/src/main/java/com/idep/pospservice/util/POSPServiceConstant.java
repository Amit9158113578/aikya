package com.idep.pospservice.util;

public class POSPServiceConstant {
	public static final String DOCUMENT_TYPE 					= "documentType";
	public static final String DOCUMENT_ID 					    = "documentId";
	public static final String REQUEST_TYPE 					= "requestType";
	public static final String DOCID_CONFIG						= "DocumentIDConfig";
	public static final String REQUESTFLAG 						= "reqFlag";
	public static final String FALSE 							= "False";
	public static final String TRUE 							= "True";	
	public static final String RES_CODE 					    = "responseCode";
	public static final String RES_MSG 					        = "message";
	public static final String RES_MSG_USERNOTEXIST 		    = "response message:1000-User Not Exist";
	public static final String RES_MSG_USEREXIST 		        = "response message:1006-User Exist";
	public static final String RES_DATA 					    = "data";
	public static final String ERROR_CONFIG_CODE 				= "errorCode";
	public static final String ERROR_CONFIG_MSG 				= "errorMessage";
	public static final String SUCC_CONFIG_CODE 				= "successCode";
	public static final String SUCC_CONFIG_MSG 					= "successMessage";
	public static final String RESPONSE_MESSAGES 				= "ResponseMessages";
	public static final String NORECORD_MESSAGES 				= "noRecordsMessage";
	public static final String NORECORD_CODE 					= "noRecordsCode";
	public static final String FAILURE_MESSAGES 				= "failureMessage";
	public static final String FAILURE_CODE 					= "failureCode";
	public static final String LOGIN_FAILED_CODE 				= "loginFailedCode";
	public static final String LOGIN_FAILED_MESSAGES			= "loginFailedMessage";
	public static final String USER_EXIST_MESSAGES 				= "userExistMessage";
	public static final String USER_EXIST_CODE 					= "userExistCode";
	public static final String POSP_SEQ_AD 		                = "SEQPOSPAD";
	public static final String POSP_SEQ_AG 		                = "SEQPOSPAG";
	public static final String POSPAD_ID		                = "POSP00AD";
	public static final String POSPAG_ID		                = "NIPOS000";
	public static final String DOC_CREATED		                = "doc_created";
	public static final String DOC_REPLACED		                = "doc_replaced";
	public static final String DOC_UPDATED                      = "doc_updated";
	public static final String POSP_USER_PROFILE                = "POSPUserProfile-";
	public static final String POSP_ADMIN_PROFILE               = "POSPAdminProfile-";
	public static final String USER_EXIST                       = "UserExist";
	public static final String SERVICE_DATE_FORMAT              = "dd/MM/yyyy hh:mm:ss";
	public static final String BLOCKED_MOBILES                  = "BlockedMobilesList";
	public static final String BLOCKED_MOBILES_CODE             = "blockedMobileCode";
	public static final String MOBILE_NO                        = "mobileNumber";
	public static final String OTP                              = "OTP";
	public static final String SMS                              = "SMS-";
	public static final String NO_RECORDS_CODE                  = "noRecordsCode";
	public static final String NO_RECORDS_MSG                   = "noRecordsMessage";
    public static final String CREATED_DATE_TIME                = "createdDateTime";
	public static final String EXPIRATION_TIME                  = "expirationTime"; 
	public static final String EXPIRED_OTP_CODE                 = "expiredOTPCode";
	public static final String EXPIRED_OTP_MSG                  = "expiredOTPMessage";
	public static final String ADMIN                            = "Admin";
	public static final String ROLEID                           = "Role-";
	public static final String GROPID                           = "Group-";
	public static final String STATUS                           = "status";
	public static final String STAGE                            = "stage";
	public static final String PROFILE_VERF                     = "isProfileVerified";
	public static final String USER_PROFILE_DEFAULT_CONFIG      = "PospUserProfileDefaultConfig";
	
	public static final String TRANING_COMP                     = "isTraningCompleted";
    public static final String LEAD_QUERRY                      = "SELECT id,first_name,last_name,phone_mobile,useremail_c,lineof_business_c,status,lead_source,stage_c,date_entered,agencyid_c FROM leads join leads_cstm on leads.id = leads_cstm.id_c where campaign_id = '59f9bb13-6a98-870c-f66d-5bffa9f88f6f' order by date_entered desc";
    public static final String CUSTOMER_QUERY                   = "select mobile_c,aos_contracts_cstm.policynumber_c,aos_contracts_cstm.total_amt_c,aos_contracts_cstm.messageid_c,aos_contracts_cstm.carriername_c,aos_contracts_cstm.email_c,aos_contracts_cstm.start_date1_c,aos_contracts_cstm.end_date1_c,aos_contracts_cstm.leadid_c,aos_contracts_cstm.policy_issue_date_c from leads_cstm join aos_contracts_cstm on leads_cstm.id_c = leadid_c where campaign_id_c = '59f9bb13-6a98-870c-f66d-5bffa9f88f6f' order by aos_contracts_cstm.policy_issue_date_c desc";
    public static final String REQ_QUEUE                        = "Leads";
    public static final String POSP_TICKET_CREATED_DATE         = "pospTicketCreatedDate";
    public static final String POSPTICKETID        			    = "id";
	public static final String ERROR_CODE_VAL					= "1010";
	public static final String ERROR_MSG_VAL 					= "failure";
	public static final String SUCCESS_MSG_VAL 					= "success";
	
  /*
   * POSP Ticket Constants
   */
	public static final String POSP_TICKET_CONFIG      			= "POSPTicketConfig";
	public static final String NEW_TICKET_STATUS      			= "new";
	public static final String TICKET_DOC_TYPE      			= "POSPTicket";
	public static final String TICKET_DOC_NOT_CREATED      		= "doc_not_created";
	public static final String TICKET_SUCCESS_RES_CODE      	= "1000";
	public static final String TICKET_FAILURE_RES_CODE      	= "1002";
	public static final String NO_RECORD_RES_CODE      			= "1009";
	public static final String TICKET_NO_RECORD_FOUND	      	= "No Record Found";
	public static final String TICKET_CREATED			      	= "ticket created";
	public static final String TICKET_NOT_CREATED			    = "ticket not created";
	public static final String NO_RECORD_FOUND				    = "No Records Found";
	public static final String POSP_BUCKET				  		= "PospData";
	public static final String PROFILE_ERROR_MSG				  		= "User Profile Not Found";
	

}  

