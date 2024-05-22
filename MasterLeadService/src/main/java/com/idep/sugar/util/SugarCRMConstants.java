package com.idep.sugar.util;

public class SugarCRMConstants
{
  public static final String SUCCESS_MESSAGE = "Request has been forwarded to insurance company";
  public static final int SUCCESS_RESPONSE_CODE = 0;
  public static final String LEAD_RES_CODE = "responseCode";
  public static final String LEAD_RES_MSG = "message";
  public static final String LEAD_RES_DATA = "data";
  public static final String CONTACT_INFO = "contactInfo";
  public static final String QUOTE_PARAM = "quoteParam";
  public static final String QUOTE_ID = "QUOTE_ID";
  public static final String LATEST_QUOTE_ID = "latestQUOTE_ID";
  public static final String LATEST_QUOTE_LOB = "latestQuoteBusinessLineId";
  public static final String PROPOSAL_ID = "proposalId";
  public static final String LATEST_PROPOSAL_ID = "latestProposalId";
  public static final String LATEST_PROPOSAL_LOB = "latestProposalBusinessLineId";
  
  public static final String 	DOCUMENT_TYPE 		= "documentType";
  public static final String 	MESSAGE_ID 		    = "messageId";


  public static final String REQ_QUEUE = "Leads";
  public static final String ERROR_CONFIG_CODE 		= "errorCode";
  public static final String ERROR_CONFIG_MSG 		= "errorMessage";
  public static final String RESPONSE_CONFIG_DOC 	= "ResponseMessages";
  public static final String P365_IntegrationList 	= "P365IntegrationList";
  public static final String LEAD_CLOSED		 	= "closed";
  public static final String LEAD_STATUS_QUERY		= "SELECT leads.status,leads_cstm.messageid_c FROM leads JOIN leads_cstm"+
		  											  " ON leads.id = leads_cstm.id_c WHERE leads_cstm.messageid_c = ? AND "+
		  											  "deleted = 0 order by leads.date_entered desc";
  
  public static final String LEAD_PROFILE_QUERY		= "select messageId from PolicyTransaction where documentType = 'leadProfileRequest' and mobileNumber = '";
 
}
