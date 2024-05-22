package com.idep.smsemail.util;

public class SMSConstants
{
  public static final String USERNOTREG = "User not Registered";
  public static final int OTP = 0;
  public static final String ERRORMOBILENUMBER = "0";
  public static final String EXCEPTION = "001";
  public static final String ERRORSMS = "ERROR";
  public static final int ERROROTP = -1;
  public static final String SUCCESSMESSAGE = "Success";
  public static final String MESSAGE = "message";
  public static final String RESPONSECODE = "responseCode";
  
  public static final String FAILUREMESSAGE = "Failure";
  public static final int ERRORRESPONSE = -1;
  public static final int SUCCESSRESPONSE = 0;
  public static final String INVALID = "Invalid";
  public static final String MSG_TEMPLATE = "message";
  public static final String FUNC_TYPE = "funcType";
  public static final String PARAM_MAP = "paramMap";
  public static final String MOBILE_NO = "mobileNumber";
  public static final String VALIDATION_FLAG = "validationFlag";
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  
  public static final String SESSIONKEYBUCKET = "SessionKey";
  
  public static final String SUCC_CONFIG_CODE = "successCode";
  public static final String SUCC_CONFIG_MSG = "successMessage";
  public static final String FAIL_CONFIG_CODE = "failureCode";
  public static final String FAIL_CONFIG_MSG = "failureMessage";
  public static final String ERROR_CONFIG_CODE = "errorCode";
  public static final String RESPONSEDATA = "data";
  public static final String MOBILE_NO_NOT_EXIST_CODE = "mobileInvalidCode";
  public static final String MOBILE_NO_NOT_EXIST_MSG = "mobileInvalidMessage";
  
  
  public static final String ERROR_CONFIG_MSG = "errorMessage";
  public static final String NO_RECORDS_CODE = "noRecordsCode";
  public static final String NO_RECORDS_MSG = "noRecordsMessage";
  
  public static final String RESPONSE_MSG = "ResponseMessages";
  public static final String BLOCKED_MOBILES = "BlockedMobilesList";
  public static final String BLOCKED_MOBILES_CODE = "blockedMobileCode";
  public static final String USER_PROFILE = "UserProfile-";
  public static final String SESSIONIDQUERY = "select meta().id as documentid ,session.* from SessionKey session where documentType='webSessionKey' and sessionId=$1 order by documentid desc"; 
  public static final String MOBILESESSIONIDQUERY="select meta().id as documentid ,session.* from SessionKey session where documentType='webSessionKey' and mobileNumber=$1 and STR_TO_MILLIS(updatedDate) = STR_TO_MILLIS($2) order by documentid desc"; 
  public static final String GET_PROFESIONAINFO_SELECT_QUERY="select QUOTE_ID from QuoteData where documentType='professionalQuoteRequest' and commonInfo.mobileNumber=$1 order by TONUMBER(SUBSTR(QUOTE_ID, 10)) desc limit 1"; 

}
