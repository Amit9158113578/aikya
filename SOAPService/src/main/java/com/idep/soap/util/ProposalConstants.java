package com.idep.soap.util;

public class ProposalConstants {
  public static final String INPUT_REQUEST = "inputRequest";
  
  public static final String RESONSE_MESSAGES = "ResponseMessages";
  
  public static final String REQ_CONF_QUERY_OG = "select ServerConfig.* from ServerConfig where documentType='SOAPRequestConfiguration' and lob=$1 and carrierId=$2 and productId=$3 and stage=$4";
  
  public static final String JOLT_REQ_QUERY_OG = "select ServerConfig.* from ServerConfig where documentType='JOLTRequestConfiguration' and lob=$1 and carrierId=$2 and productId=$3 and stage=$4";
  
  public static final String RES_CONF_DOC = "ResponseConfigDoc";
  
  public static final String REPLACE_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
  
  public static final String REPLACE_STRING2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
  
  public static final String REPLACE_STRING3 = "<o>";
  
  public static final String REPLACE_STRING4 = "</o>&#13;";
  
  public static final String RES_CODE = "responseCode";
  
  public static final String STATUS = "status";
  
  public static final String MESSAGE = "message";
  
  public static final String DATA = "data";
  
  public static final String CARRIER_ID = "carrierId";
  
  public static final String PRODUCT_ID = "productId";
  
  public static final String STAGE = "stage";
  
  public static final String LOB = "lob";
  
  public static final String POLICY_TYPE = "policyType";
  
  public static final String DOC_TYPE = "documentType";
  
  public static final String VEHICLE_INFO = "vehicleInfo";
  
  public static final String BEST_QUOTE_ID = "best_quote_id";
  
  public static final String PRODUCT_INFO = "productInfo";
  
  public static final String QUOTE_BUCKET = "QuoteData";
  
  public static final String QUOTES = "quotes";
  
  public static final String MIN_IDV = "minIdvValue";
  
  public static final String MAX_IDV = "maxIdvValue";
}


